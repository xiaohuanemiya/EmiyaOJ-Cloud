package com.emiyaoj.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.emiyaoj.auth.api.AuthUserFeignClient;
import com.emiyaoj.auth.vo.UserVO;
import com.emiyaoj.blog.domain.*;
import com.emiyaoj.blog.dto.*;
import com.emiyaoj.blog.config.BlogModerationProperties;
import com.emiyaoj.blog.mapper.*;
import com.emiyaoj.blog.service.BlogImageUrlResolver;
import com.emiyaoj.blog.service.IBlogService;
import com.emiyaoj.blog.service.ModerationTaskPublisher;
import com.emiyaoj.blog.vo.BlogPictureVO;
import com.emiyaoj.blog.vo.BlogTagVO;
import com.emiyaoj.blog.vo.BlogVO;
import com.emiyaoj.blog.vo.CommentVO;
import com.emiyaoj.common.domain.PageDTO;
import com.emiyaoj.common.domain.PageVO;
import com.emiyaoj.common.domain.ResponseResult;
import com.emiyaoj.common.exception.BaseException;
import com.emiyaoj.common.utils.RedisUtil;
import com.emiyaoj.moderation.dto.AuditStatus;
import com.emiyaoj.moderation.dto.ModerationResultDTO;
import com.emiyaoj.moderation.dto.ModerationTargetType;
import com.emiyaoj.moderation.dto.ModerationTaskMessage;
import com.emiyaoj.problem.api.ProblemFeignClient;
import com.emiyaoj.problem.dto.ProblemVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Blog service implementation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

    private static final int BLOG_TYPE_NORMAL = 0;
    private static final int BLOG_TYPE_SOLUTION = 1;
    private static final long VIEW_TTL_MILLIS = 24 * 60 * 60 * 1000L;
    private static final String MQ_PUBLISH_FAILED_REASON = "Moderation message publish failed";

    private final BlogTagMapper blogTagMapper;
    private final BlogTagAssociationMapper blogTagAssociationMapper;
    private final BlogCommentMapper blogCommentMapper;
    private final BlogPictureMapper blogPictureMapper;
    private final BlogLikeMapper blogLikeMapper;
    private final AuthUserFeignClient authUserFeignClient;
    private final ProblemFeignClient problemFeignClient;
    private final RedisUtil redisUtil;
    private final ModerationTaskPublisher moderationTaskPublisher;
    private final BlogImageUrlResolver blogImageUrlResolver;

    @Override
    public List<BlogVO> selectAll() {
        List<Blog> blogs = list(new LambdaQueryWrapper<Blog>()
                .eq(Blog::getAuditStatus, AuditStatus.APPROVED.getCode()));
        Map<Long, UserVO> usersById = loadUsersByIds(blogs.stream().map(Blog::getUserId).toList());
        return blogs.stream()
                .map(blog -> convertBlogToVO(blog, null, false, usersById))
                .toList();
    }

    @Override
    public PageVO<BlogVO> select(BlogQueryDTO queryDTO) {
        return select(queryDTO, null);
    }

    @Override
    public PageVO<BlogVO> select(BlogQueryDTO queryDTO, Long userId) {
        return select(queryDTO, userId, null);
    }

    @Override
    public PageVO<BlogVO> select(BlogQueryDTO queryDTO, Long userId, String permissions) {
        Page<Blog> page = new Page<>(queryDTO.getPageNo(), queryDTO.getPageSize());
        LambdaQueryWrapper<Blog> wrapper = buildBlogQueryWrapper(queryDTO, userId, permissions);
        this.page(page, wrapper);
        Map<Long, UserVO> usersById = loadUsersByIds(page.getRecords().stream().map(Blog::getUserId).toList());
        return PageVO.of(page, blog -> convertBlogToVO(blog, userId, false, usersById));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBlog(BlogSaveDTO saveDTO, Long userId) {
        int blogType = saveDTO.getBlogType() == null ? BLOG_TYPE_NORMAL : saveDTO.getBlogType();
        if (blogType == BLOG_TYPE_SOLUTION) {
            if (saveDTO.getProblemId() == null) {
                throw new BaseException(400, "题解必须关联题目");
            }
            return saveSolution(saveDTO.getProblemId(), saveDTO, userId);
        }
        saveDTO.setBlogType(BLOG_TYPE_NORMAL);
        saveDTO.setProblemId(null);
        Blog blog = createBlog(saveDTO, userId);
        return blog.getId() != null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveSolution(Long problemId, BlogSaveDTO saveDTO, Long userId) {
        ProblemVO problem = requireProblem(problemId);
        saveDTO.setBlogType(BLOG_TYPE_SOLUTION);
        saveDTO.setProblemId(problem.getId());

        Blog existing = getOne(new LambdaQueryWrapper<Blog>()
                .eq(Blog::getUserId, userId)
                .eq(Blog::getProblemId, problem.getId())
                .eq(Blog::getBlogType, BLOG_TYPE_SOLUTION)
                .last("LIMIT 1"));
        if (existing != null) {
            existing.setTitle(saveDTO.getTitle());
            existing.setContent(saveDTO.getContent());
            existing.setUpdateTime(LocalDateTime.now());
            existing.setDeleted(0);
            prepareForAudit(existing);
            if (!updateById(existing)) {
                return false;
            }
            replaceTags(existing.getId(), saveDTO.getTagIds());
            bindPictures(existing.getId(), saveDTO.getPictureIds(), userId);
            submitBlogModeration(getById(existing.getId()));
            return true;
        }

        Blog blog = createBlog(saveDTO, userId);
        return blog.getId() != null;
    }

    @Override
    public PageVO<BlogVO> selectProblemSolutions(Long problemId, BlogQueryDTO queryDTO, Long userId) {
        requireProblem(problemId);
        queryDTO.setProblemId(problemId);
        queryDTO.setBlogType(BLOG_TYPE_SOLUTION);
        Page<Blog> page = new Page<>(queryDTO.getPageNo(), queryDTO.getPageSize());
        this.page(page, buildBlogQueryWrapper(queryDTO, userId, null));
        Map<Long, UserVO> usersById = loadUsersByIds(page.getRecords().stream().map(Blog::getUserId).toList());
        return PageVO.of(page, blog -> convertBlogToVO(blog, userId, false, usersById));
    }

    @Override
    public BlogVO selectBlogById(Long blogId) {
        return selectBlogById(blogId, null);
    }

    @Override
    public BlogVO selectBlogById(Long blogId, Long userId) {
        return selectBlogById(blogId, userId, null);
    }

    @Override
    public BlogVO selectBlogById(Long blogId, Long userId, String permissions) {
        Blog blog = getById(blogId);
        if (blog == null || !isApproved(blog)) {
            return null;
        }
        if (!canViewBlog(blog, userId, permissions)) {
            return null;
        }
        if (isApproved(blog)) {
            increaseViewCount(blog, userId);
        }
        Blog refreshed = getById(blogId);
        Map<Long, UserVO> usersById = loadUsersByIds(
                refreshed == null || refreshed.getUserId() == null ? List.of() : List.of(refreshed.getUserId()));
        return convertBlogToVO(refreshed, userId, true, usersById);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteBlogById(Long blogId) {
        Blog blog = getById(blogId);
        if (blog == null) {
            return false;
        }
        blogTagAssociationMapper.delete(
                new LambdaQueryWrapper<BlogTagAssociation>().eq(BlogTagAssociation::getBlogId, blogId));
        blogPictureMapper.update(new LambdaUpdateWrapper<BlogPicture>()
                .eq(BlogPicture::getBlogId, blogId)
                .set(BlogPicture::getBlogId, null));
        return this.removeById(blogId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean editBlog(BlogEditDTO editDTO, Long userId) {
        Blog existing = getById(editDTO.getId());
        if (existing == null) {
            throw new BaseException(404, "博客不存在");
        }
        if (!existing.getUserId().equals(userId)) {
            throw new BaseException(403, "只能修改自己的博客");
        }
        Blog blog = new Blog();
        blog.setId(editDTO.getId());
        blog.setTitle(editDTO.getTitle());
        blog.setContent(editDTO.getContent());
        blog.setUpdateTime(LocalDateTime.now());
        boolean textChanged = editDTO.getTitle() != null || editDTO.getContent() != null;
        if (textChanged) {
            prepareForAudit(blog);
        }
        boolean updated = this.updateById(blog);
        if (updated && editDTO.getPictureIds() != null) {
            bindPictures(editDTO.getId(), editDTO.getPictureIds(), userId);
        }
        if (updated && textChanged) {
            submitBlogModeration(getById(editDTO.getId()));
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean likeBlog(Long blogId, Long userId) {
        Blog blog = requireActiveBlog(blogId);
        int restored = blogLikeMapper.update(new LambdaUpdateWrapper<BlogLike>()
                .eq(BlogLike::getUserId, userId)
                .eq(BlogLike::getBlogId, blogId)
                .eq(BlogLike::getDeleted, 1)
                .set(BlogLike::getDeleted, 0)
                .set(BlogLike::getCreateTime, LocalDateTime.now()));
        if (restored == 1) {
            incrementLikeCount(blog.getId(), 1);
            return true;
        }
        BlogLike active = blogLikeMapper.selectOne(new LambdaQueryWrapper<BlogLike>()
                .eq(BlogLike::getUserId, userId)
                .eq(BlogLike::getBlogId, blogId)
                .eq(BlogLike::getDeleted, 0)
                .last("LIMIT 1"));
        if (active != null) {
            return true;
        }
        try {
            blogLikeMapper.insert(new BlogLike(null, userId, blogId, LocalDateTime.now(), 0));
        } catch (DuplicateKeyException e) {
            return true;
        }
        incrementLikeCount(blog.getId(), 1);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unlikeBlog(Long blogId, Long userId) {
        int updated = blogLikeMapper.update(new LambdaUpdateWrapper<BlogLike>()
                .eq(BlogLike::getUserId, userId)
                .eq(BlogLike::getBlogId, blogId)
                .eq(BlogLike::getDeleted, 0)
                .set(BlogLike::getDeleted, 1));
        if (updated == 1) {
            incrementLikeCount(blogId, -1);
        }
        return true;
    }

    @Override
    public List<BlogTagVO> selectAllTags() {
        return blogTagMapper.selectList(null).stream()
                .map(this::convertTagToVO)
                .toList();
    }

    @Override
    public BlogTagVO selectTagById(Long tagId) {
        return convertTagToVO(blogTagMapper.selectById(tagId));
    }

    @Override
    public BlogTagVO saveTag(BlogTagSaveDTO saveDTO) {
        BlogTag tag = new BlogTag();
        tag.setName(saveDTO.getName());
        tag.setDesc(saveDTO.getDesc());
        blogTagMapper.insert(tag);
        return convertTagToVO(tag);
    }

    @Override
    public BlogTagVO updateTag(BlogTagSaveDTO saveDTO) {
        BlogTag tag = blogTagMapper.selectById(saveDTO.getId());
        if (tag == null) {
            throw new BaseException(404, "标签不存在");
        }
        tag.setName(saveDTO.getName());
        tag.setDesc(saveDTO.getDesc());
        blogTagMapper.updateById(tag);
        return convertTagToVO(tag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTagById(Long tagId) {
        BlogTag tag = blogTagMapper.selectById(tagId);
        if (tag == null) {
            throw new BaseException(404, "标签不存在");
        }
        blogTagAssociationMapper.delete(
                new LambdaQueryWrapper<BlogTagAssociation>().eq(BlogTagAssociation::getTagId, tagId));
        return blogTagMapper.deleteById(tagId) == 1;
    }

    @Override
    public PageVO<CommentVO> selectCommentPage(Long blogId, PageDTO pageDTO) {
        return selectCommentPage(blogId, pageDTO, null, null);
    }

    @Override
    public PageVO<CommentVO> selectCommentPage(Long blogId, PageDTO pageDTO, Long userId, String permissions) {
        Page<BlogComment> page = pageDTO.toMpPageDefaultSortByCreateTimeDesc();

        LambdaQueryWrapper<BlogComment> wrapper = new LambdaQueryWrapper<BlogComment>()
                .eq(BlogComment::getBlogId, blogId);
        applyCommentAuditVisibility(wrapper, userId, permissions, null);
        blogCommentMapper.selectPage(page, wrapper);

        Map<Long, UserVO> usersById = loadUsersByIds(page.getRecords().stream().map(BlogComment::getUserId).toList());
        return PageVO.of(page, comment -> convertCommentToVO(comment, usersById));
    }

    @Override
    public CommentVO selectCommentById(Long commentId) {
        return selectCommentById(commentId, null, null);
    }

    @Override
    public CommentVO selectCommentById(Long commentId, Long userId, String permissions) {
        BlogComment comment = blogCommentMapper.selectById(commentId);
        if (comment == null) {
            return null;
        }
        if (!canViewComment(comment, userId, permissions)) {
            return null;
        }
        Map<Long, UserVO> usersById = loadUsersByIds(comment.getUserId() == null ? List.of() : List.of(comment.getUserId()));
        return convertCommentToVO(comment, usersById);
    }

    @Override
    public List<CommentVO> selectComment(CommentQueryDTO queryDTO) {
        return selectComment(queryDTO, null, null);
    }

    @Override
    public List<CommentVO> selectComment(CommentQueryDTO queryDTO, Long userId, String permissions) {
        LambdaQueryWrapper<BlogComment> wrapper = new LambdaQueryWrapper<BlogComment>()
                .eq(queryDTO.getBlogId() != null, BlogComment::getBlogId, queryDTO.getBlogId())
                .ge(queryDTO.getFromDay() != null, BlogComment::getCreateTime, queryDTO.getFromDay())
                .le(queryDTO.getToDay() != null, BlogComment::getCreateTime, queryDTO.getToDay());
        applyCommentAuditVisibility(wrapper, userId, permissions, queryDTO.getAuditStatus());
        List<BlogComment> comments = blogCommentMapper.selectList(wrapper);
        Map<Long, UserVO> usersById = loadUsersByIds(comments.stream().map(BlogComment::getUserId).toList());
        return comments.stream()
                .map(comment -> convertCommentToVO(comment, usersById))
                .toList();
    }

    @Override
    public boolean saveComment(Long blogId, BlogCommentSaveDTO saveDTO, Long userId) {
        requireActiveBlog(blogId);
        BlogComment comment = new BlogComment();
        comment.setBlogId(blogId);
        comment.setUserId(userId);
        comment.setContent(saveDTO.getContent());
        prepareForAudit(comment);
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());
        comment.setDeleted(0);
        boolean inserted = blogCommentMapper.insert(comment) == 1;
        if (inserted) {
            submitCommentModeration(comment);
        }
        return inserted;
    }

    @Override
    public int deleteComment(Long commentId) {
        try {
            BlogComment blogComment = blogCommentMapper.selectById(commentId);
            if (blogComment == null) return HttpServletResponse.SC_NOT_FOUND;
            blogCommentMapper.deleteById(commentId);
            return HttpServletResponse.SC_OK;
        } catch (Exception e) {
            log.warn(e.getMessage());
            return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean applyModerationResult(ModerationResultDTO resultDTO) {
        if (resultDTO == null || resultDTO.getTargetType() == null || resultDTO.getTaskId() == null) {
            return false;
        }
        AuditStatus status = AuditStatus.fromCode(resultDTO.getAuditStatus());
        if (status == null) {
            return false;
        }
        LocalDateTime auditTime = resultDTO.getAuditTime() == null ? LocalDateTime.now() : resultDTO.getAuditTime();
        String reason = resultDTO.getReason() == null ? "" : resultDTO.getReason();
        String labels = resultDTO.getLabels() == null ? "" : resultDTO.getLabels();

        int updated = switch (resultDTO.getTargetType()) {
            case BLOG -> update(new LambdaUpdateWrapper<Blog>()
                    .eq(Blog::getId, resultDTO.getTargetId())
                    .eq(Blog::getAuditTaskId, resultDTO.getTaskId())
                    .set(Blog::getAuditStatus, status.getCode())
                    .set(Blog::getAuditReason, reason)
                    .set(Blog::getAuditLabels, labels)
                    .set(Blog::getAuditTime, auditTime)
                    .set(Blog::getUpdateTime, LocalDateTime.now())) ? 1 : 0;
            case COMMENT -> blogCommentMapper.update(new LambdaUpdateWrapper<BlogComment>()
                    .eq(BlogComment::getId, resultDTO.getTargetId())
                    .eq(BlogComment::getAuditTaskId, resultDTO.getTaskId())
                    .set(BlogComment::getAuditStatus, status.getCode())
                    .set(BlogComment::getAuditReason, reason)
                    .set(BlogComment::getAuditLabels, labels)
                    .set(BlogComment::getAuditTime, auditTime)
                    .set(BlogComment::getUpdateTime, LocalDateTime.now()));
        };
        if (updated == 0) {
            log.info("Ignored stale moderation result, taskId={}, targetType={}, targetId={}",
                    resultDTO.getTaskId(), resultDTO.getTargetType(), resultDTO.getTargetId());
        }
        return true;
    }

    @Override
    public boolean updateBlogAuditStatus(Long blogId, Integer auditStatus, String reason, String permissions) {
        requireModerationManager(permissions);
        AuditStatus status = requireAuditStatus(auditStatus);
        return update(new LambdaUpdateWrapper<Blog>()
                .eq(Blog::getId, blogId)
                .set(Blog::getAuditStatus, status.getCode())
                .set(Blog::getAuditReason, reason == null ? "Manual moderation" : reason)
                .set(Blog::getAuditLabels, "manual")
                .set(Blog::getAuditTime, LocalDateTime.now())
                .set(Blog::getUpdateTime, LocalDateTime.now()));
    }

    @Override
    public boolean updateCommentAuditStatus(Long commentId, Integer auditStatus, String reason, String permissions) {
        requireModerationManager(permissions);
        AuditStatus status = requireAuditStatus(auditStatus);
        return blogCommentMapper.update(new LambdaUpdateWrapper<BlogComment>()
                .eq(BlogComment::getId, commentId)
                .set(BlogComment::getAuditStatus, status.getCode())
                .set(BlogComment::getAuditReason, reason == null ? "Manual moderation" : reason)
                .set(BlogComment::getAuditLabels, "manual")
                .set(BlogComment::getAuditTime, LocalDateTime.now())
                .set(BlogComment::getUpdateTime, LocalDateTime.now())) == 1;
    }

    private LambdaQueryWrapper<Blog> buildBlogQueryWrapper(BlogQueryDTO queryDTO, Long userId, String permissions) {
        LambdaQueryWrapper<Blog> wrapper = new LambdaQueryWrapper<Blog>()
                .eq(Blog::getDeleted, 0)
                .like(!ObjectUtils.isEmpty(queryDTO.getTitle()), Blog::getTitle, queryDTO.getTitle())
                .eq(queryDTO.getBlogType() != null, Blog::getBlogType, queryDTO.getBlogType())
                .eq(queryDTO.getProblemId() != null, Blog::getProblemId, queryDTO.getProblemId());

        applyBlogAuditVisibility(wrapper, userId, permissions, queryDTO.getAuditStatus());

        if (queryDTO.getTagId() != null) {
            List<Long> blogIds = blogTagAssociationMapper.selectList(new LambdaQueryWrapper<BlogTagAssociation>()
                            .eq(BlogTagAssociation::getTagId, queryDTO.getTagId()))
                    .stream()
                    .map(BlogTagAssociation::getBlogId)
                    .toList();
            wrapper.in(!blogIds.isEmpty(), Blog::getId, blogIds);
            wrapper.eq(blogIds.isEmpty(), Blog::getId, -1L);
        }

        Optional.ofNullable(queryDTO.getCreateTime()).ifPresent(t -> {
            LocalDateTime startOfDay = t.toLocalDate().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);
            wrapper.between(Blog::getCreateTime, startOfDay, endOfDay);
        });

        applySort(wrapper, queryDTO.getSortBy());
        return wrapper;
    }

    private void applyBlogAuditVisibility(LambdaQueryWrapper<Blog> wrapper,
                                          Long userId,
                                          String permissions,
                                          Integer auditStatus) {
        if (isModerationManager(permissions)) {
            wrapper.eq(auditStatus != null, Blog::getAuditStatus, auditStatus);
            wrapper.eq(auditStatus == null, Blog::getAuditStatus, AuditStatus.APPROVED.getCode());
            return;
        }
        if (userId != null && auditStatus != null) {
            wrapper.eq(Blog::getUserId, userId)
                    .eq(Blog::getAuditStatus, auditStatus);
            return;
        }
        wrapper.eq(Blog::getAuditStatus, AuditStatus.APPROVED.getCode());
    }

    private void applyCommentAuditVisibility(LambdaQueryWrapper<BlogComment> wrapper,
                                             Long userId,
                                             String permissions,
                                             Integer auditStatus) {
        if (isModerationManager(permissions)) {
            wrapper.eq(auditStatus != null, BlogComment::getAuditStatus, auditStatus);
            return;
        }
        if (userId != null && auditStatus != null) {
            wrapper.eq(BlogComment::getUserId, userId)
                    .eq(BlogComment::getAuditStatus, auditStatus);
            return;
        }
        if (userId != null) {
            wrapper.and(w -> w.eq(BlogComment::getAuditStatus, AuditStatus.APPROVED.getCode())
                    .or()
                    .eq(BlogComment::getUserId, userId));
            return;
        }
        wrapper.eq(BlogComment::getAuditStatus, AuditStatus.APPROVED.getCode());
    }

    private boolean canViewBlog(Blog blog, Long userId, String permissions) {
        return isApproved(blog)
                || isModerationManager(permissions)
                || (userId != null && userId.equals(blog.getUserId()));
    }

    private boolean canViewComment(BlogComment comment, Long userId, String permissions) {
        return isApproved(comment)
                || isModerationManager(permissions)
                || (userId != null && userId.equals(comment.getUserId()));
    }

    private boolean isApproved(Blog blog) {
        return blog != null && Integer.valueOf(AuditStatus.APPROVED.getCode()).equals(blog.getAuditStatus());
    }

    private boolean isApproved(BlogComment comment) {
        return comment != null && Integer.valueOf(AuditStatus.APPROVED.getCode()).equals(comment.getAuditStatus());
    }

    private boolean isModerationManager(String permissions) {
        if (!org.springframework.util.StringUtils.hasText(permissions)) {
            return false;
        }
        for (String permission : permissions.split(",")) {
            if (BlogModerationProperties.MANAGE_PERMISSION.equals(permission.trim())) {
                return true;
            }
        }
        return false;
    }

    private void requireModerationManager(String permissions) {
        if (!isModerationManager(permissions)) {
            throw new BaseException(403, "No moderation permission");
        }
    }

    private AuditStatus requireAuditStatus(Integer auditStatus) {
        AuditStatus status = AuditStatus.fromCode(auditStatus);
        if (status == null || status == AuditStatus.PENDING) {
            throw new BaseException(400, "Invalid audit status");
        }
        return status;
    }

    private void prepareForAudit(Blog blog) {
        blog.setAuditStatus(AuditStatus.PENDING.getCode());
        blog.setAuditTaskId(newAuditTaskId());
        blog.setAuditReason("");
        blog.setAuditLabels("");
        blog.setAuditTime(LocalDateTime.now());
    }

    private void prepareForAudit(BlogComment comment) {
        comment.setAuditStatus(AuditStatus.PENDING.getCode());
        comment.setAuditTaskId(newAuditTaskId());
        comment.setAuditReason("");
        comment.setAuditLabels("");
        comment.setAuditTime(LocalDateTime.now());
    }

    private void submitBlogModeration(Blog blog) {
        if (blog == null) {
            return;
        }
        ModerationTaskMessage message = new ModerationTaskMessage(
                blog.getAuditTaskId(),
                ModerationTargetType.BLOG,
                blog.getId(),
                blog.getUserId(),
                blog.getTitle(),
                blog.getContent(),
                LocalDateTime.now()
        );
        if (!moderationTaskPublisher.publishBlog(message)) {
            markBlogManualReview(blog.getId(), blog.getAuditTaskId(), MQ_PUBLISH_FAILED_REASON);
        }
    }

    private void submitCommentModeration(BlogComment comment) {
        if (comment == null) {
            return;
        }
        ModerationTaskMessage message = new ModerationTaskMessage(
                comment.getAuditTaskId(),
                ModerationTargetType.COMMENT,
                comment.getId(),
                comment.getUserId(),
                null,
                comment.getContent(),
                LocalDateTime.now()
        );
        if (!moderationTaskPublisher.publishComment(message)) {
            markCommentManualReview(comment.getId(), comment.getAuditTaskId(), MQ_PUBLISH_FAILED_REASON);
        }
    }

    private void markBlogManualReview(Long blogId, String taskId, String reason) {
        update(new LambdaUpdateWrapper<Blog>()
                .eq(Blog::getId, blogId)
                .eq(Blog::getAuditTaskId, taskId)
                .set(Blog::getAuditStatus, AuditStatus.MANUAL_REVIEW.getCode())
                .set(Blog::getAuditReason, reason)
                .set(Blog::getAuditLabels, "system")
                .set(Blog::getAuditTime, LocalDateTime.now())
                .set(Blog::getUpdateTime, LocalDateTime.now()));
    }

    private void markCommentManualReview(Long commentId, String taskId, String reason) {
        blogCommentMapper.update(new LambdaUpdateWrapper<BlogComment>()
                .eq(BlogComment::getId, commentId)
                .eq(BlogComment::getAuditTaskId, taskId)
                .set(BlogComment::getAuditStatus, AuditStatus.MANUAL_REVIEW.getCode())
                .set(BlogComment::getAuditReason, reason)
                .set(BlogComment::getAuditLabels, "system")
                .set(BlogComment::getAuditTime, LocalDateTime.now())
                .set(BlogComment::getUpdateTime, LocalDateTime.now()));
    }

    private String newAuditTaskId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private void applySort(LambdaQueryWrapper<Blog> wrapper, String sortBy) {
        if ("viewCount".equals(sortBy)) {
            wrapper.orderByDesc(Blog::getViewCount);
        } else if ("likeCount".equals(sortBy)) {
            wrapper.orderByDesc(Blog::getLikeCount);
        } else if ("updateTime".equals(sortBy)) {
            wrapper.orderByDesc(Blog::getUpdateTime);
        } else {
            wrapper.orderByDesc(Blog::getCreateTime);
        }
    }

    private Blog createBlog(BlogSaveDTO saveDTO, Long userId) {
        Blog blog = new Blog();
        blog.setUserId(userId);
        blog.setTitle(saveDTO.getTitle());
        blog.setContent(saveDTO.getContent());
        blog.setBlogType(saveDTO.getBlogType() == null ? BLOG_TYPE_NORMAL : saveDTO.getBlogType());
        blog.setProblemId(blog.getBlogType() == BLOG_TYPE_SOLUTION ? saveDTO.getProblemId() : null);
        blog.setViewCount(0);
        blog.setLikeCount(0);
        prepareForAudit(blog);
        blog.setCreateTime(LocalDateTime.now());
        blog.setUpdateTime(LocalDateTime.now());
        blog.setDeleted(0);
        if (!this.save(blog)) {
            throw new BaseException("添加失败");
        }
        replaceTags(blog.getId(), saveDTO.getTagIds());
        bindPictures(blog.getId(), saveDTO.getPictureIds(), userId);
        submitBlogModeration(blog);
        return blog;
    }

    private void replaceTags(Long blogId, List<Long> tagIds) {
        blogTagAssociationMapper.delete(
                new LambdaQueryWrapper<BlogTagAssociation>().eq(BlogTagAssociation::getBlogId, blogId));
        if (CollectionUtils.isEmpty(tagIds)) {
            return;
        }
        List<BlogTag> tags = blogTagMapper.selectByIds(tagIds);
        if (tags.size() != tagIds.stream().distinct().count()) {
            throw new BaseException(400, "标签不存在");
        }
        List<BlogTagAssociation> associations = tagIds.stream()
                .distinct()
                .map(tagId -> new BlogTagAssociation(null, blogId, tagId))
                .toList();
        blogTagAssociationMapper.insert(associations);
    }

    private void bindPictures(Long blogId, List<Long> pictureIds, Long userId) {
        blogPictureMapper.update(new LambdaUpdateWrapper<BlogPicture>()
                .eq(BlogPicture::getBlogId, blogId)
                .set(BlogPicture::getBlogId, null));
        if (CollectionUtils.isEmpty(pictureIds)) {
            return;
        }
        List<Long> ids = pictureIds.stream().distinct().toList();
        List<BlogPicture> pictures = blogPictureMapper.selectByIds(ids);
        if (pictures.size() != ids.size()) {
            throw new BaseException(400, "图片不存在");
        }
        boolean invalid = pictures.stream().anyMatch(picture ->
                !picture.getUserId().equals(userId) || Integer.valueOf(1).equals(picture.getDeleted()));
        if (invalid) {
            throw new BaseException(400, "只能绑定自己上传的有效图片");
        }
        blogPictureMapper.update(new LambdaUpdateWrapper<BlogPicture>()
                .in(BlogPicture::getId, ids)
                .set(BlogPicture::getBlogId, blogId));
    }

    private ProblemVO requireProblem(Long problemId) {
        ResponseResult<ProblemVO> result = problemFeignClient.getProblemById(problemId);
        if (result == null || result.getCode() != 200 || result.getData() == null) {
            throw new BaseException(404, "题目不存在");
        }
        return result.getData();
    }

    private Blog requireActiveBlog(Long blogId) {
        Blog blog = getById(blogId);
        if (blog == null || Integer.valueOf(1).equals(blog.getDeleted())) {
            throw new BaseException(404, "博客不存在");
        }
        return blog;
    }

    private void increaseViewCount(Blog blog, Long userId) {
        if (userId == null) {
            return;
        }
        String key = "blog:view:%d:%d".formatted(blog.getId(), userId);
        if (redisUtil.hasKey(key)) {
            return;
        }
        redisUtil.set(key, "1", VIEW_TTL_MILLIS);
        update(new LambdaUpdateWrapper<Blog>()
                .eq(Blog::getId, blog.getId())
                .setSql("view_count = view_count + 1"));
    }

    private void incrementLikeCount(Long blogId, int delta) {
        String sql = delta > 0 ? "like_count = like_count + 1" : "like_count = GREATEST(like_count - 1, 0)";
        update(new LambdaUpdateWrapper<Blog>()
                .eq(Blog::getId, blogId)
                .setSql(sql));
    }

    private BlogTagVO convertTagToVO(BlogTag tag) {
        if (tag == null) return null;
        return new BlogTagVO(tag.getId(), tag.getName(), tag.getDesc());
    }

    private Map<Long, UserVO> loadUsersByIds(List<Long> userIds) {
        List<Long> ids = userIds == null ? List.of() : userIds.stream()
                .filter(id -> id != null)
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        try {
            ResponseResult<List<UserVO>> result = authUserFeignClient.listUsersByIds(ids);
            if (result == null || result.getCode() != 200 || result.getData() == null) {
                return Map.of();
            }
            return result.getData().stream()
                    .filter(user -> user.getId() != null)
                    .collect(Collectors.toMap(UserVO::getId, Function.identity(), (left, right) -> left));
        } catch (Exception e) {
            log.warn("load users failed, ids={}", ids, e);
            return Map.of();
        }
    }

    private String nicknameOf(Long userId, Map<Long, UserVO> usersById) {
        UserVO user = usersById.get(userId);
        return user != null && org.springframework.util.StringUtils.hasText(user.getNickname()) ? user.getNickname() : "";
    }

    private String usernameOf(Long userId, Map<Long, UserVO> usersById) {
        UserVO user = usersById.get(userId);
        return user != null && org.springframework.util.StringUtils.hasText(user.getUsername()) ? user.getUsername() : "";
    }

    private BlogVO convertBlogToVO(Blog blog, Long userId, boolean includeProblem, Map<Long, UserVO> usersById) {
        if (blog == null) return null;
        BlogVO blogVO = new BlogVO();
        BeanUtils.copyProperties(blog, blogVO);
        blogVO.setAuthorNickname(nicknameOf(blog.getUserId(), usersById));
        blogVO.setContent(blogImageUrlResolver.rewriteLegacyContentUrls(blog.getContent()));
        blogVO.setTags(selectBlogTags(blog.getId()));
        blogVO.setPictures(selectBlogPictures(blog.getId()));
        blogVO.setLiked(isLiked(blog.getId(), userId));
        if (includeProblem && Integer.valueOf(BLOG_TYPE_SOLUTION).equals(blog.getBlogType()) && blog.getProblemId() != null) {
            try {
                blogVO.setProblemTitle(requireProblem(blog.getProblemId()).getTitle());
            } catch (Exception e) {
                log.warn("load problem title failed, problemId={}", blog.getProblemId());
            }
        }
        return blogVO;
    }

    private List<BlogTagVO> selectBlogTags(Long blogId) {
        List<Long> tagIds = blogTagAssociationMapper.selectList(
                new LambdaQueryWrapper<BlogTagAssociation>().eq(BlogTagAssociation::getBlogId, blogId)
        ).stream().map(BlogTagAssociation::getTagId).toList();

        if (tagIds.isEmpty()) {
            return List.of();
        }
        return blogTagMapper.selectByIds(tagIds).stream()
                .map(tag -> new BlogTagVO(tag.getId(), tag.getName(), tag.getDesc()))
                .toList();
    }

    private List<BlogPictureVO> selectBlogPictures(Long blogId) {
        return blogPictureMapper.selectList(new LambdaQueryWrapper<BlogPicture>()
                        .eq(BlogPicture::getBlogId, blogId)
                        .eq(BlogPicture::getDeleted, 0)
                        .orderByAsc(BlogPicture::getCreateTime))
                .stream()
                .map(this::convertPictureToVO)
                .toList();
    }

    private Boolean isLiked(Long blogId, Long userId) {
        if (userId == null) {
            return false;
        }
        Long count = blogLikeMapper.selectCount(new LambdaQueryWrapper<BlogLike>()
                .eq(BlogLike::getBlogId, blogId)
                .eq(BlogLike::getUserId, userId)
                .eq(BlogLike::getDeleted, 0));
        return count > 0;
    }

    private BlogPictureVO convertPictureToVO(BlogPicture picture) {
        BlogPictureVO vo = new BlogPictureVO();
        BeanUtils.copyProperties(picture, vo);
        vo.setUrl(blogImageUrlResolver.buildPublicUrl(picture.getObjectName()));
        return vo;
    }

    private CommentVO convertCommentToVO(BlogComment bc, Map<Long, UserVO> usersById) {
        if (bc == null) return null;
        CommentVO commentVO = new CommentVO();
        BeanUtils.copyProperties(bc, commentVO);
        commentVO.setUsername(usernameOf(bc.getUserId(), usersById));
        commentVO.setNickname(nicknameOf(bc.getUserId(), usersById));
        return commentVO;
    }
}

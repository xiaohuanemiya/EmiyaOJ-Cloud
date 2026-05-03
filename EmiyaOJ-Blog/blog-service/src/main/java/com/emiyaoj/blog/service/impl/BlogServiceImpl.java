package com.emiyaoj.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.emiyaoj.blog.domain.*;
import com.emiyaoj.blog.dto.*;
import com.emiyaoj.blog.mapper.*;
import com.emiyaoj.blog.service.IBlogService;
import com.emiyaoj.blog.vo.BlogPictureVO;
import com.emiyaoj.blog.vo.BlogTagVO;
import com.emiyaoj.blog.vo.BlogVO;
import com.emiyaoj.blog.vo.CommentVO;
import com.emiyaoj.common.domain.PageDTO;
import com.emiyaoj.common.domain.PageVO;
import com.emiyaoj.common.domain.ResponseResult;
import com.emiyaoj.common.exception.BaseException;
import com.emiyaoj.common.utils.RedisUtil;
import com.emiyaoj.problem.api.ProblemFeignClient;
import com.emiyaoj.problem.dto.ProblemVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    private final BlogTagMapper blogTagMapper;
    private final BlogTagAssociationMapper blogTagAssociationMapper;
    private final BlogCommentMapper blogCommentMapper;
    private final BlogPictureMapper blogPictureMapper;
    private final BlogLikeMapper blogLikeMapper;
    private final UserBlogMapper userBlogMapper;
    private final ProblemFeignClient problemFeignClient;
    private final RedisUtil redisUtil;

    @Override
    public List<BlogVO> selectAll() {
        return list(new LambdaQueryWrapper<Blog>().eq(Blog::getDeleted, 0))
                .stream()
                .map(blog -> convertBlogToVO(blog, null, false))
                .toList();
    }

    @Override
    public PageVO<BlogVO> select(BlogQueryDTO queryDTO) {
        return select(queryDTO, null);
    }

    @Override
    public PageVO<BlogVO> select(BlogQueryDTO queryDTO, Long userId) {
        Page<Blog> page = new Page<>(queryDTO.getPageNo(), queryDTO.getPageSize());
        LambdaQueryWrapper<Blog> wrapper = buildBlogQueryWrapper(queryDTO);
        this.page(page, wrapper);
        return PageVO.of(page, blog -> convertBlogToVO(blog, userId, false));
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
                .eq(Blog::getDeleted, 0)
                .last("LIMIT 1"));
        if (existing != null) {
            existing.setTitle(saveDTO.getTitle());
            existing.setContent(saveDTO.getContent());
            existing.setUpdateTime(LocalDateTime.now());
            existing.setDeleted(0);
            if (!updateById(existing)) {
                return false;
            }
            replaceTags(existing.getId(), saveDTO.getTagIds());
            bindPictures(existing.getId(), saveDTO.getPictureIds(), userId);
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
        this.page(page, buildBlogQueryWrapper(queryDTO));
        return PageVO.of(page, blog -> convertBlogToVO(blog, userId, false));
    }

    @Override
    public BlogVO selectBlogById(Long blogId) {
        return selectBlogById(blogId, null);
    }

    @Override
    public BlogVO selectBlogById(Long blogId, Long userId) {
        Blog blog = getById(blogId);
        if (blog == null || Integer.valueOf(1).equals(blog.getDeleted())) {
            return null;
        }
        increaseViewCount(blog, userId);
        return convertBlogToVO(getById(blogId), userId, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteBlogById(Long blogId) {
        blogTagAssociationMapper.delete(
                new LambdaQueryWrapper<BlogTagAssociation>().eq(BlogTagAssociation::getBlogId, blogId));
        blogPictureMapper.update(new LambdaUpdateWrapper<BlogPicture>()
                .eq(BlogPicture::getBlogId, blogId)
                .set(BlogPicture::getBlogId, null));
        return this.updateById(new Blog().setId(blogId).setUpdateTime(LocalDateTime.now()).setDeleted(1));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean editBlog(BlogEditDTO editDTO, Long userId) {
        Blog existing = getById(editDTO.getId());
        if (existing == null || Integer.valueOf(1).equals(existing.getDeleted())) {
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
        boolean updated = this.updateById(blog);
        if (updated && editDTO.getPictureIds() != null) {
            bindPictures(editDTO.getId(), editDTO.getPictureIds(), userId);
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
        blogLikeMapper.insert(new BlogLike(null, userId, blogId, LocalDateTime.now(), 0));
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
        Page<BlogComment> page = pageDTO.toMpPageDefaultSortByCreateTimeDesc();

        LambdaQueryWrapper<BlogComment> wrapper = new LambdaQueryWrapper<BlogComment>()
                .eq(BlogComment::getBlogId, blogId)
                .eq(BlogComment::getDeleted, 0);
        blogCommentMapper.selectPage(page, wrapper);

        return PageVO.of(page, this::convertCommentToVO);
    }

    @Override
    public CommentVO selectCommentById(Long commentId) {
        return convertCommentToVO(blogCommentMapper.selectById(commentId));
    }

    @Override
    public List<CommentVO> selectComment(CommentQueryDTO queryDTO) {
        LambdaQueryWrapper<BlogComment> wrapper = new LambdaQueryWrapper<BlogComment>()
                .eq(BlogComment::getDeleted, 0)
                .eq(queryDTO.getBlogId() != null, BlogComment::getBlogId, queryDTO.getBlogId())
                .ge(queryDTO.getFromDay() != null, BlogComment::getCreateTime, queryDTO.getFromDay())
                .le(queryDTO.getToDay() != null, BlogComment::getCreateTime, queryDTO.getToDay());
        return blogCommentMapper.selectList(wrapper).stream()
                .map(this::convertCommentToVO)
                .toList();
    }

    @Override
    public boolean saveComment(Long blogId, BlogCommentSaveDTO saveDTO, Long userId) {
        requireActiveBlog(blogId);
        BlogComment comment = new BlogComment(null, blogId, userId,
                saveDTO.getContent(), LocalDateTime.now(), LocalDateTime.now(), 0);
        return blogCommentMapper.insert(comment) == 1;
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

    private LambdaQueryWrapper<Blog> buildBlogQueryWrapper(BlogQueryDTO queryDTO) {
        LambdaQueryWrapper<Blog> wrapper = new LambdaQueryWrapper<Blog>()
                .eq(Blog::getDeleted, 0)
                .like(!ObjectUtils.isEmpty(queryDTO.getTitle()), Blog::getTitle, queryDTO.getTitle())
                .eq(queryDTO.getBlogType() != null, Blog::getBlogType, queryDTO.getBlogType())
                .eq(queryDTO.getProblemId() != null, Blog::getProblemId, queryDTO.getProblemId());

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
        blog.setCreateTime(LocalDateTime.now());
        blog.setUpdateTime(LocalDateTime.now());
        blog.setDeleted(0);
        if (!this.save(blog)) {
            throw new BaseException("添加失败");
        }
        replaceTags(blog.getId(), saveDTO.getTagIds());
        bindPictures(blog.getId(), saveDTO.getPictureIds(), userId);
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

    private BlogVO convertBlogToVO(Blog blog, Long userId, boolean includeProblem) {
        if (blog == null) return null;
        BlogVO blogVO = new BlogVO();
        BeanUtils.copyProperties(blog, blogVO);
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
        return vo;
    }

    private CommentVO convertCommentToVO(BlogComment bc) {
        if (bc == null) return null;
        CommentVO commentVO = new CommentVO();
        BeanUtils.copyProperties(bc, commentVO);

        Long userId = bc.getUserId();
        UserBlog ub = userBlogMapper.selectById(userId);
        if (ub != null) {
            commentVO.setUsername(ub.getUsername());
            commentVO.setNickname(ub.getNickname());
        } else {
            commentVO.setUsername("");
            commentVO.setNickname("");
        }

        return commentVO;
    }
}

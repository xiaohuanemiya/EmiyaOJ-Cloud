package com.emiyaoj.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.emiyaoj.blog.domain.*;
import com.emiyaoj.blog.dto.*;
import com.emiyaoj.blog.mapper.*;
import com.emiyaoj.blog.service.IBlogService;
import com.emiyaoj.blog.vo.BlogTagVO;
import com.emiyaoj.blog.vo.BlogVO;
import com.emiyaoj.blog.vo.CommentVO;
import com.emiyaoj.common.domain.PageDTO;
import com.emiyaoj.common.domain.PageVO;
import com.emiyaoj.common.exception.BaseException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 博客服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

    private final BlogTagMapper blogTagMapper;
    private final BlogTagAssociationMapper blogTagAssociationMapper;
    private final BlogCommentMapper blogCommentMapper;
    private final BlogPictureMapper blogPictureMapper;
    private final UserBlogMapper userBlogMapper;

    @Override
    public List<BlogVO> selectAll() {
        return list(new LambdaQueryWrapper<Blog>().eq(Blog::getDeleted, 0))
                .stream()
                .map(this::convertBlogToVO)
                .toList();
    }

    @Override
    public PageVO<BlogVO> select(BlogQueryDTO queryDTO) {
        Page<Blog> page = new Page<>(queryDTO.getPageNo(), queryDTO.getPageSize());
        LambdaQueryWrapper<Blog> wrapper = new LambdaQueryWrapper<Blog>()
                .eq(Blog::getDeleted, 0)
                .like(!ObjectUtils.isEmpty(queryDTO.getTitle()), Blog::getTitle, queryDTO.getTitle())
                .orderByDesc(Blog::getCreateTime);

        Optional.ofNullable(queryDTO.getCreateTime()).ifPresent(t -> {
            LocalDateTime startOfDay = t.toLocalDate().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);
            wrapper.between(Blog::getCreateTime, startOfDay, endOfDay);
        });

        this.page(page, wrapper);
        return PageVO.of(page, this::convertBlogToVO);
    }

    @Override
    public boolean saveBlog(BlogSaveDTO saveDTO, Long userId) {
        Blog blog = new Blog(null, userId, saveDTO.getTitle(), saveDTO.getContent(),
                LocalDateTime.now(), LocalDateTime.now(), 0);
        if (!this.save(blog)) {
            try {
                this.deleteBlogById(blog.getId());
            } catch (Exception ignored) {
            }
            return false;
        }

        // 标签为可选项，未传标签时直接返回成功
        List<Long> tagIds = saveDTO.getTagIds();
        if (tagIds == null || tagIds.isEmpty()) {
            return true;
        }

        // 检查标签id是否存在且合法
        List<BlogTag> tags = blogTagMapper.selectByIds(tagIds);
        if (tags.size() != tagIds.size()) {
            log.warn("标签id不合法");
            return false;
        }

        List<BlogTagAssociation> list = tagIds.stream()
                .map(tagId -> new BlogTagAssociation(null, blog.getId(), tagId))
                .toList();
        return !blogTagAssociationMapper.insert(list).isEmpty();
    }

    @Override
    public BlogVO selectBlogById(Long blogId) {
        return convertBlogToVO(this.getById(blogId));
    }

    @Override
    public boolean deleteBlogById(Long blogId) {
        blogTagAssociationMapper.delete(
                new LambdaQueryWrapper<BlogTagAssociation>().eq(BlogTagAssociation::getBlogId, blogId));
        return this.updateById(new Blog().setId(blogId).setUpdateTime(LocalDateTime.now()).setDeleted(1));
    }

    @Override
    public boolean editBlog(BlogEditDTO editDTO, Long userId) {
        Blog blog = new Blog();
        blog.setId(editDTO.getId());
        blog.setUserId(userId);
        blog.setTitle(editDTO.getTitle());
        blog.setContent(editDTO.getContent());
        blog.setUpdateTime(LocalDateTime.now());
        return this.updateById(blog);
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

    // ==================== 转换方法 ====================

    private BlogTagVO convertTagToVO(BlogTag tag) {
        if (tag == null) return null;
        return new BlogTagVO(tag.getId(), tag.getName(), tag.getDesc());
    }

    private BlogVO convertBlogToVO(Blog blog) {
        if (blog == null) return null;
        BlogVO blogVO = new BlogVO();
        BeanUtils.copyProperties(blog, blogVO);

        List<Long> tagIds = blogTagAssociationMapper.selectList(
                new LambdaQueryWrapper<BlogTagAssociation>().eq(BlogTagAssociation::getBlogId, blog.getId())
        ).stream().map(BlogTagAssociation::getTagId).toList();

        if (!tagIds.isEmpty()) {
            List<BlogTagVO> tags = blogTagMapper.selectByIds(tagIds).stream()
                    .map(tag -> new BlogTagVO(tag.getId(), tag.getName(), tag.getDesc()))
                    .toList();
            blogVO.setTags(tags);
        } else {
            blogVO.setTags(List.of());
        }

        return blogVO;
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

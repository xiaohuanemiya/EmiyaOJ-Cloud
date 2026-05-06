package com.emiyaoj.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.emiyaoj.blog.domain.Blog;
import com.emiyaoj.blog.domain.BlogStar;
import com.emiyaoj.blog.domain.UserBlog;
import com.emiyaoj.blog.config.BlogModerationProperties;
import com.emiyaoj.blog.dto.UserBlogBlogsQueryDTO;
import com.emiyaoj.blog.dto.UserBlogStarsQueryDTO;
import com.emiyaoj.blog.mapper.BlogMapper;
import com.emiyaoj.blog.mapper.BlogStarMapper;
import com.emiyaoj.blog.mapper.UserBlogMapper;
import com.emiyaoj.blog.service.IUserBlogService;
import com.emiyaoj.blog.vo.BlogVO;
import com.emiyaoj.blog.vo.UserBlogVO;
import com.emiyaoj.common.domain.PageVO;
import com.emiyaoj.moderation.dto.AuditStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户博客服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserBlogServiceImpl extends ServiceImpl<UserBlogMapper, UserBlog> implements IUserBlogService {

    private final BlogMapper blogMapper;
    private final BlogStarMapper blogStarMapper;

    @Override
    public UserBlogVO selectUserBlogById(Long id) {
        UserBlog userBlog = this.getById(id);
        if (userBlog == null) {
            // 尝试创建一个空的用户博客记录
            userBlog = new UserBlog(id);
            this.save(userBlog);
        }
        UserBlogVO userBlogVO = new UserBlogVO();
        BeanUtils.copyProperties(userBlog, userBlogVO);
        return userBlogVO;
    }

    @Override
    public PageVO<BlogVO> selectUserBlogBlogs(UserBlogBlogsQueryDTO queryDTO) {
        return selectUserBlogBlogs(queryDTO, null, null);
    }

    @Override
    public PageVO<BlogVO> selectUserBlogBlogs(UserBlogBlogsQueryDTO queryDTO, Long viewerId, String permissions) {
        Page<Blog> page = new Page<>(queryDTO.getPageNo(), queryDTO.getPageSize());
        LambdaQueryWrapper<Blog> wrapper = new LambdaQueryWrapper<Blog>()
                .eq(Blog::getUserId, queryDTO.getUserId())
                .eq(Blog::getDeleted, 0)
                .orderByDesc(Blog::getUpdateTime);
        applyAuditVisibility(wrapper, queryDTO, viewerId, permissions);
        blogMapper.selectPage(page, wrapper);
        return PageVO.of(page, this::convertBlogToVO);
    }

    @Override
    public PageVO<BlogVO> selectUserBlogStars(UserBlogStarsQueryDTO queryDTO) {
        Page<BlogStar> page = new Page<>(queryDTO.getPageNo(), queryDTO.getPageSize());
        blogStarMapper.selectPage(page, new LambdaQueryWrapper<BlogStar>()
                .eq(BlogStar::getUserId, queryDTO.getUserId())
                .eq(BlogStar::getDeleted, 0)
                .orderByDesc(BlogStar::getCreateTime));

        List<Long> blogIds = page.getRecords().stream().map(BlogStar::getBlogId).toList();
        if (blogIds.isEmpty()) {
            return PageVO.empty((long) queryDTO.getPageNo(), (long) queryDTO.getPageSize());
        }

        List<Blog> blogs = blogMapper.selectByIds(blogIds).stream()
                .filter(blog -> Integer.valueOf(0).equals(blog.getDeleted()))
                .filter(blog -> Integer.valueOf(AuditStatus.APPROVED.getCode()).equals(blog.getAuditStatus()))
                .toList();
        List<BlogVO> blogVOs = blogs.stream().map(this::convertBlogToVO).toList();

        return new PageVO<>(page.getTotal(), blogVOs, page.getCurrent(), page.getSize());
    }

    @Override
    public boolean starBlog(Long blogId, Long userId) {
        Blog blog = blogMapper.selectById(blogId);
        if (blog == null || blog.getDeleted() == 1
                || !Integer.valueOf(AuditStatus.APPROVED.getCode()).equals(blog.getAuditStatus())) return false;

        // 尝试恢复旧记录
        int update = blogStarMapper.update(new LambdaUpdateWrapper<BlogStar>()
                .eq(BlogStar::getUserId, userId)
                .eq(BlogStar::getBlogId, blogId)
                .set(BlogStar::getDeleted, 0));
        if (update == 1) return true;

        // 插入新记录
        BlogStar blogStar = new BlogStar(null, userId, blogId, LocalDateTime.now(), 0);
        return blogStarMapper.insert(blogStar) == 1;
    }

    @Override
    public boolean unstarBlog(Long blogId, Long userId) {
        int update = blogStarMapper.update(new LambdaUpdateWrapper<BlogStar>()
                .eq(BlogStar::getUserId, userId)
                .eq(BlogStar::getBlogId, blogId)
                .set(BlogStar::getDeleted, 1));
        return update == 1;
    }

    private BlogVO convertBlogToVO(Blog blog) {
        if (blog == null) return null;
        BlogVO blogVO = new BlogVO();
        BeanUtils.copyProperties(blog, blogVO);
        return blogVO;
    }

    private void applyAuditVisibility(LambdaQueryWrapper<Blog> wrapper,
                                      UserBlogBlogsQueryDTO queryDTO,
                                      Long viewerId,
                                      String permissions) {
        boolean manager = isModerationManager(permissions);
        boolean owner = viewerId != null && viewerId.equals(queryDTO.getUserId());
        if (manager) {
            wrapper.eq(queryDTO.getAuditStatus() != null, Blog::getAuditStatus, queryDTO.getAuditStatus());
            wrapper.eq(queryDTO.getAuditStatus() == null, Blog::getAuditStatus, AuditStatus.APPROVED.getCode());
        } else if (owner && queryDTO.getAuditStatus() != null) {
            wrapper.eq(Blog::getAuditStatus, queryDTO.getAuditStatus());
        } else {
            wrapper.eq(Blog::getAuditStatus, AuditStatus.APPROVED.getCode());
        }
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
}

package com.emiyaoj.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.emiyaoj.auth.api.AuthUserFeignClient;
import com.emiyaoj.auth.vo.UserVO;
import com.emiyaoj.blog.config.BlogModerationProperties;
import com.emiyaoj.blog.domain.Blog;
import com.emiyaoj.blog.domain.BlogStar;
import com.emiyaoj.blog.domain.UserBlog;
import com.emiyaoj.blog.dto.UserBlogBlogsQueryDTO;
import com.emiyaoj.blog.dto.UserBlogStarsQueryDTO;
import com.emiyaoj.blog.mapper.BlogMapper;
import com.emiyaoj.blog.mapper.BlogStarMapper;
import com.emiyaoj.blog.mapper.UserBlogMapper;
import com.emiyaoj.blog.service.IUserBlogService;
import com.emiyaoj.blog.vo.BlogVO;
import com.emiyaoj.blog.vo.UserBlogVO;
import com.emiyaoj.common.domain.PageVO;
import com.emiyaoj.common.domain.ResponseResult;
import com.emiyaoj.moderation.dto.AuditStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserBlogServiceImpl extends ServiceImpl<UserBlogMapper, UserBlog> implements IUserBlogService {

    private final BlogMapper blogMapper;
    private final BlogStarMapper blogStarMapper;
    private final AuthUserFeignClient authUserFeignClient;

    @Override
    public UserBlogVO selectUserBlogById(Long id) {
        UserBlogVO userBlogVO = new UserBlogVO();
        userBlogVO.setUserId(id);

        UserVO user = loadUserById(id);
        if (user != null) {
            userBlogVO.setUsername(textOrEmpty(user.getUsername()));
            userBlogVO.setNickname(textOrEmpty(user.getNickname()));
        } else {
            userBlogVO.setUsername("");
            userBlogVO.setNickname("");
        }

        userBlogVO.setBlogCount(Math.toIntExact(blogMapper.selectCount(new LambdaQueryWrapper<Blog>()
                .eq(Blog::getUserId, id)
                .eq(Blog::getDeleted, 0)
                .eq(Blog::getAuditStatus, AuditStatus.APPROVED.getCode()))));
        userBlogVO.setStarCount(Math.toIntExact(blogStarMapper.selectCount(new LambdaQueryWrapper<BlogStar>()
                .eq(BlogStar::getUserId, id)
                .eq(BlogStar::getDeleted, 0))));
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
        Map<Long, UserVO> usersById = loadUsersByIds(page.getRecords().stream().map(Blog::getUserId).toList());
        return PageVO.of(page, blog -> convertBlogToVO(blog, usersById));
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
        Map<Long, UserVO> usersById = loadUsersByIds(blogs.stream().map(Blog::getUserId).toList());
        List<BlogVO> blogVOs = blogs.stream().map(blog -> convertBlogToVO(blog, usersById)).toList();

        return new PageVO<>(page.getTotal(), blogVOs, page.getCurrent(), page.getSize());
    }

    @Override
    public boolean starBlog(Long blogId, Long userId) {
        Blog blog = blogMapper.selectById(blogId);
        if (blog == null || blog.getDeleted() == 1
                || !Integer.valueOf(AuditStatus.APPROVED.getCode()).equals(blog.getAuditStatus())) return false;

        int update = blogStarMapper.update(new LambdaUpdateWrapper<BlogStar>()
                .eq(BlogStar::getUserId, userId)
                .eq(BlogStar::getBlogId, blogId)
                .set(BlogStar::getDeleted, 0));
        if (update == 1) return true;

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

    private UserVO loadUserById(Long userId) {
        Map<Long, UserVO> usersById = loadUsersByIds(userId == null ? List.of() : List.of(userId));
        return usersById.get(userId);
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

    private String textOrEmpty(String text) {
        return org.springframework.util.StringUtils.hasText(text) ? text : "";
    }

    private BlogVO convertBlogToVO(Blog blog, Map<Long, UserVO> usersById) {
        if (blog == null) return null;
        BlogVO blogVO = new BlogVO();
        BeanUtils.copyProperties(blog, blogVO);
        UserVO author = usersById.get(blog.getUserId());
        blogVO.setAuthorNickname(author == null ? "" : textOrEmpty(author.getNickname()));
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

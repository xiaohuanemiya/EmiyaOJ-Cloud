package com.emiyaoj.blog.service;

import com.emiyaoj.blog.dto.UserBlogBlogsQueryDTO;
import com.emiyaoj.blog.dto.UserBlogStarsQueryDTO;
import com.emiyaoj.blog.vo.BlogVO;
import com.emiyaoj.blog.vo.UserBlogVO;
import com.emiyaoj.common.domain.PageVO;

/**
 * 用户博客服务接口
 */
public interface IUserBlogService {

    UserBlogVO selectUserBlogById(Long id);

    PageVO<BlogVO> selectUserBlogBlogs(UserBlogBlogsQueryDTO queryDTO);

    PageVO<BlogVO> selectUserBlogStars(UserBlogStarsQueryDTO queryDTO);

    boolean starBlog(Long blogId, Long userId);

    boolean unstarBlog(Long blogId, Long userId);
}

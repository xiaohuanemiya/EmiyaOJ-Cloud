package com.emiyaoj.blog.service;

import com.emiyaoj.blog.dto.*;
import com.emiyaoj.blog.vo.BlogTagVO;
import com.emiyaoj.blog.vo.BlogVO;
import com.emiyaoj.blog.vo.CommentVO;
import com.emiyaoj.common.domain.PageDTO;
import com.emiyaoj.common.domain.PageVO;

import java.util.List;

/**
 * 博客服务接口
 */
public interface IBlogService {

    List<BlogVO> selectAll();

    PageVO<BlogVO> select(BlogQueryDTO queryDTO);

    boolean saveBlog(BlogSaveDTO saveDTO, Long userId);

    BlogVO selectBlogById(Long blogId);

    boolean deleteBlogById(Long blogId);

    boolean editBlog(BlogEditDTO editDTO, Long userId);

    List<BlogTagVO> selectAllTags();

    BlogTagVO selectTagById(Long tagId);

    BlogTagVO saveTag(BlogTagSaveDTO saveDTO);

    BlogTagVO updateTag(BlogTagSaveDTO saveDTO);

    boolean deleteTagById(Long tagId);

    PageVO<CommentVO> selectCommentPage(Long blogId, PageDTO pageDTO);

    CommentVO selectCommentById(Long commentId);

    List<CommentVO> selectComment(CommentQueryDTO queryDTO);

    boolean saveComment(Long blogId, BlogCommentSaveDTO saveDTO, Long userId);

    int deleteComment(Long commentId);
}

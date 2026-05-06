package com.emiyaoj.blog.service;

import com.emiyaoj.blog.dto.*;
import com.emiyaoj.blog.vo.BlogTagVO;
import com.emiyaoj.blog.vo.BlogVO;
import com.emiyaoj.blog.vo.CommentVO;
import com.emiyaoj.common.domain.PageDTO;
import com.emiyaoj.common.domain.PageVO;
import com.emiyaoj.moderation.dto.ModerationResultDTO;

import java.util.List;

/**
 * 博客服务接口
 */
public interface IBlogService {

    List<BlogVO> selectAll();

    PageVO<BlogVO> select(BlogQueryDTO queryDTO);

    PageVO<BlogVO> select(BlogQueryDTO queryDTO, Long userId);

    PageVO<BlogVO> select(BlogQueryDTO queryDTO, Long userId, String permissions);

    boolean saveBlog(BlogSaveDTO saveDTO, Long userId);

    boolean saveSolution(Long problemId, BlogSaveDTO saveDTO, Long userId);

    PageVO<BlogVO> selectProblemSolutions(Long problemId, BlogQueryDTO queryDTO, Long userId);

    BlogVO selectBlogById(Long blogId);

    BlogVO selectBlogById(Long blogId, Long userId);

    BlogVO selectBlogById(Long blogId, Long userId, String permissions);

    boolean deleteBlogById(Long blogId);

    boolean editBlog(BlogEditDTO editDTO, Long userId);

    boolean likeBlog(Long blogId, Long userId);

    boolean unlikeBlog(Long blogId, Long userId);

    List<BlogTagVO> selectAllTags();

    BlogTagVO selectTagById(Long tagId);

    BlogTagVO saveTag(BlogTagSaveDTO saveDTO);

    BlogTagVO updateTag(BlogTagSaveDTO saveDTO);

    boolean deleteTagById(Long tagId);

    PageVO<CommentVO> selectCommentPage(Long blogId, PageDTO pageDTO);

    PageVO<CommentVO> selectCommentPage(Long blogId, PageDTO pageDTO, Long userId, String permissions);

    CommentVO selectCommentById(Long commentId);

    CommentVO selectCommentById(Long commentId, Long userId, String permissions);

    List<CommentVO> selectComment(CommentQueryDTO queryDTO);

    List<CommentVO> selectComment(CommentQueryDTO queryDTO, Long userId, String permissions);

    boolean saveComment(Long blogId, BlogCommentSaveDTO saveDTO, Long userId);

    int deleteComment(Long commentId);

    boolean applyModerationResult(ModerationResultDTO resultDTO);

    boolean updateBlogAuditStatus(Long blogId, Integer auditStatus, String reason, String permissions);

    boolean updateCommentAuditStatus(Long commentId, Integer auditStatus, String reason, String permissions);
}

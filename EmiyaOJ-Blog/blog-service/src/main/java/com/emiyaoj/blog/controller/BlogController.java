package com.emiyaoj.blog.controller;

import com.emiyaoj.blog.dto.*;
import com.emiyaoj.blog.service.IBlogService;
import com.emiyaoj.blog.service.IUserBlogService;
import com.emiyaoj.blog.vo.*;
import com.emiyaoj.common.domain.PageDTO;
import com.emiyaoj.common.domain.PageVO;
import com.emiyaoj.common.domain.ResponseResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 博客管理（后台管理员接口）
 */
@Tag(name = "博客管理")
@RestController
@RequestMapping("/blog")
@RequiredArgsConstructor
public class BlogController {

    private final IUserBlogService userBlogService;
    private final IBlogService blogService;

    /**
     * 查所有博客
     */
    @GetMapping("")
    public ResponseResult<List<BlogVO>> blogs() {
        return ResponseResult.success(blogService.selectAll());
    }

    /**
     * 发布博客
     */
    @PostMapping("")
    public ResponseResult<?> addBlog(@RequestBody BlogSaveDTO blogSaveDTO,
                                     @RequestHeader("X-User-Id") Long userId) {
        boolean success = blogService.saveBlog(blogSaveDTO, userId);
        return success ? ResponseResult.success() : ResponseResult.fail("添加失败");
    }

    /**
     * 分页条件查博客
     */
    @PostMapping("/query")
    public ResponseResult<PageVO<BlogVO>> queryBlog(@RequestBody BlogQueryDTO blogQueryDTO) {
        return ResponseResult.success(blogService.select(blogQueryDTO));
    }

    /**
     * 获取指定博客信息
     */
    @GetMapping("/{bid}")
    public ResponseResult<BlogVO> getBlog(@PathVariable Long bid) {
        BlogVO vo = blogService.selectBlogById(bid);
        return vo != null ? ResponseResult.success(vo) : ResponseResult.fail(404, "未找到该博客");
    }

    /**
     * 删博客（逻辑删）
     */
    @DeleteMapping("/{bid}")
    public ResponseResult<?> deleteBlog(@PathVariable Long bid) {
        boolean success = blogService.deleteBlogById(bid);
        return success ? ResponseResult.success() : ResponseResult.fail("删除失败");
    }

    /**
     * 改博客
     */
    @PutMapping("/{bid}")
    public ResponseResult<?> editBlog(@PathVariable Long bid,
                                      @RequestBody BlogEditDTO blogEditDTO,
                                      @RequestHeader("X-User-Id") Long userId) {
        blogEditDTO.setId(bid);
        if (blogEditDTO.getTitle() != null && blogEditDTO.getTitle().isBlank()) blogEditDTO.setTitle(null);
        if (blogEditDTO.getContent() != null && blogEditDTO.getContent().isBlank()) blogEditDTO.setContent(null);
        boolean success = blogService.editBlog(blogEditDTO, userId);
        return success ? ResponseResult.success() : ResponseResult.fail("修改失败");
    }

    /**
     * 查评论（分页）
     */
    @PostMapping("/{bid}/comments/query")
    public ResponseResult<PageVO<CommentVO>> selectCommentPage(@PathVariable Long bid,
                                                               @RequestBody PageDTO pageDTO) {
        return ResponseResult.success(blogService.selectCommentPage(bid, pageDTO));
    }

    /**
     * 发表评论
     */
    @PostMapping("/{bid}/comments")
    public ResponseResult<?> addComment(@PathVariable Long bid,
                                        @RequestBody BlogCommentSaveDTO saveDTO,
                                        @RequestHeader("X-User-Id") Long userId) {
        boolean success = blogService.saveComment(bid, saveDTO, userId);
        return success ? ResponseResult.success() : ResponseResult.fail("添加失败");
    }

    /**
     * 用户收藏博客
     */
    @PostMapping("/{bid}/star")
    public ResponseResult<?> starBlog(@PathVariable Long bid,
                                      @RequestHeader("X-User-Id") Long userId) {
        boolean success = userBlogService.starBlog(bid, userId);
        return success ? ResponseResult.success() : ResponseResult.fail("收藏失败");
    }

    /**
     * 用户取消收藏博客
     */
    @DeleteMapping("/{bid}/star")
    public ResponseResult<?> unstarBlog(@PathVariable Long bid,
                                        @RequestHeader("X-User-Id") Long userId) {
        boolean success = userBlogService.unstarBlog(bid, userId);
        return success ? ResponseResult.success() : ResponseResult.fail("取消失败");
    }

    /**
     * 查博客模块用户信息
     */
    @GetMapping("/user/{uid}")
    public ResponseResult<UserBlogVO> userBlog(@PathVariable Long uid) {
        UserBlogVO vo = userBlogService.selectUserBlogById(uid);
        return vo != null ? ResponseResult.success(vo) : ResponseResult.fail(404, "未找到该用户");
    }

    /**
     * 分页条件查用户发表的博客
     */
    @PostMapping("/user/{uid}/blogs/query")
    public ResponseResult<PageVO<BlogVO>> userBlogBlogs(@PathVariable Long uid,
                                                        @RequestBody UserBlogBlogsQueryDTO blogsQueryDTO) {
        blogsQueryDTO.setUserId(uid);
        return ResponseResult.success(userBlogService.selectUserBlogBlogs(blogsQueryDTO));
    }

    /**
     * 分页条件查用户收藏的博客
     */
    @PostMapping("/user/{uid}/stars/query")
    public ResponseResult<PageVO<BlogVO>> userBlogStars(@PathVariable Long uid,
                                                        @RequestBody UserBlogStarsQueryDTO starsQueryDTO) {
        starsQueryDTO.setUserId(uid);
        return ResponseResult.success(userBlogService.selectUserBlogStars(starsQueryDTO));
    }

    /**
     * 查所有标签
     */
    @GetMapping("/tags")
    public ResponseResult<List<BlogTagVO>> tags() {
        return ResponseResult.success(blogService.selectAllTags());
    }

    /**
     * 查评论（条件查询）
     */
    @PostMapping("/comments/query")
    public ResponseResult<List<CommentVO>> queryComments(@RequestBody CommentQueryDTO queryDTO) {
        return ResponseResult.success(blogService.selectComment(queryDTO));
    }

    /**
     * 获取指定评论
     */
    @GetMapping("/comments/{cid}")
    public ResponseResult<CommentVO> getComment(@PathVariable Long cid) {
        CommentVO vo = blogService.selectCommentById(cid);
        return vo != null ? ResponseResult.success(vo) : ResponseResult.fail(404, "未找到该评论");
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/comments/{cid}")
    public ResponseResult<?> deleteComment(@PathVariable Long cid) {
        int code = blogService.deleteComment(cid);
        return switch (code) {
            case HttpServletResponse.SC_OK -> ResponseResult.success();
            case HttpServletResponse.SC_NOT_FOUND -> ResponseResult.fail(404, "未找到该评论");
            default -> ResponseResult.fail(500, "服务器错误");
        };
    }
}

package com.emiyaoj.blog.controller;

import com.emiyaoj.blog.dto.*;
import com.emiyaoj.blog.service.IBlogService;
import com.emiyaoj.blog.service.IUserBlogService;
import com.emiyaoj.blog.vo.*;
import com.emiyaoj.common.domain.PageDTO;
import com.emiyaoj.common.domain.PageVO;
import com.emiyaoj.common.domain.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

    @Operation(summary = "查询所有博客")
    @GetMapping("")
    public ResponseResult<List<BlogVO>> blogs() {
        return ResponseResult.success(blogService.selectAll());
    }

    @Operation(summary = "发布博客")
    @PostMapping("")
    public ResponseResult<?> addBlog(@RequestBody BlogSaveDTO blogSaveDTO,
                                     @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        boolean success = blogService.saveBlog(blogSaveDTO, userId);
        return success ? ResponseResult.success() : ResponseResult.fail("添加失败");
    }

    @Operation(summary = "分页条件查询博客")
    @PostMapping("/query")
    public ResponseResult<PageVO<BlogVO>> queryBlog(@RequestBody BlogQueryDTO blogQueryDTO) {
        return ResponseResult.success(blogService.select(blogQueryDTO));
    }

    @Operation(summary = "获取指定博客信息")
    @GetMapping("/{bid}")
    public ResponseResult<BlogVO> getBlog(@Parameter(description = "博客ID") @PathVariable Long bid) {
        BlogVO vo = blogService.selectBlogById(bid);
        return vo != null ? ResponseResult.success(vo) : ResponseResult.fail(404, "未找到该博客");
    }

    @Operation(summary = "删除博客", description = "逻辑删除")
    @DeleteMapping("/{bid}")
    public ResponseResult<?> deleteBlog(@Parameter(description = "博客ID") @PathVariable Long bid) {
        boolean success = blogService.deleteBlogById(bid);
        return success ? ResponseResult.success() : ResponseResult.fail("删除失败");
    }

    @Operation(summary = "修改博客")
    @PutMapping("/{bid}")
    public ResponseResult<?> editBlog(@Parameter(description = "博客ID") @PathVariable Long bid,
                                      @RequestBody BlogEditDTO blogEditDTO,
                                      @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        blogEditDTO.setId(bid);
        if (blogEditDTO.getTitle() != null && blogEditDTO.getTitle().isBlank()) blogEditDTO.setTitle(null);
        if (blogEditDTO.getContent() != null && blogEditDTO.getContent().isBlank()) blogEditDTO.setContent(null);
        boolean success = blogService.editBlog(blogEditDTO, userId);
        return success ? ResponseResult.success() : ResponseResult.fail("修改失败");
    }

    @Operation(summary = "分页查询博客评论")
    @PostMapping("/{bid}/comments/query")
    public ResponseResult<PageVO<CommentVO>> selectCommentPage(@Parameter(description = "博客ID") @PathVariable Long bid,
                                                               @RequestBody PageDTO pageDTO) {
        return ResponseResult.success(blogService.selectCommentPage(bid, pageDTO));
    }

    @Operation(summary = "发表评论")
    @PostMapping("/{bid}/comments")
    public ResponseResult<?> addComment(@Parameter(description = "博客ID") @PathVariable Long bid,
                                        @RequestBody BlogCommentSaveDTO saveDTO,
                                        @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        boolean success = blogService.saveComment(bid, saveDTO, userId);
        return success ? ResponseResult.success() : ResponseResult.fail("添加失败");
    }

    @Operation(summary = "收藏博客")
    @PostMapping("/{bid}/star")
    public ResponseResult<?> starBlog(@Parameter(description = "博客ID") @PathVariable Long bid,
                                      @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        boolean success = userBlogService.starBlog(bid, userId);
        return success ? ResponseResult.success() : ResponseResult.fail("收藏失败");
    }

    @Operation(summary = "取消收藏博客")
    @DeleteMapping("/{bid}/star")
    public ResponseResult<?> unstarBlog(@Parameter(description = "博客ID") @PathVariable Long bid,
                                        @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        boolean success = userBlogService.unstarBlog(bid, userId);
        return success ? ResponseResult.success() : ResponseResult.fail("取消失败");
    }

    @Operation(summary = "查询博客模块用户信息")
    @GetMapping("/user/{uid}")
    public ResponseResult<UserBlogVO> userBlog(@Parameter(description = "用户ID") @PathVariable Long uid) {
        UserBlogVO vo = userBlogService.selectUserBlogById(uid);
        return vo != null ? ResponseResult.success(vo) : ResponseResult.fail(404, "未找到该用户");
    }

    @Operation(summary = "分页查询用户发表的博客")
    @PostMapping("/user/{uid}/blogs/query")
    public ResponseResult<PageVO<BlogVO>> userBlogBlogs(@Parameter(description = "用户ID") @PathVariable Long uid,
                                                        @RequestBody UserBlogBlogsQueryDTO blogsQueryDTO) {
        blogsQueryDTO.setUserId(uid);
        return ResponseResult.success(userBlogService.selectUserBlogBlogs(blogsQueryDTO));
    }

    @Operation(summary = "分页查询用户收藏的博客")
    @PostMapping("/user/{uid}/stars/query")
    public ResponseResult<PageVO<BlogVO>> userBlogStars(@Parameter(description = "用户ID") @PathVariable Long uid,
                                                        @RequestBody UserBlogStarsQueryDTO starsQueryDTO) {
        starsQueryDTO.setUserId(uid);
        return ResponseResult.success(userBlogService.selectUserBlogStars(starsQueryDTO));
    }

    @Operation(summary = "查询所有标签")
    @GetMapping("/tags")
    public ResponseResult<List<BlogTagVO>> tags() {
        return ResponseResult.success(blogService.selectAllTags());
    }

    @Operation(summary = "条件查询评论")
    @PostMapping("/comments/query")
    public ResponseResult<List<CommentVO>> queryComments(@RequestBody CommentQueryDTO queryDTO) {
        return ResponseResult.success(blogService.selectComment(queryDTO));
    }

    @Operation(summary = "获取指定评论")
    @GetMapping("/comments/{cid}")
    public ResponseResult<CommentVO> getComment(@Parameter(description = "评论ID") @PathVariable Long cid) {
        CommentVO vo = blogService.selectCommentById(cid);
        return vo != null ? ResponseResult.success(vo) : ResponseResult.fail(404, "未找到该评论");
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/comments/{cid}")
    public ResponseResult<?> deleteComment(@Parameter(description = "评论ID") @PathVariable Long cid) {
        int code = blogService.deleteComment(cid);
        return switch (code) {
            case HttpServletResponse.SC_OK -> ResponseResult.success();
            case HttpServletResponse.SC_NOT_FOUND -> ResponseResult.fail(404, "未找到该评论");
            default -> ResponseResult.fail(500, "服务器错误");
        };
    }
}

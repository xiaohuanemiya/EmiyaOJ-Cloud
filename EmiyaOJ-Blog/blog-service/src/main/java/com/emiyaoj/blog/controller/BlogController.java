package com.emiyaoj.blog.controller;

import com.emiyaoj.blog.dto.*;
import com.emiyaoj.blog.service.IBlogImageService;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Blog")
@RestController
@RequestMapping("/blog")
@RequiredArgsConstructor
public class BlogController {

    private final IUserBlogService userBlogService;
    private final IBlogService blogService;
    private final IBlogImageService blogImageService;

    @Operation(summary = "List all blogs")
    @GetMapping("")
    public ResponseResult<List<BlogVO>> blogs() {
        return ResponseResult.success(blogService.selectAll());
    }

    @Operation(summary = "Publish a blog")
    @PostMapping("")
    public ResponseResult<?> addBlog(@RequestBody BlogSaveDTO blogSaveDTO,
                                     @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        boolean success = blogService.saveBlog(blogSaveDTO, userId);
        return success ? ResponseResult.success() : ResponseResult.fail("添加失败");
    }

    @Operation(summary = "Query blogs")
    @PostMapping("/query")
    public ResponseResult<PageVO<BlogVO>> queryBlog(@RequestBody BlogQueryDTO blogQueryDTO,
                                                    @Parameter(hidden = true) @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ResponseResult.success(blogService.select(blogQueryDTO, userId));
    }

    @Operation(summary = "Get blog detail")
    @GetMapping("/{bid}")
    public ResponseResult<BlogVO> getBlog(@Parameter(description = "Blog ID") @PathVariable Long bid,
                                          @Parameter(hidden = true) @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        BlogVO vo = blogService.selectBlogById(bid, userId);
        return vo != null ? ResponseResult.success(vo) : ResponseResult.fail(404, "未找到该博客");
    }

    @Operation(summary = "Create a problem solution")
    @PostMapping("/problems/{problemId}/solutions")
    public ResponseResult<?> addSolution(@Parameter(description = "Problem ID") @PathVariable Long problemId,
                                         @RequestBody BlogSaveDTO blogSaveDTO,
                                         @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        boolean success = blogService.saveSolution(problemId, blogSaveDTO, userId);
        return success ? ResponseResult.success() : ResponseResult.fail("添加失败");
    }

    @Operation(summary = "Query problem solutions")
    @PostMapping("/problems/{problemId}/solutions/query")
    public ResponseResult<PageVO<BlogVO>> querySolutions(@Parameter(description = "Problem ID") @PathVariable Long problemId,
                                                         @RequestBody BlogQueryDTO blogQueryDTO,
                                                         @Parameter(hidden = true) @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ResponseResult.success(blogService.selectProblemSolutions(problemId, blogQueryDTO, userId));
    }

    @Operation(summary = "Delete blog")
    @DeleteMapping("/{bid}")
    public ResponseResult<?> deleteBlog(@Parameter(description = "Blog ID") @PathVariable Long bid) {
        boolean success = blogService.deleteBlogById(bid);
        return success ? ResponseResult.success() : ResponseResult.fail("删除失败");
    }

    @Operation(summary = "Edit blog")
    @PutMapping("/{bid}")
    public ResponseResult<?> editBlog(@Parameter(description = "Blog ID") @PathVariable Long bid,
                                      @RequestBody BlogEditDTO blogEditDTO,
                                      @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        blogEditDTO.setId(bid);
        if (blogEditDTO.getTitle() != null && blogEditDTO.getTitle().isBlank()) blogEditDTO.setTitle(null);
        if (blogEditDTO.getContent() != null && blogEditDTO.getContent().isBlank()) blogEditDTO.setContent(null);
        boolean success = blogService.editBlog(blogEditDTO, userId);
        return success ? ResponseResult.success() : ResponseResult.fail("修改失败");
    }

    @Operation(summary = "Query blog comments")
    @PostMapping("/{bid}/comments/query")
    public ResponseResult<PageVO<CommentVO>> selectCommentPage(@Parameter(description = "Blog ID") @PathVariable Long bid,
                                                               @RequestBody PageDTO pageDTO) {
        return ResponseResult.success(blogService.selectCommentPage(bid, pageDTO));
    }

    @Operation(summary = "Add comment")
    @PostMapping("/{bid}/comments")
    public ResponseResult<?> addComment(@Parameter(description = "Blog ID") @PathVariable Long bid,
                                        @RequestBody BlogCommentSaveDTO saveDTO,
                                        @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        boolean success = blogService.saveComment(bid, saveDTO, userId);
        return success ? ResponseResult.success() : ResponseResult.fail("添加失败");
    }

    @Operation(summary = "Favorite blog")
    @PostMapping("/{bid}/star")
    public ResponseResult<?> starBlog(@Parameter(description = "Blog ID") @PathVariable Long bid,
                                      @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        boolean success = userBlogService.starBlog(bid, userId);
        return success ? ResponseResult.success() : ResponseResult.fail("收藏失败");
    }

    @Operation(summary = "Unfavorite blog")
    @DeleteMapping("/{bid}/star")
    public ResponseResult<?> unstarBlog(@Parameter(description = "Blog ID") @PathVariable Long bid,
                                        @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        boolean success = userBlogService.unstarBlog(bid, userId);
        return success ? ResponseResult.success() : ResponseResult.fail("取消失败");
    }

    @Operation(summary = "Like blog")
    @PostMapping("/{bid}/like")
    public ResponseResult<?> likeBlog(@Parameter(description = "Blog ID") @PathVariable Long bid,
                                      @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        boolean success = blogService.likeBlog(bid, userId);
        return success ? ResponseResult.success() : ResponseResult.fail("点赞失败");
    }

    @Operation(summary = "Unlike blog")
    @DeleteMapping("/{bid}/like")
    public ResponseResult<?> unlikeBlog(@Parameter(description = "Blog ID") @PathVariable Long bid,
                                        @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        boolean success = blogService.unlikeBlog(bid, userId);
        return success ? ResponseResult.success() : ResponseResult.fail("取消失败");
    }

    @Operation(summary = "Upload blog image")
    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseResult<BlogPictureVO> uploadImage(@RequestPart("file") MultipartFile file,
                                                     @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return ResponseResult.success(blogImageService.upload(file, userId));
    }

    @Operation(summary = "Download blog image")
    @GetMapping("/images/{id}/download")
    public ResponseEntity<InputStreamResource> downloadImage(@Parameter(description = "Image ID") @PathVariable Long id) {
        return blogImageService.download(id);
    }

    @Operation(summary = "Delete blog image")
    @DeleteMapping("/images/{id}")
    public ResponseResult<?> deleteImage(@Parameter(description = "Image ID") @PathVariable Long id,
                                         @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        boolean success = blogImageService.delete(id, userId);
        return success ? ResponseResult.success() : ResponseResult.fail("删除失败");
    }

    @Operation(summary = "Get blog user profile")
    @GetMapping("/user/{uid}")
    public ResponseResult<UserBlogVO> userBlog(@Parameter(description = "User ID") @PathVariable Long uid) {
        UserBlogVO vo = userBlogService.selectUserBlogById(uid);
        return vo != null ? ResponseResult.success(vo) : ResponseResult.fail(404, "未找到该用户");
    }

    @Operation(summary = "Query user's blogs")
    @PostMapping("/user/{uid}/blogs/query")
    public ResponseResult<PageVO<BlogVO>> userBlogBlogs(@Parameter(description = "User ID") @PathVariable Long uid,
                                                        @RequestBody UserBlogBlogsQueryDTO blogsQueryDTO) {
        blogsQueryDTO.setUserId(uid);
        return ResponseResult.success(userBlogService.selectUserBlogBlogs(blogsQueryDTO));
    }

    @Operation(summary = "Query user's favorite blogs")
    @PostMapping("/user/{uid}/stars/query")
    public ResponseResult<PageVO<BlogVO>> userBlogStars(@Parameter(description = "User ID") @PathVariable Long uid,
                                                        @RequestBody UserBlogStarsQueryDTO starsQueryDTO) {
        starsQueryDTO.setUserId(uid);
        return ResponseResult.success(userBlogService.selectUserBlogStars(starsQueryDTO));
    }

    @Operation(summary = "List tags")
    @GetMapping("/tags")
    public ResponseResult<List<BlogTagVO>> tags() {
        return ResponseResult.success(blogService.selectAllTags());
    }

    @Operation(summary = "Get tag")
    @GetMapping("/tags/{tagId}")
    public ResponseResult<BlogTagVO> getTag(@Parameter(description = "Tag ID") @PathVariable Long tagId) {
        BlogTagVO vo = blogService.selectTagById(tagId);
        return vo != null ? ResponseResult.success(vo) : ResponseResult.fail(404, "未找到该标签");
    }

    @Operation(summary = "Create tag")
    @PostMapping("/tags")
    public ResponseResult<BlogTagVO> addTag(@Valid @RequestBody BlogTagSaveDTO saveDTO) {
        return ResponseResult.success(blogService.saveTag(saveDTO));
    }

    @Operation(summary = "Edit tag")
    @PutMapping("/tags/{tagId}")
    public ResponseResult<BlogTagVO> editTag(@Parameter(description = "Tag ID") @PathVariable Long tagId,
                                             @Valid @RequestBody BlogTagSaveDTO saveDTO) {
        saveDTO.setId(tagId);
        return ResponseResult.success(blogService.updateTag(saveDTO));
    }

    @Operation(summary = "Delete tag")
    @DeleteMapping("/tags/{tagId}")
    public ResponseResult<?> deleteTag(@Parameter(description = "Tag ID") @PathVariable Long tagId) {
        boolean success = blogService.deleteTagById(tagId);
        return success ? ResponseResult.success() : ResponseResult.fail("删除失败");
    }

    @Operation(summary = "Query comments")
    @PostMapping("/comments/query")
    public ResponseResult<List<CommentVO>> queryComments(@RequestBody CommentQueryDTO queryDTO) {
        return ResponseResult.success(blogService.selectComment(queryDTO));
    }

    @Operation(summary = "Get comment")
    @GetMapping("/comments/{cid}")
    public ResponseResult<CommentVO> getComment(@Parameter(description = "Comment ID") @PathVariable Long cid) {
        CommentVO vo = blogService.selectCommentById(cid);
        return vo != null ? ResponseResult.success(vo) : ResponseResult.fail(404, "未找到该评论");
    }

    @Operation(summary = "Delete comment")
    @DeleteMapping("/comments/{cid}")
    public ResponseResult<?> deleteComment(@Parameter(description = "Comment ID") @PathVariable Long cid) {
        int code = blogService.deleteComment(cid);
        return switch (code) {
            case HttpServletResponse.SC_OK -> ResponseResult.success();
            case HttpServletResponse.SC_NOT_FOUND -> ResponseResult.fail(404, "未找到该评论");
            default -> ResponseResult.fail(500, "服务器错误");
        };
    }
}

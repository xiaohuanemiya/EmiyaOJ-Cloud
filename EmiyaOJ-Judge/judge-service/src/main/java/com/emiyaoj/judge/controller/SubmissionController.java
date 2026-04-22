package com.emiyaoj.judge.controller;

import com.emiyaoj.common.domain.PageDTO;
import com.emiyaoj.common.domain.PageVO;
import com.emiyaoj.common.domain.ResponseResult;
import com.emiyaoj.judge.dto.SubmissionVO;
import com.emiyaoj.judge.service.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 提交记录查询控制器
 */
@Tag(name = "提交记录管理")
@RestController
@RequestMapping("/submission")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @Operation(summary = "根据ID查询提交记录")
    @GetMapping("/{id}")
    public ResponseResult<SubmissionVO> getSubmissionById(@Parameter(description = "提交记录ID") @PathVariable Long id) {
        SubmissionVO vo = submissionService.getSubmissionById(id);
        if (vo == null) {
            return ResponseResult.fail(404, "提交记录不存在");
        }
        return ResponseResult.success(vo);
    }

    @Operation(summary = "分页查询提交记录")
    @GetMapping("/page")
    public ResponseResult<PageVO<SubmissionVO>> getSubmissionPage(
            PageDTO pageDTO,
            @Parameter(description = "题目ID") @RequestParam(required = false) Long problemId,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId) {
        PageVO<SubmissionVO> page = submissionService.getSubmissionPage(pageDTO, problemId, userId);
        return ResponseResult.success(page);
    }

    @Operation(summary = "查询当前用户的提交记录")
    @GetMapping("/my")
    public ResponseResult<PageVO<SubmissionVO>> getMySubmissions(
            PageDTO pageDTO,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "题目ID") @RequestParam(required = false) Long problemId) {
        PageVO<SubmissionVO> page = submissionService.getSubmissionPage(pageDTO, problemId, userId);
        return ResponseResult.success(page);
    }
}

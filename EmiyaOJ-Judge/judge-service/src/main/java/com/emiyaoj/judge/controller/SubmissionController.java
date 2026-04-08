package com.emiyaoj.judge.controller;

import com.emiyaoj.common.domain.PageDTO;
import com.emiyaoj.common.domain.PageVO;
import com.emiyaoj.common.domain.ResponseResult;
import com.emiyaoj.judge.dto.SubmissionVO;
import com.emiyaoj.judge.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 提交记录查询控制器
 */
@RestController
@RequestMapping("/submission")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    /**
     * 根据ID查询提交记录
     */
    @GetMapping("/{id}")
    public ResponseResult<SubmissionVO> getSubmissionById(@PathVariable Long id) {
        SubmissionVO vo = submissionService.getSubmissionById(id);
        if (vo == null) {
            return ResponseResult.fail(404, "提交记录不存在");
        }
        return ResponseResult.success(vo);
    }

    /**
     * 分页查询提交记录
     */
    @GetMapping("/page")
    public ResponseResult<PageVO<SubmissionVO>> getSubmissionPage(
            PageDTO pageDTO,
            @RequestParam(required = false) Long problemId,
            @RequestParam(required = false) Long userId) {
        PageVO<SubmissionVO> page = submissionService.getSubmissionPage(pageDTO, problemId, userId);
        return ResponseResult.success(page);
    }

    /**
     * 查询当前用户的提交记录
     */
    @GetMapping("/my")
    public ResponseResult<PageVO<SubmissionVO>> getMySubmissions(
            PageDTO pageDTO,
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) Long problemId) {
        PageVO<SubmissionVO> page = submissionService.getSubmissionPage(pageDTO, problemId, userId);
        return ResponseResult.success(page);
    }
}

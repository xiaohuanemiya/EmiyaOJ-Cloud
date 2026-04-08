package com.emiyaoj.judge.controller;

import com.emiyaoj.common.domain.ResponseResult;
import com.emiyaoj.judge.dto.SubmissionVO;
import com.emiyaoj.judge.dto.SubmitCodeDTO;
import com.emiyaoj.judge.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 判题提交控制器
 */
@RestController
@RequestMapping("/judge")
@RequiredArgsConstructor
public class JudgeController {

    private final SubmissionService submissionService;

    /**
     * 提交代码进行判题
     */
    @PostMapping("/submit")
    public ResponseResult<SubmissionVO> submitCode(
            @Valid @RequestBody SubmitCodeDTO dto,
            @RequestHeader("X-User-Id") Long userId) {
        SubmissionVO vo = submissionService.submitCode(dto, userId);
        return ResponseResult.success(vo);
    }
}

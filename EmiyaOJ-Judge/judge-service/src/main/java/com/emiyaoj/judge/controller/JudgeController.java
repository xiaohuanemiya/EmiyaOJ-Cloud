package com.emiyaoj.judge.controller;

import com.emiyaoj.common.domain.ResponseResult;
import com.emiyaoj.judge.dto.SubmissionVO;
import com.emiyaoj.judge.dto.SubmitCodeDTO;
import com.emiyaoj.judge.service.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 判题提交控制器。
 */
@Tag(name = "判题管理")
@RestController
@RequestMapping("/judge")
@RequiredArgsConstructor
public class JudgeController {

    private final SubmissionService submissionService;

    @Operation(summary = "提交代码进行判题")
    @PostMapping("/submit")
    public ResponseResult<SubmissionVO> submitCode(
            @Valid @RequestBody SubmitCodeDTO dto,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        SubmissionVO vo = submissionService.submitCode(dto, userId);
        return ResponseResult.success(vo);
    }
}

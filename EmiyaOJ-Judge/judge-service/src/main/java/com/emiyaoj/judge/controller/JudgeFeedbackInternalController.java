package com.emiyaoj.judge.controller;

import com.emiyaoj.common.domain.ResponseResult;
import com.emiyaoj.common.exception.BaseException;
import com.emiyaoj.judge.dto.JudgeFeedbackCallbackDTO;
import com.emiyaoj.judge.dto.JudgeFeedbackContextVO;
import com.emiyaoj.judge.dto.JudgeFeedbackVO;
import com.emiyaoj.judge.service.JudgeFeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Internal Judge Feedback")
@RestController
@RequestMapping("/judge/internal/feedback")
@RequiredArgsConstructor
public class JudgeFeedbackInternalController {

    private final JudgeFeedbackService judgeFeedbackService;

    @Value("${judge.internal-token:emiyaoj-judge-internal}")
    private String judgeInternalToken;

    @GetMapping("/context/{submissionId}")
    @Operation(summary = "Get sanitized feedback context for Agent")
    public ResponseResult<JudgeFeedbackContextVO> getContext(
            @Parameter(hidden = true) @RequestHeader("X-Judge-Internal-Token") String internalToken,
            @PathVariable Long submissionId) {
        validateToken(internalToken);
        return ResponseResult.success(judgeFeedbackService.buildContext(submissionId));
    }

    @PostMapping
    @Operation(summary = "Apply Agent feedback result")
    public ResponseResult<JudgeFeedbackVO> applyFeedback(
            @Parameter(hidden = true) @RequestHeader("X-Judge-Internal-Token") String internalToken,
            @RequestBody JudgeFeedbackCallbackDTO callback) {
        validateToken(internalToken);
        return ResponseResult.success(judgeFeedbackService.applyCallback(callback));
    }

    private void validateToken(String internalToken) {
        if (!judgeInternalToken.equals(internalToken)) {
            throw new BaseException(403, "Invalid internal token");
        }
    }
}

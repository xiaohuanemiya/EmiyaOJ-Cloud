package com.emiyaoj.judge.controller;

import com.emiyaoj.common.domain.ResponseResult;
import com.emiyaoj.common.exception.BaseException;
import com.emiyaoj.judge.dto.TestCaseGeneratorRunRequestDTO;
import com.emiyaoj.judge.dto.TestCaseGeneratorRunResultVO;
import com.emiyaoj.judge.service.TestCaseGeneratorRunner;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Internal Test Case Generator")
@RestController
@RequestMapping("/judge/internal/test-case-generator")
@RequiredArgsConstructor
public class TestCaseGeneratorInternalController {

    private final TestCaseGeneratorRunner testCaseGeneratorRunner;

    @Value("${judge.internal-token:emiyaoj-judge-internal}")
    private String judgeInternalToken;

    @PostMapping("/run")
    @Operation(summary = "Run Python test case generator in sandbox")
    public ResponseResult<TestCaseGeneratorRunResultVO> runGenerator(
            @Parameter(hidden = true) @RequestHeader("X-Internal-Token") String internalToken,
            @RequestBody TestCaseGeneratorRunRequestDTO request) {
        if (!judgeInternalToken.equals(internalToken)) {
            throw new BaseException(403, "Invalid internal token");
        }
        return ResponseResult.success(testCaseGeneratorRunner.run(
                request == null ? null : request.getGeneratorCode()));
    }
}

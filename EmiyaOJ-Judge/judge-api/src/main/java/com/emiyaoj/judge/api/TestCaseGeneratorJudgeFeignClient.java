package com.emiyaoj.judge.api;

import com.emiyaoj.common.domain.ResponseResult;
import com.emiyaoj.judge.dto.TestCaseGeneratorRunRequestDTO;
import com.emiyaoj.judge.dto.TestCaseGeneratorRunResultVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(contextId = "testCaseGeneratorJudgeFeignClient", name = "judge-service",
        path = "/judge/internal/test-case-generator")
public interface TestCaseGeneratorJudgeFeignClient {

    @PostMapping("/run")
    ResponseResult<TestCaseGeneratorRunResultVO> runGenerator(
            @RequestHeader("X-Internal-Token") String internalToken,
            @RequestBody TestCaseGeneratorRunRequestDTO request);
}

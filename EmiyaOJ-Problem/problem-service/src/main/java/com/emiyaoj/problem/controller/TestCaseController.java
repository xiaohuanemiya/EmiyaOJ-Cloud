package com.emiyaoj.problem.controller;

import com.emiyaoj.common.domain.ResponseResult;
import com.emiyaoj.problem.dto.TestCaseVO;
import com.emiyaoj.problem.service.TestCaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 测试用例控制器
 */
@Tag(name = "测试用例管理")
@RestController
@RequestMapping("/test-case")
@RequiredArgsConstructor
public class TestCaseController {

    private final TestCaseService testCaseService;

    /**
     * 根据题目 ID 查询测试用例（供 Feign 调用）
     */
    @GetMapping("/problem/{problemId}")
    @Operation(summary = "根据题目ID查询测试用例")
    public ResponseResult<List<TestCaseVO>> getByProblemId(@PathVariable Long problemId) {
        List<TestCaseVO> list = testCaseService.getByProblemId(problemId);
        return ResponseResult.success(list);
    }
}

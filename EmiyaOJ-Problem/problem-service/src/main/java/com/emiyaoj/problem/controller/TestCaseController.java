package com.emiyaoj.problem.controller;

import com.emiyaoj.common.domain.ResponseResult;
import com.emiyaoj.problem.dto.TestCaseSaveDTO;
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
     * 根据题目 ID 查询测试用例列表（供 Feign 调用）
     */
    @GetMapping("/problem/{problemId}")
    @Operation(summary = "根据题目ID查询测试用例列表")
    public ResponseResult<List<TestCaseVO>> getByProblemId(@PathVariable Long problemId) {
        List<TestCaseVO> list = testCaseService.getByProblemId(problemId);
        return ResponseResult.success(list);
    }

    /**
     * 根据 ID 查询单个测试用例
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询单个测试用例")
    public ResponseResult<TestCaseVO> getById(@PathVariable Long id) {
        TestCaseVO vo = testCaseService.getTestCaseById(id);
        if (vo == null) {
            return ResponseResult.fail(404, "测试用例不存在");
        }
        return ResponseResult.success(vo);
    }

    /**
     * 新增单个测试用例
     */
    @PostMapping
    @Operation(summary = "新增测试用例")
    public ResponseResult<TestCaseVO> save(@RequestBody TestCaseSaveDTO dto) {
        TestCaseVO vo = testCaseService.saveTestCase(dto);
        return ResponseResult.success(vo);
    }

    /**
     * 批量新增测试用例
     */
    @PostMapping("/batch/{problemId}")
    @Operation(summary = "批量新增测试用例")
    public ResponseResult<List<TestCaseVO>> batchSave(@PathVariable Long problemId,
                                                       @RequestBody List<TestCaseSaveDTO> dtos) {
        List<TestCaseVO> result = testCaseService.batchSaveTestCases(problemId, dtos);
        return ResponseResult.success(result);
    }

    /**
     * 更新测试用例
     */
    @PutMapping
    @Operation(summary = "更新测试用例")
    public ResponseResult<Boolean> update(@RequestBody TestCaseSaveDTO dto) {
        boolean result = testCaseService.updateTestCase(dto);
        return ResponseResult.success(result);
    }

    /**
     * 删除单个测试用例（逻辑删除）
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除单个测试用例")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        boolean result = testCaseService.deleteTestCaseById(id);
        return ResponseResult.success(result);
    }

    /**
     * 批量删除测试用例（逻辑删除）
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除测试用例")
    public ResponseResult<Boolean> batchDelete(@RequestBody List<Long> ids) {
        boolean result = testCaseService.batchDeleteByIds(ids);
        return ResponseResult.success(result);
    }

    /**
     * 删除题目下所有测试用例（逻辑删除）
     */
    @DeleteMapping("/problem/{problemId}")
    @Operation(summary = "删除题目下所有测试用例")
    public ResponseResult<Boolean> deleteByProblemId(@PathVariable Long problemId) {
        boolean result = testCaseService.deleteByProblemId(problemId);
        return ResponseResult.success(result);
    }
}

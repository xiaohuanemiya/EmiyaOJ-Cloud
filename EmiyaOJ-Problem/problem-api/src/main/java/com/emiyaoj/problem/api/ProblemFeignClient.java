package com.emiyaoj.problem.api;

import com.emiyaoj.common.domain.ResponseResult;
import com.emiyaoj.problem.dto.ContestSubmitCheckVO;
import com.emiyaoj.problem.dto.ProblemVO;
import com.emiyaoj.problem.dto.TestCaseVO;
import com.emiyaoj.problem.dto.LanguageVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 题目服务 Feign 客户端 — 供判题服务等远程调用
 */
@FeignClient(name = "problem-service")
public interface ProblemFeignClient {

    /**
     * 获取题目详情
     */
    @GetMapping("/problem/{id}")
    ResponseResult<ProblemVO> getProblemById(@PathVariable("id") Long id);

    /**
     * 获取题目的所有测试用例
     */
    @GetMapping("/test-case/problem/{problemId}")
    ResponseResult<List<TestCaseVO>> getTestCasesByProblemId(@PathVariable("problemId") Long problemId);

    /**
     * 获取编程语言配置
     */
    @GetMapping("/language/{id}")
    ResponseResult<LanguageVO> getLanguageById(@PathVariable("id") Long id);

    @GetMapping("/contest/internal/{id}/submit-check")
    ResponseResult<ContestSubmitCheckVO> checkContestSubmit(@PathVariable("id") Long id,
                                                            @RequestParam("problemId") Long problemId,
                                                            @RequestParam("userId") Long userId);
}

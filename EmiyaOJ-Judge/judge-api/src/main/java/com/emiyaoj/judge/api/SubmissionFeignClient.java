package com.emiyaoj.judge.api;

import com.emiyaoj.common.domain.ResponseResult;
import com.emiyaoj.common.domain.PageDTO;
import com.emiyaoj.common.domain.PageVO;
import com.emiyaoj.judge.dto.JudgeUserStatsVO;
import com.emiyaoj.judge.dto.SolvedProblemVO;
import com.emiyaoj.judge.dto.SubmissionDetailVO;
import com.emiyaoj.judge.dto.SubmissionVO;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 判题服务 Feign 客户端。
 */
@FeignClient(name = "judge-service", path = "/submission")
public interface SubmissionFeignClient {

    /**
     * 根据ID查询提交记录详情。
     */
    @GetMapping("/{id}")
    ResponseResult<SubmissionDetailVO> getSubmissionById(@PathVariable("id") Long id);

    @GetMapping("/contest/{contestId}")
    ResponseResult<List<SubmissionVO>> listContestSubmissions(@PathVariable("contestId") Long contestId);

    @GetMapping("/user/{userId}/stats")
    ResponseResult<JudgeUserStatsVO> getUserStats(@PathVariable("userId") Long userId);

    @GetMapping("/user/{userId}/solved")
    ResponseResult<PageVO<SolvedProblemVO>> getSolvedProblems(@PathVariable("userId") Long userId,
                                                              @SpringQueryMap PageDTO pageDTO,
                                                              @RequestParam(value = "difficulty", required = false) Integer difficulty);
}

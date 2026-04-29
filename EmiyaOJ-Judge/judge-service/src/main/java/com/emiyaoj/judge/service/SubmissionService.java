package com.emiyaoj.judge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.emiyaoj.common.domain.PageDTO;
import com.emiyaoj.common.domain.PageVO;
import com.emiyaoj.common.domain.ResponseResult;
import com.emiyaoj.common.exception.BaseException;
import com.emiyaoj.judge.domain.JudgeStatus;
import com.emiyaoj.judge.domain.entity.Submission;
import com.emiyaoj.judge.domain.entity.SubmissionCaseResult;
import com.emiyaoj.judge.domain.entity.SubmissionJudgeResult;
import com.emiyaoj.judge.dto.SubmissionCaseResultVO;
import com.emiyaoj.judge.dto.SubmissionDetailVO;
import com.emiyaoj.judge.dto.SubmissionVO;
import com.emiyaoj.judge.dto.SubmitCodeDTO;
import com.emiyaoj.judge.mapper.SubmissionCaseResultMapper;
import com.emiyaoj.judge.mapper.SubmissionJudgeResultMapper;
import com.emiyaoj.judge.mapper.SubmissionMapper;
import com.emiyaoj.problem.api.ProblemFeignClient;
import com.emiyaoj.problem.dto.LanguageVO;
import com.emiyaoj.problem.dto.ProblemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 提交与判题服务。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionMapper submissionMapper;
    private final SubmissionJudgeResultMapper judgeResultMapper;
    private final SubmissionCaseResultMapper caseResultMapper;
    private final JudgeExecutor judgeExecutor;
    private final ProblemFeignClient problemFeignClient;

    /**
     * 提交代码：创建提交记录和初始判题汇总结果后异步执行判题。
     */
    public SubmissionVO submitCode(SubmitCodeDTO dto, Long userId) {
        ResponseResult<ProblemVO> problemResult = problemFeignClient.getProblemById(dto.getProblemId());
        if (problemResult == null || problemResult.getCode() != 200 || problemResult.getData() == null) {
            throw new BaseException(400, "题目不存在");
        }

        ResponseResult<LanguageVO> languageResult = problemFeignClient.getLanguageById(dto.getLanguageId());
        if (languageResult == null || languageResult.getCode() != 200 || languageResult.getData() == null) {
            throw new BaseException(400, "编程语言不存在");
        }

        Submission submission = new Submission();
        submission.setProblemId(dto.getProblemId());
        submission.setUserId(userId);
        submission.setLanguageId(dto.getLanguageId());
        submission.setCode(dto.getCode());
        submission.setCreateTime(LocalDateTime.now());
        submission.setUpdateTime(LocalDateTime.now());
        submission.setDeleted(0);
        submissionMapper.insert(submission);

        SubmissionJudgeResult judgeResult = new SubmissionJudgeResult();
        judgeResult.setSubmissionId(submission.getId());
        judgeResult.setStatus(JudgeStatus.PENDING);
        judgeResult.setPassedCaseCount(0);
        judgeResult.setTotalCaseCount(0);
        judgeResult.setScore(0);
        judgeResult.setMaxTimeUsed(0L);
        judgeResult.setMaxMemoryUsed(0L);
        judgeResultMapper.insert(judgeResult);

        log.info("Submission created: id={}", submission.getId());
        judgeExecutor.executeJudgeAsync(submission.getId(), dto.getProblemId(), dto.getLanguageId(), dto.getCode());

        return toSubmissionVO(submission, judgeResult);
    }

    /**
     * 查询提交详情。
     */
    public SubmissionDetailVO getSubmissionById(Long id) {
        Submission submission = submissionMapper.selectById(id);
        if (submission == null) {
            return null;
        }

        SubmissionJudgeResult judgeResult = selectJudgeResult(id);
        List<SubmissionCaseResult> caseResults = caseResultMapper.selectList(
                new LambdaQueryWrapper<SubmissionCaseResult>()
                        .eq(SubmissionCaseResult::getSubmissionId, id)
                        .orderByAsc(SubmissionCaseResult::getCaseOrder)
                        .orderByAsc(SubmissionCaseResult::getId)
        );

        SubmissionDetailVO vo = new SubmissionDetailVO();
        BeanUtils.copyProperties(toSubmissionVO(submission, judgeResult), vo);
        vo.setCaseResults(caseResults.stream().map(this::toCaseResultVO).toList());
        return vo;
    }

    /**
     * 分页查询提交记录。
     */
    public PageVO<SubmissionVO> getSubmissionPage(PageDTO pageDTO, Long problemId, Long userId) {
        LambdaQueryWrapper<Submission> wrapper = new LambdaQueryWrapper<>();
        if (problemId != null) {
            wrapper.eq(Submission::getProblemId, problemId);
        }
        if (userId != null) {
            wrapper.eq(Submission::getUserId, userId);
        }
        wrapper.orderByDesc(Submission::getCreateTime);

        Page<Submission> page = submissionMapper.selectPage(
                new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize()),
                wrapper
        );

        Map<Long, SubmissionJudgeResult> resultMap = selectJudgeResultMap(page.getRecords());
        List<SubmissionVO> voList = page.getRecords().stream()
                .map(submission -> toSubmissionVO(submission, resultMap.get(submission.getId())))
                .toList();

        return new PageVO<>(page.getTotal(), voList, (long) pageDTO.getPageNum(), (long) pageDTO.getPageSize());
    }

    private SubmissionJudgeResult selectJudgeResult(Long submissionId) {
        return judgeResultMapper.selectOne(
                new LambdaQueryWrapper<SubmissionJudgeResult>()
                        .eq(SubmissionJudgeResult::getSubmissionId, submissionId)
                        .last("LIMIT 1")
        );
    }

    private Map<Long, SubmissionJudgeResult> selectJudgeResultMap(List<Submission> submissions) {
        if (submissions == null || submissions.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> submissionIds = submissions.stream().map(Submission::getId).toList();
        return judgeResultMapper.selectList(
                        new LambdaQueryWrapper<SubmissionJudgeResult>()
                                .in(SubmissionJudgeResult::getSubmissionId, submissionIds)
                ).stream()
                .collect(Collectors.toMap(
                        SubmissionJudgeResult::getSubmissionId,
                        Function.identity(),
                        (left, right) -> left
                ));
    }

    private SubmissionVO toSubmissionVO(Submission submission, SubmissionJudgeResult judgeResult) {
        SubmissionVO vo = new SubmissionVO();
        vo.setId(submission.getId());
        vo.setProblemId(submission.getProblemId());
        vo.setUserId(submission.getUserId());
        vo.setLanguageId(submission.getLanguageId());
        vo.setCreateTime(submission.getCreateTime());

        if (judgeResult == null) {
            vo.setStatus(JudgeStatus.PENDING);
            vo.setPassedCaseCount(0);
            vo.setTotalCaseCount(0);
            vo.setScore(0);
            vo.setMaxTimeUsed(0L);
            vo.setMaxMemoryUsed(0L);
            return vo;
        }

        vo.setStatus(judgeResult.getStatus());
        vo.setPassedCaseCount(judgeResult.getPassedCaseCount());
        vo.setTotalCaseCount(judgeResult.getTotalCaseCount());
        vo.setScore(judgeResult.getScore());
        vo.setMaxTimeUsed(judgeResult.getMaxTimeUsed());
        vo.setMaxMemoryUsed(judgeResult.getMaxMemoryUsed());
        vo.setErrorMessage(judgeResult.getErrorMessage());
        vo.setCompileMessage(judgeResult.getCompileMessage());
        vo.setFinishTime(judgeResult.getFinishTime());
        return vo;
    }

    private SubmissionCaseResultVO toCaseResultVO(SubmissionCaseResult result) {
        SubmissionCaseResultVO vo = new SubmissionCaseResultVO();
        BeanUtils.copyProperties(result, vo);
        return vo;
    }
}

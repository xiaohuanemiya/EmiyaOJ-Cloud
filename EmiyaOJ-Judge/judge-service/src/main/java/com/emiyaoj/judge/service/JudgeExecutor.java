package com.emiyaoj.judge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.emiyaoj.common.domain.ResponseResult;
import com.emiyaoj.judge.domain.JudgeStatus;
import com.emiyaoj.judge.domain.entity.Submission;
import com.emiyaoj.judge.domain.entity.SubmissionCaseResult;
import com.emiyaoj.judge.domain.entity.SubmissionJudgeResult;
import com.emiyaoj.judge.domain.gojudge.GoJudgeResult;
import com.emiyaoj.judge.domain.gojudge.GoJudgeStatus;
import com.emiyaoj.judge.mapper.SubmissionCaseResultMapper;
import com.emiyaoj.judge.mapper.SubmissionJudgeResultMapper;
import com.emiyaoj.judge.mapper.SubmissionMapper;
import com.emiyaoj.problem.api.ProblemFeignClient;
import com.emiyaoj.problem.dto.LanguageVO;
import com.emiyaoj.problem.dto.ProblemVO;
import com.emiyaoj.problem.dto.TestCaseVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 判题执行器，异步执行判题逻辑。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JudgeExecutor {

    private final SubmissionMapper submissionMapper;
    private final SubmissionJudgeResultMapper judgeResultMapper;
    private final SubmissionCaseResultMapper caseResultMapper;
    private final GoJudgeService goJudgeService;
    private final ProblemFeignClient problemFeignClient;
    private final JudgeFeedbackService judgeFeedbackService;

    /**
     * 异步执行判题。
     */
    @Async
    public void executeJudgeAsync(Long submissionId, Long problemId, Long languageId, String code) {
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            log.error("Submission not found: {}", submissionId);
            return;
        }

        updateJudgeStatus(submissionId, JudgeStatus.JUDGING);
        clearCaseResults(submissionId);

        Map<String, String> fileIds = null;
        List<TestCaseVO> testCases = List.of();
        List<SubmissionCaseResult> caseResults = new ArrayList<>();

        try {
            ResponseResult<LanguageVO> langResult = problemFeignClient.getLanguageById(languageId);
            if (langResult == null || langResult.getData() == null) {
                completeJudge(submission, testCases, caseResults, JudgeStatus.SYSTEM_ERROR, "获取语言信息失败", null);
                return;
            }
            LanguageVO language = langResult.getData();

            ResponseResult<List<TestCaseVO>> testCaseResult = problemFeignClient.getTestCasesByProblemId(problemId);
            if (testCaseResult == null || testCaseResult.getData() == null || testCaseResult.getData().isEmpty()) {
                completeJudge(submission, testCases, caseResults, JudgeStatus.SYSTEM_ERROR, "获取测试用例失败或无测试用例", null);
                return;
            }
            testCases = testCaseResult.getData();

            ResponseResult<ProblemVO> problemResult = problemFeignClient.getProblemById(problemId);
            ProblemVO problem = problemResult != null ? problemResult.getData() : null;
            long timeLimit = problem != null && problem.getTimeLimit() != null ? problem.getTimeLimit() : 1000;
            long memoryLimit = problem != null && problem.getMemoryLimit() != null ? problem.getMemoryLimit() : 256;

            if (language.getIsCompiled() == null || language.getIsCompiled() == 1) {
                GoJudgeResult compileResult = goJudgeService.compile(code, language);
                if (compileResult != null && !GoJudgeStatus.ACCEPTED.getValue().equals(compileResult.getStatus())) {
                    String compileError = extractCompileError(compileResult);
                    completeJudge(submission, testCases, caseResults, JudgeStatus.COMPILE_ERROR, null, compileError);
                    return;
                }
                if (compileResult != null) {
                    fileIds = compileResult.getFileIds();
                }
            }

            boolean weightedScore = JudgeResultCalculator.useWeightedScore(testCases);
            Integer forcedStatus = null;
            String errorMessage = null;

            for (int i = 0; i < testCases.size(); i++) {
                TestCaseVO testCase = testCases.get(i);
                GoJudgeResult runResult = goJudgeService.run(language, fileIds, code, testCase, timeLimit, memoryLimit);
                SubmissionCaseResult caseResult = buildBaseCaseResult(submissionId, testCase, i + 1, runResult);
                JudgeFeedbackHintBuilder.applyOutputHint(caseResult, testCase, runResult);

                if (runResult == null) {
                    JudgeResultCalculator.CaseFailure failure = JudgeResultCalculator.mapExecutionFailure(null);
                    caseResult.setStatus(failure.status());
                    caseResult.setErrorMessage(failure.message());
                    forcedStatus = failure.status();
                    errorMessage = failure.message();
                    saveCaseResult(caseResult, caseResults);
                    break;
                }

                if (!GoJudgeStatus.ACCEPTED.getValue().equals(runResult.getStatus())) {
                    JudgeResultCalculator.CaseFailure failure = JudgeResultCalculator.mapExecutionFailure(runResult);
                    caseResult.setStatus(failure.status());
                    caseResult.setErrorMessage(failure.message());
                    forcedStatus = failure.status();
                    errorMessage = failure.message();
                    saveCaseResult(caseResult, caseResults);
                    break;
                }

                boolean accepted = outputAccepted(runResult, testCase);
                caseResult.setStatus(accepted ? JudgeStatus.ACCEPTED : JudgeStatus.WRONG_ANSWER);
                caseResult.setScore(JudgeResultCalculator.awardedCaseScore(testCase, weightedScore, accepted));
                if (!accepted && errorMessage == null) {
                    errorMessage = "Wrong Answer";
                }
                saveCaseResult(caseResult, caseResults);
            }

            SubmissionJudgeResult summary = updateSummary(submissionId, testCases, caseResults, forcedStatus, errorMessage, null);
            if (summary.getStatus() == JudgeStatus.PARTIAL_ACCEPTED) {
                summary.setErrorMessage("Partial Accepted (" + summary.getPassedCaseCount() + "/" + summary.getTotalCaseCount() + ")");
                updateSummaryById(summary);
            }
            publishFeedbackTask(submission, summary);

            log.info("Judge complete: submissionId={}, score={}, passed={}/{}",
                    submissionId, summary.getScore(), summary.getPassedCaseCount(), summary.getTotalCaseCount());
        } catch (Exception e) {
            log.error("Judge failed for submission: {}", submissionId, e);
            completeJudge(submission, testCases, caseResults, JudgeStatus.SYSTEM_ERROR, "系统错误: " + e.getMessage(), null);
        } finally {
            if (fileIds != null) {
                fileIds.values().forEach(goJudgeService::deleteFile);
            }
        }
    }

    private SubmissionJudgeResult completeJudge(Submission submission, List<TestCaseVO> testCases,
                                                List<SubmissionCaseResult> caseResults, Integer forcedStatus,
                                                String errorMessage, String compileMessage) {
        SubmissionJudgeResult summary = updateSummary(
                submission.getId(), testCases, caseResults, forcedStatus, errorMessage, compileMessage);
        publishFeedbackTask(submission, summary);
        return summary;
    }

    private SubmissionCaseResult buildBaseCaseResult(Long submissionId, TestCaseVO testCase, int caseOrder,
                                                     GoJudgeResult runResult) {
        SubmissionCaseResult result = new SubmissionCaseResult();
        result.setSubmissionId(submissionId);
        result.setTestCaseId(testCase.getId());
        result.setCaseOrder(caseOrder);
        result.setStatus(JudgeStatus.SYSTEM_ERROR);
        result.setScore(0);
        result.setTimeUsed(runResult != null && runResult.getTime() != null ? runResult.getTime() / 1_000_000 : 0L);
        result.setMemoryUsed(runResult != null && runResult.getMemory() != null ? runResult.getMemory() / 1024 : 0L);
        return result;
    }

    private void saveCaseResult(SubmissionCaseResult caseResult, List<SubmissionCaseResult> caseResults) {
        caseResultMapper.insert(caseResult);
        caseResults.add(caseResult);
    }

    private SubmissionJudgeResult updateSummary(Long submissionId, List<TestCaseVO> testCases,
                                                List<SubmissionCaseResult> caseResults, Integer forcedStatus,
                                                String errorMessage, String compileMessage) {
        SubmissionJudgeResult summary = JudgeResultCalculator.buildSummary(
                submissionId, testCases, caseResults, forcedStatus, errorMessage, compileMessage);
        updateSummaryBySubmissionId(summary);
        return summary;
    }

    private void updateJudgeStatus(Long submissionId, int status) {
        SubmissionJudgeResult result = new SubmissionJudgeResult();
        result.setStatus(status);
        result.setUpdateTime(LocalDateTime.now());
        judgeResultMapper.update(result, new LambdaUpdateWrapper<SubmissionJudgeResult>()
                .eq(SubmissionJudgeResult::getSubmissionId, submissionId));
    }

    private void updateSummaryBySubmissionId(SubmissionJudgeResult summary) {
        summary.setUpdateTime(LocalDateTime.now());
        judgeResultMapper.update(summary, new LambdaUpdateWrapper<SubmissionJudgeResult>()
                .eq(SubmissionJudgeResult::getSubmissionId, summary.getSubmissionId()));
    }

    private void updateSummaryById(SubmissionJudgeResult summary) {
        SubmissionJudgeResult current = selectJudgeResult(summary.getSubmissionId());
        if (current == null) {
            updateSummaryBySubmissionId(summary);
            return;
        }
        summary.setId(current.getId());
        summary.setUpdateTime(LocalDateTime.now());
        judgeResultMapper.updateById(summary);
    }

    private SubmissionJudgeResult selectJudgeResult(Long submissionId) {
        return judgeResultMapper.selectOne(new LambdaQueryWrapper<SubmissionJudgeResult>()
                .eq(SubmissionJudgeResult::getSubmissionId, submissionId)
                .last("LIMIT 1"));
    }

    private void clearCaseResults(Long submissionId) {
        caseResultMapper.delete(new LambdaQueryWrapper<SubmissionCaseResult>()
                .eq(SubmissionCaseResult::getSubmissionId, submissionId));
    }

    private String extractCompileError(GoJudgeResult compileResult) {
        String compileError = "";
        if (compileResult.getFiles() != null) {
            compileError = compileResult.getFiles().getOrDefault("stderr", "");
        }
        if (compileError.isEmpty() && compileResult.getError() != null) {
            compileError = compileResult.getError();
        }
        return compileError;
    }

    private boolean outputAccepted(GoJudgeResult runResult, TestCaseVO testCase) {
        String actualOutput = JudgeFeedbackHintBuilder.extractStdout(runResult).trim();
        String expectedOutput = testCase.getOutput() != null ? testCase.getOutput().trim() : "";
        return actualOutput.equals(expectedOutput);
    }

    private void publishFeedbackTask(Submission submission, SubmissionJudgeResult summary) {
        try {
            judgeFeedbackService.requestFeedback(submission, summary);
        } catch (Exception e) {
            log.warn("Request judge feedback failed, submissionId={}", submission.getId(), e);
        }
    }
}

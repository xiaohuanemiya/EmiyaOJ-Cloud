package com.emiyaoj.judge.service;

import com.emiyaoj.common.domain.ResponseResult;
import com.emiyaoj.judge.domain.entity.Submission;
import com.emiyaoj.judge.domain.gojudge.GoJudgeResult;
import com.emiyaoj.judge.domain.gojudge.GoJudgeStatus;
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
import java.util.List;
import java.util.Map;

/**
 * 判题执行器 - 异步执行判题逻辑
 * 独立为单独的 Bean, 确保 @Async 代理生效
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JudgeExecutor {

    private final SubmissionMapper submissionMapper;
    private final GoJudgeService goJudgeService;
    private final ProblemFeignClient problemFeignClient;

    /**
     * 异步执行判题
     */
    @Async
    public void executeJudgeAsync(Long submissionId, Long problemId, Long languageId, String code) {
        // 更新状态为判题中
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            log.error("Submission not found: {}", submissionId);
            return;
        }
        submission.setStatus(1); // 判题中
        submission.setUpdateTime(LocalDateTime.now());
        submissionMapper.updateById(submission);

        try {
            // 获取语言信息
            ResponseResult<LanguageVO> langResult = problemFeignClient.getLanguageById(languageId);
            if (langResult == null || langResult.getData() == null) {
                updateSubmissionError(submission, "获取语言信息失败");
                return;
            }
            String languageName = langResult.getData().getName();

            // 获取测试用例
            ResponseResult<List<TestCaseVO>> testCaseResult = problemFeignClient.getTestCasesByProblemId(problemId);
            if (testCaseResult == null || testCaseResult.getData() == null || testCaseResult.getData().isEmpty()) {
                updateSubmissionError(submission, "获取测试用例失败或无测试用例");
                return;
            }
            List<TestCaseVO> testCases = testCaseResult.getData();

            // 获取题目信息 (获取时间/内存限制)
            ResponseResult<ProblemVO> problemResult = problemFeignClient.getProblemById(problemId);
            ProblemVO problem = problemResult != null ? problemResult.getData() : null;
            long timeLimit = problem != null && problem.getTimeLimit() != null ? problem.getTimeLimit() : 1000; // 毫秒
            long memoryLimit = problem != null && problem.getMemoryLimit() != null ? problem.getMemoryLimit() : 256; // MB

            // 编译
            Map<String, String> fileIds = null;
            if (!languageName.toLowerCase().contains("python")) {
                GoJudgeResult compileResult = goJudgeService.compile(code, languageName);
                if (compileResult != null) {
                    if (!GoJudgeStatus.ACCEPTED.getValue().equals(compileResult.getStatus())) {
                        String compileError = "";
                        if (compileResult.getFiles() != null) {
                            compileError = compileResult.getFiles().getOrDefault("stderr", "");
                        }
                        if (compileError.isEmpty() && compileResult.getError() != null) {
                            compileError = compileResult.getError();
                        }
                        updateSubmissionCompileError(submission, compileError);
                        return;
                    }
                    fileIds = compileResult.getFileIds();
                }
            }

            // 逐个运行测试用例
            int passCount = 0;
            long maxTime = 0;
            long maxMemory = 0;
            int judgeStatus = 2;  // 默认 AC
            String errorMsg = null;

            for (TestCaseVO testCase : testCases) {
                GoJudgeResult runResult = goJudgeService.run(languageName, fileIds, code, testCase, timeLimit, memoryLimit);

                if (runResult == null) {
                    judgeStatus = 4; // SE
                    errorMsg = "运行无返回结果";
                    break;
                }

                // 记录资源使用
                if (runResult.getTime() != null) {
                    maxTime = Math.max(maxTime, runResult.getTime() / 1_000_000); // 纳秒 -> 毫秒
                }
                if (runResult.getMemory() != null) {
                    maxMemory = Math.max(maxMemory, runResult.getMemory() / 1024); // 字节 -> KB
                }

                if (!GoJudgeStatus.ACCEPTED.getValue().equals(runResult.getStatus())) {
                    GoJudgeStatus goStatus = GoJudgeStatus.fromValue(runResult.getStatus());
                    switch (goStatus) {
                        case TIME_LIMIT_EXCEEDED -> {
                            judgeStatus = 6; // TLE
                            errorMsg = "Time Limit Exceeded";
                        }
                        case MEMORY_LIMIT_EXCEEDED -> {
                            judgeStatus = 7; // MLE
                            errorMsg = "Memory Limit Exceeded";
                        }
                        case NONZERO_EXIT_STATUS, SIGNALLED -> {
                            judgeStatus = 8; // RE
                            errorMsg = "Runtime Error";
                            if (runResult.getFiles() != null) {
                                String stderr = runResult.getFiles().getOrDefault("stderr", "");
                                if (!stderr.isBlank()) {
                                    errorMsg = "Runtime Error: " + stderr.substring(0, Math.min(stderr.length(), 500));
                                }
                            }
                        }
                        case OUTPUT_LIMIT_EXCEEDED -> {
                            judgeStatus = 9; // OLE
                            errorMsg = "Output Limit Exceeded";
                        }
                        default -> {
                            judgeStatus = 4; // SE
                            errorMsg = runResult.getStatus();
                        }
                    }
                    break; // 遇到错误立即终止后续用例
                }

                // 比较输出
                String actualOutput = runResult.getFiles() != null
                        ? runResult.getFiles().getOrDefault("stdout", "").trim()
                        : "";
                String expectedOutput = testCase.getOutput() != null
                        ? testCase.getOutput().trim()
                        : "";

                if (actualOutput.equals(expectedOutput)) {
                    passCount++;
                } else {
                    // 记录第一次答案错误，但继续运行剩余用例以计算通过率
                    if (judgeStatus == 2) {
                        judgeStatus = 5; // WA
                        errorMsg = "Wrong Answer";
                    }
                }
            }

            // 清理缓存文件
            if (fileIds != null) {
                fileIds.values().forEach(goJudgeService::deleteFile);
            }

            // 计算结果
            double passRate = testCases.isEmpty() ? 0 : (double) passCount / testCases.size();
            int score = (int) Math.round(passRate * 100);

            // 部分通过: 有通过的用例但未全部通过 (WA 状态下)
            if (judgeStatus == 5 && passCount > 0) {
                judgeStatus = 10; // PA
                errorMsg = "Partial Accepted (" + passCount + "/" + testCases.size() + ")";
            }

            submission.setStatus(judgeStatus);
            submission.setScore(score);
            submission.setTimeUsed(maxTime);
            submission.setMemoryUsed(maxMemory);
            submission.setPassRate(passRate);
            submission.setErrorMessage(errorMsg);
            submission.setUpdateTime(LocalDateTime.now());
            submissionMapper.updateById(submission);

            log.info("Judge complete: submissionId={}, score={}, passRate={}", submissionId, score, passRate);

        } catch (Exception e) {
            log.error("Judge failed for submission: {}", submissionId, e);
            updateSubmissionError(submission, "系统错误: " + e.getMessage());
        }
    }

    private void updateSubmissionError(Submission submission, String errorMessage) {
        submission.setStatus(4); // 系统错误
        submission.setErrorMessage(errorMessage);
        submission.setScore(0);
        submission.setPassRate(0.0);
        submission.setUpdateTime(LocalDateTime.now());
        submissionMapper.updateById(submission);
    }

    private void updateSubmissionCompileError(Submission submission, String compileMessage) {
        submission.setStatus(3); // 编译错误
        submission.setCompileMessage(compileMessage);
        submission.setScore(0);
        submission.setPassRate(0.0);
        submission.setUpdateTime(LocalDateTime.now());
        submissionMapper.updateById(submission);
    }
}

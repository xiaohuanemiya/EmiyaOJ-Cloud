package com.emiyaoj.judge.service;

import com.emiyaoj.judge.domain.JudgeStatus;
import com.emiyaoj.judge.domain.entity.SubmissionCaseResult;
import com.emiyaoj.judge.domain.entity.SubmissionJudgeResult;
import com.emiyaoj.judge.domain.gojudge.GoJudgeResult;
import com.emiyaoj.judge.domain.gojudge.GoJudgeStatus;
import com.emiyaoj.problem.dto.TestCaseVO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 判题汇总计算器。
 */
public final class JudgeResultCalculator {

    private static final int ERROR_MESSAGE_LIMIT = 500;

    private JudgeResultCalculator() {
    }

    public static SubmissionJudgeResult buildSummary(Long submissionId,
                                                     List<TestCaseVO> testCases,
                                                     List<SubmissionCaseResult> caseResults,
                                                     Integer forcedStatus,
                                                     String errorMessage,
                                                     String compileMessage) {
        SubmissionJudgeResult result = new SubmissionJudgeResult();
        result.setSubmissionId(submissionId);
        result.setTotalCaseCount(testCases == null ? 0 : testCases.size());
        result.setPassedCaseCount(countAccepted(caseResults));
        result.setScore(calculateScore(testCases, caseResults));
        result.setMaxTimeUsed(maxTime(caseResults));
        result.setMaxMemoryUsed(maxMemory(caseResults));
        result.setStatus(forcedStatus != null
                ? forcedStatus
                : resolveOutputStatus(result.getPassedCaseCount(), result.getTotalCaseCount()));
        result.setErrorMessage(errorMessage);
        result.setCompileMessage(compileMessage);
        result.setFinishTime(LocalDateTime.now());
        return result;
    }

    public static boolean useWeightedScore(List<TestCaseVO> testCases) {
        return testCases != null && testCases.stream()
                .map(TestCaseVO::getScore)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .filter(score -> score > 0)
                .sum() > 0;
    }

    public static int awardedCaseScore(TestCaseVO testCase, boolean weighted, boolean accepted) {
        if (!accepted) {
            return 0;
        }
        if (!weighted) {
            return 1;
        }
        Integer score = testCase == null ? null : testCase.getScore();
        return score != null && score > 0 ? score : 0;
    }

    public static CaseFailure mapExecutionFailure(GoJudgeResult runResult) {
        if (runResult == null) {
            return new CaseFailure(JudgeStatus.SYSTEM_ERROR, "运行无返回结果");
        }

        GoJudgeStatus goStatus = GoJudgeStatus.fromValue(runResult.getStatus());
        return switch (goStatus) {
            case TIME_LIMIT_EXCEEDED ->
                    new CaseFailure(JudgeStatus.TIME_LIMIT_EXCEEDED, "Time Limit Exceeded");
            case MEMORY_LIMIT_EXCEEDED ->
                    new CaseFailure(JudgeStatus.MEMORY_LIMIT_EXCEEDED, "Memory Limit Exceeded");
            case NONZERO_EXIT_STATUS, SIGNALLED ->
                    new CaseFailure(JudgeStatus.RUNTIME_ERROR, runtimeErrorMessage(runResult));
            case OUTPUT_LIMIT_EXCEEDED ->
                    new CaseFailure(JudgeStatus.OUTPUT_LIMIT_EXCEEDED, "Output Limit Exceeded");
            default ->
                    new CaseFailure(JudgeStatus.SYSTEM_ERROR, runResult.getStatus());
        };
    }

    private static int countAccepted(List<SubmissionCaseResult> caseResults) {
        if (caseResults == null) {
            return 0;
        }
        return (int) caseResults.stream()
                .filter(result -> Objects.equals(result.getStatus(), JudgeStatus.ACCEPTED))
                .count();
    }

    private static int calculateScore(List<TestCaseVO> testCases, List<SubmissionCaseResult> caseResults) {
        if (testCases == null || testCases.isEmpty()) {
            return 0;
        }

        if (!useWeightedScore(testCases)) {
            int accepted = countAccepted(caseResults);
            return (int) Math.round((double) accepted * 100 / testCases.size());
        }

        int totalScore = testCases.stream()
                .map(TestCaseVO::getScore)
                .filter(Objects::nonNull)
                .mapToInt(score -> Math.max(score, 0))
                .sum();
        if (totalScore <= 0) {
            return 0;
        }

        Map<Long, Integer> caseScoreMap = testCases.stream()
                .filter(testCase -> testCase.getId() != null)
                .collect(Collectors.toMap(
                        TestCaseVO::getId,
                        testCase -> Math.max(testCase.getScore() == null ? 0 : testCase.getScore(), 0),
                        Integer::sum
                ));

        int acceptedScore = caseResults == null ? 0 : caseResults.stream()
                .filter(result -> Objects.equals(result.getStatus(), JudgeStatus.ACCEPTED))
                .map(SubmissionCaseResult::getTestCaseId)
                .map(caseScoreMap::get)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        return (int) Math.round((double) acceptedScore * 100 / totalScore);
    }

    private static int resolveOutputStatus(int passedCount, int totalCount) {
        if (totalCount > 0 && passedCount == totalCount) {
            return JudgeStatus.ACCEPTED;
        }
        if (passedCount > 0) {
            return JudgeStatus.PARTIAL_ACCEPTED;
        }
        return JudgeStatus.WRONG_ANSWER;
    }

    private static long maxTime(List<SubmissionCaseResult> caseResults) {
        if (caseResults == null) {
            return 0L;
        }
        return caseResults.stream()
                .map(SubmissionCaseResult::getTimeUsed)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);
    }

    private static long maxMemory(List<SubmissionCaseResult> caseResults) {
        if (caseResults == null) {
            return 0L;
        }
        return caseResults.stream()
                .map(SubmissionCaseResult::getMemoryUsed)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);
    }

    private static String runtimeErrorMessage(GoJudgeResult runResult) {
        String message = "Runtime Error";
        if (runResult.getFiles() != null) {
            String stderr = runResult.getFiles().getOrDefault("stderr", "");
            if (!stderr.isBlank()) {
                message = "Runtime Error: " + stderr.substring(0, Math.min(stderr.length(), ERROR_MESSAGE_LIMIT));
            }
        }
        return message;
    }

    public record CaseFailure(int status, String message) {
    }
}

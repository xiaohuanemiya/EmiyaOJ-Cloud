package com.emiyaoj.judge.service;

import com.emiyaoj.judge.domain.JudgeStatus;
import com.emiyaoj.judge.domain.entity.SubmissionCaseResult;
import com.emiyaoj.judge.domain.entity.SubmissionJudgeResult;
import com.emiyaoj.judge.domain.gojudge.GoJudgeResult;
import com.emiyaoj.judge.domain.gojudge.GoJudgeStatus;
import com.emiyaoj.problem.dto.TestCaseVO;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JudgeResultCalculatorTest {

    @Test
    void buildSummaryReturnsAcceptedWhenAllCasesPass() {
        List<TestCaseVO> testCases = List.of(testCase(1L, 30), testCase(2L, 70));
        List<SubmissionCaseResult> caseResults = List.of(
                caseResult(1L, JudgeStatus.ACCEPTED, 12L, 128L),
                caseResult(2L, JudgeStatus.ACCEPTED, 20L, 96L)
        );

        SubmissionJudgeResult summary = JudgeResultCalculator.buildSummary(100L, testCases, caseResults, null, null, null);

        assertEquals(JudgeStatus.ACCEPTED, summary.getStatus());
        assertEquals(2, summary.getPassedCaseCount());
        assertEquals(2, summary.getTotalCaseCount());
        assertEquals(100, summary.getScore());
        assertEquals(20L, summary.getMaxTimeUsed());
        assertEquals(128L, summary.getMaxMemoryUsed());
    }

    @Test
    void buildSummaryReturnsPartialAcceptedWithWeightedScore() {
        List<TestCaseVO> testCases = List.of(testCase(1L, 30), testCase(2L, 70));
        List<SubmissionCaseResult> caseResults = List.of(
                caseResult(1L, JudgeStatus.ACCEPTED, 12L, 128L),
                caseResult(2L, JudgeStatus.WRONG_ANSWER, 10L, 64L)
        );

        SubmissionJudgeResult summary = JudgeResultCalculator.buildSummary(
                100L, testCases, caseResults, null, "Wrong Answer", null);

        assertEquals(JudgeStatus.PARTIAL_ACCEPTED, summary.getStatus());
        assertEquals(1, summary.getPassedCaseCount());
        assertEquals(30, summary.getScore());
        assertEquals("Wrong Answer", summary.getErrorMessage());
    }

    @Test
    void buildSummaryReturnsCompileErrorWhenForced() {
        SubmissionJudgeResult summary = JudgeResultCalculator.buildSummary(
                100L, List.of(testCase(1L, 100)), List.of(),
                JudgeStatus.COMPILE_ERROR, null, "syntax error");

        assertEquals(JudgeStatus.COMPILE_ERROR, summary.getStatus());
        assertEquals(0, summary.getScore());
        assertEquals("syntax error", summary.getCompileMessage());
    }

    @Test
    void buildSummaryReturnsWrongAnswerWhenNoCasesPass() {
        List<TestCaseVO> testCases = List.of(testCase(1L, 50), testCase(2L, 50));
        List<SubmissionCaseResult> caseResults = List.of(
                caseResult(1L, JudgeStatus.WRONG_ANSWER, 3L, 32L),
                caseResult(2L, JudgeStatus.WRONG_ANSWER, 4L, 48L)
        );

        SubmissionJudgeResult summary = JudgeResultCalculator.buildSummary(
                100L, testCases, caseResults, null, "Wrong Answer", null);

        assertEquals(JudgeStatus.WRONG_ANSWER, summary.getStatus());
        assertEquals(0, summary.getPassedCaseCount());
        assertEquals(0, summary.getScore());
    }

    @Test
    void buildSummaryFallsBackToEqualWeightScore() {
        List<TestCaseVO> testCases = List.of(testCase(1L, null), testCase(2L, null));
        List<SubmissionCaseResult> caseResults = List.of(
                caseResult(1L, JudgeStatus.ACCEPTED, 3L, 32L),
                caseResult(2L, JudgeStatus.WRONG_ANSWER, 4L, 48L)
        );

        SubmissionJudgeResult summary = JudgeResultCalculator.buildSummary(
                100L, testCases, caseResults, null, "Wrong Answer", null);

        assertEquals(JudgeStatus.PARTIAL_ACCEPTED, summary.getStatus());
        assertEquals(50, summary.getScore());
    }

    @Test
    void mapExecutionFailureMapsResourceAndRuntimeErrors() {
        assertEquals(JudgeStatus.TIME_LIMIT_EXCEEDED,
                JudgeResultCalculator.mapExecutionFailure(goResult(GoJudgeStatus.TIME_LIMIT_EXCEEDED, null)).status());
        assertEquals(JudgeStatus.MEMORY_LIMIT_EXCEEDED,
                JudgeResultCalculator.mapExecutionFailure(goResult(GoJudgeStatus.MEMORY_LIMIT_EXCEEDED, null)).status());

        JudgeResultCalculator.CaseFailure runtimeFailure = JudgeResultCalculator.mapExecutionFailure(
                goResult(GoJudgeStatus.NONZERO_EXIT_STATUS, "Exception in thread main"));

        assertEquals(JudgeStatus.RUNTIME_ERROR, runtimeFailure.status());
        assertEquals("Runtime Error: Exception in thread main", runtimeFailure.message());
    }

    private TestCaseVO testCase(Long id, Integer score) {
        TestCaseVO testCase = new TestCaseVO();
        testCase.setId(id);
        testCase.setScore(score);
        return testCase;
    }

    private SubmissionCaseResult caseResult(Long testCaseId, Integer status, Long timeUsed, Long memoryUsed) {
        SubmissionCaseResult result = new SubmissionCaseResult();
        result.setTestCaseId(testCaseId);
        result.setStatus(status);
        result.setTimeUsed(timeUsed);
        result.setMemoryUsed(memoryUsed);
        return result;
    }

    private GoJudgeResult goResult(GoJudgeStatus status, String stderr) {
        GoJudgeResult result = new GoJudgeResult();
        result.setStatus(status.getValue());
        if (stderr != null) {
            result.setFiles(Map.of("stderr", stderr));
        }
        return result;
    }
}

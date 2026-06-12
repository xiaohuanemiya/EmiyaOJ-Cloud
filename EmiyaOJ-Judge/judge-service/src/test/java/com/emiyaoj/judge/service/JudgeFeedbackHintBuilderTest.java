package com.emiyaoj.judge.service;

import com.emiyaoj.judge.domain.entity.SubmissionCaseResult;
import com.emiyaoj.judge.domain.gojudge.GoJudgeResult;
import com.emiyaoj.problem.dto.TestCaseVO;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class JudgeFeedbackHintBuilderTest {

    @Test
    void hiddenCaseDoesNotStoreRawInputOrOutputs() {
        TestCaseVO testCase = new TestCaseVO();
        testCase.setInput("SECRET_INPUT");
        testCase.setOutput("SECRET_EXPECTED");
        testCase.setIsSample(0);

        SubmissionCaseResult result = new SubmissionCaseResult();
        JudgeFeedbackHintBuilder.applyOutputHint(result, testCase, goResult("SECRET_ACTUAL"));

        assertEquals(0, result.getIsSample());
        assertNull(result.getInputPreview());
        assertNull(result.getExpectedOutputPreview());
        assertNull(result.getActualOutputPreview());
        assertFalse(result.getOutputDiffSummary().contains("SECRET_INPUT"));
        assertFalse(result.getOutputDiffSummary().contains("SECRET_EXPECTED"));
        assertFalse(result.getOutputDiffSummary().contains("SECRET_ACTUAL"));
        assertEquals("Hidden case output differs. expectedLength=15, actualLength=13, expectedLines=1, actualLines=1, firstDifferentLine=1",
                result.getOutputDiffSummary());
    }

    @Test
    void sampleCaseStoresTruncatedPreviews() {
        TestCaseVO testCase = new TestCaseVO();
        testCase.setInput("1 2");
        testCase.setOutput("3");
        testCase.setIsSample(1);

        SubmissionCaseResult result = new SubmissionCaseResult();
        JudgeFeedbackHintBuilder.applyOutputHint(result, testCase, goResult("4"));

        assertEquals(1, result.getIsSample());
        assertEquals("1 2", result.getInputPreview());
        assertEquals("3", result.getExpectedOutputPreview());
        assertEquals("4", result.getActualOutputPreview());
        assertEquals("Sample case output differs. expectedLength=1, actualLength=1, expectedLines=1, actualLines=1, firstDifferentLine=1",
                result.getOutputDiffSummary());
    }

    private GoJudgeResult goResult(String stdout) {
        GoJudgeResult result = new GoJudgeResult();
        result.setFiles(Map.of("stdout", stdout));
        return result;
    }
}

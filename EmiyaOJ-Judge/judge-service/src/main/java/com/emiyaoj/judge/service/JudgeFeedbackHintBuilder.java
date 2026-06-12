package com.emiyaoj.judge.service;

import com.emiyaoj.judge.domain.entity.SubmissionCaseResult;
import com.emiyaoj.judge.domain.gojudge.GoJudgeResult;
import com.emiyaoj.problem.dto.TestCaseVO;

import java.util.Objects;

/**
 * Builds safe case-level hints for Agent feedback without leaking hidden tests.
 */
public final class JudgeFeedbackHintBuilder {

    private static final int PREVIEW_LIMIT = 500;
    private static final int SUMMARY_LIMIT = 1000;

    private JudgeFeedbackHintBuilder() {
    }

    public static void applyOutputHint(SubmissionCaseResult result, TestCaseVO testCase, GoJudgeResult runResult) {
        String expected = testCase != null && testCase.getOutput() != null ? testCase.getOutput() : "";
        String actual = extractStdout(runResult);
        boolean sample = testCase != null && Objects.equals(testCase.getIsSample(), 1);

        result.setIsSample(sample ? 1 : 0);
        result.setOutputDiffSummary(buildDiffSummary(sample, expected, actual));
        if (sample) {
            result.setInputPreview(truncate(testCase.getInput()));
            result.setExpectedOutputPreview(truncate(expected));
            result.setActualOutputPreview(truncate(actual));
        } else {
            result.setInputPreview(null);
            result.setExpectedOutputPreview(null);
            result.setActualOutputPreview(null);
        }
    }

    public static String extractStdout(GoJudgeResult runResult) {
        if (runResult == null || runResult.getFiles() == null) {
            return "";
        }
        return runResult.getFiles().getOrDefault("stdout", "");
    }

    static String buildDiffSummary(boolean sample, String expected, String actual) {
        String normalizedExpected = normalize(expected);
        String normalizedActual = normalize(actual);
        if (normalizedExpected.equals(normalizedActual)) {
            return "Output matched";
        }

        int firstDifferentLine = firstDifferentLine(normalizedExpected, normalizedActual);
        String visibility = sample ? "Sample case output differs" : "Hidden case output differs";
        String summary = "%s. expectedLength=%d, actualLength=%d, expectedLines=%d, actualLines=%d, firstDifferentLine=%d"
                .formatted(
                        visibility,
                        normalizedExpected.length(),
                        normalizedActual.length(),
                        lineCount(normalizedExpected),
                        lineCount(normalizedActual),
                        firstDifferentLine
                );
        return truncate(summary, SUMMARY_LIMIT);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private static int firstDifferentLine(String expected, String actual) {
        String[] expectedLines = splitLines(expected);
        String[] actualLines = splitLines(actual);
        int max = Math.max(expectedLines.length, actualLines.length);
        for (int i = 0; i < max; i++) {
            String expectedLine = i < expectedLines.length ? expectedLines[i] : null;
            String actualLine = i < actualLines.length ? actualLines[i] : null;
            if (!Objects.equals(expectedLine, actualLine)) {
                return i + 1;
            }
        }
        return 0;
    }

    private static String[] splitLines(String value) {
        if (value == null || value.isEmpty()) {
            return new String[0];
        }
        return value.split("\\R", -1);
    }

    private static int lineCount(String value) {
        return splitLines(value).length;
    }

    private static String truncate(String value) {
        return truncate(value, PREVIEW_LIMIT);
    }

    private static String truncate(String value, int limit) {
        if (value == null) {
            return null;
        }
        if (value.length() <= limit) {
            return value;
        }
        return value.substring(0, limit) + "...[truncated, totalLength=" + value.length() + "]";
    }
}

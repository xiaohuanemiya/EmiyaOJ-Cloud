package com.emiyaoj.judge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Sanitized context exposed to Judge-Feedback-Agent.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JudgeFeedbackContextVO implements Serializable {

    private Long submissionId;

    private Long problemId;

    private Long userId;

    private Long languageId;

    private Integer status;

    private String statusText;

    private Integer passedCaseCount;

    private Integer totalCaseCount;

    private Integer score;

    private Long maxTimeUsed;

    private Long maxMemoryUsed;

    private String errorMessage;

    private String compileMessage;

    private String code;

    private LocalDateTime finishTime;

    private ProblemInfo problem;

    private List<FailedCaseHint> failedCases;

    private SubmissionHistory history;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProblemInfo implements Serializable {
        private Long id;
        private String title;
        private String description;
        private String inputDescription;
        private String outputDescription;
        private String sampleInput;
        private String sampleOutput;
        private String hint;
        private String difficultyDesc;
        private Integer timeLimit;
        private Integer memoryLimit;
        private List<String> tags;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailedCaseHint implements Serializable {
        private Integer caseOrder;
        private Integer status;
        private String statusText;
        private Integer isSample;
        private Integer score;
        private Long timeUsed;
        private Long memoryUsed;
        private String errorMessage;
        private String inputPreview;
        private String expectedOutputPreview;
        private String actualOutputPreview;
        private String outputDiffSummary;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubmissionHistory implements Serializable {
        private Integer totalAttempts;
        private Integer nonAcceptedAttempts;
        private Boolean acceptedBefore;
        private List<Integer> recentStatuses;
    }
}

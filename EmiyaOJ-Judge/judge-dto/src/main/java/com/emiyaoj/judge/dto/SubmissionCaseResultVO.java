package com.emiyaoj.judge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 测试用例判题明细 VO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionCaseResultVO implements Serializable {

    private Long id;

    private Long submissionId;

    private Long testCaseId;

    private Integer caseOrder;

    private Integer status;

    private Integer score;

    private Long timeUsed;

    private Long memoryUsed;

    private String errorMessage;

    private Integer isSample;

    private String inputPreview;

    private String expectedOutputPreview;

    private String actualOutputPreview;

    private String outputDiffSummary;

    private LocalDateTime createTime;
}

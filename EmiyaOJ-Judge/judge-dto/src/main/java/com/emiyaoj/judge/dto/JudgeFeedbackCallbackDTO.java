package com.emiyaoj.judge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Callback payload used by Agent workers to persist their result.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JudgeFeedbackCallbackDTO implements Serializable {

    private Long submissionId;

    private String agentType;

    private String status;

    private String content;

    private String source;

    private String model;

    private String traceId;

    private String errorMessage;
}

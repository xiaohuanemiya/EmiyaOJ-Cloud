package com.emiyaoj.judge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * User-visible Agent feedback for one submission.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JudgeFeedbackVO implements Serializable {

    private Long id;

    private Long submissionId;

    private String status;

    private String content;

    private String source;

    private String model;

    private String agentType;

    private String traceId;

    private String errorMessage;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

package com.emiyaoj.judge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Generic task envelope published to the Agent platform.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentTaskMessage implements Serializable {

    private String agentType;

    private String taskId;

    private String traceId;

    private Long submissionId;

    private Long problemId;

    private Long userId;

    private Integer status;

    private String createdAt;
}

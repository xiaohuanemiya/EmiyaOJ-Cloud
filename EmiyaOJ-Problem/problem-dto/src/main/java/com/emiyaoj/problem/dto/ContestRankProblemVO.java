package com.emiyaoj.problem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ContestRankProblemVO implements Serializable {

    private Long contestProblemId;

    private Long problemId;

    private String label;

    private Integer score;

    private Boolean accepted;

    private Integer submissionCount;

    private Integer wrongBeforeAccepted;

    private Integer penalty;

    private LocalDateTime lastSubmitTime;
}

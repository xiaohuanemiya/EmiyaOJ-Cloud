package com.emiyaoj.problem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ContestProblemVO implements Serializable {

    private Long id;

    private Long contestId;

    private Long problemId;

    private String label;

    private Integer sortOrder;

    private Integer score;

    private ProblemVO problem;

    private LocalDateTime createTime;
}

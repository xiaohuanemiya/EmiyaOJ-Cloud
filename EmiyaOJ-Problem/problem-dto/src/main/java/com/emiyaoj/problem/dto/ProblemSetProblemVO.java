package com.emiyaoj.problem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ProblemSetProblemVO implements Serializable {

    private Long id;

    private Long setId;

    private Long problemId;

    private Integer sortOrder;

    private String note;

    private ProblemVO problem;

    private LocalDateTime createTime;
}

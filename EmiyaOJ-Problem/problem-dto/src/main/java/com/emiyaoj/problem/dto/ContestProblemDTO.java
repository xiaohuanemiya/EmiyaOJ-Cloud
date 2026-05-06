package com.emiyaoj.problem.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ContestProblemDTO implements Serializable {

    private Long problemId;

    private String label;

    private Integer sortOrder;

    private Integer score;
}

package com.emiyaoj.problem.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ProblemSetProblemDTO implements Serializable {

    private Long problemId;

    private Integer sortOrder;

    private String note;
}

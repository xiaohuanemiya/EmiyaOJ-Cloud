package com.emiyaoj.problem.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ProblemSetSaveDTO implements Serializable {

    private Long id;

    private String title;

    private String description;

    /** 0-hidden, 1-public. */
    private Integer status;

    private List<ProblemSetProblemDTO> problems;
}

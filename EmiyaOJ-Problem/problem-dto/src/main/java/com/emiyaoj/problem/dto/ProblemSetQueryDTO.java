package com.emiyaoj.problem.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ProblemSetQueryDTO implements Serializable {

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    private String title;

    /** 0-hidden, 1-public. */
    private Integer status;

    private Long creatorId;
}

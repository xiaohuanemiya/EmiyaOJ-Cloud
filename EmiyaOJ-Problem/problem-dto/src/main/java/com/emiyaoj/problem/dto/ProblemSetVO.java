package com.emiyaoj.problem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProblemSetVO implements Serializable {

    private Long id;

    private String title;

    private String description;

    private Long creatorId;

    private String creatorNickname;

    /** 0-hidden, 1-public. */
    private Integer status;

    private Integer problemCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private List<ProblemSetProblemVO> problems;
}

package com.emiyaoj.problem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ContestRankUserVO implements Serializable {

    private Integer rank;

    private Long userId;

    private Integer solvedCount;

    private Integer totalScore;

    private Integer penalty;

    private LocalDateTime lastSubmitTime;

    private List<ContestRankProblemVO> problems;
}

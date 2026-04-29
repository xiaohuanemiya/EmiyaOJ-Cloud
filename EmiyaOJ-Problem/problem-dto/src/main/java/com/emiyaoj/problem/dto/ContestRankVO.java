package com.emiyaoj.problem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ContestRankVO implements Serializable {

    private Long contestId;

    /** 1-ACM/ICPC, 2-IOI, 3-Codeforces. */
    private Integer ruleType;

    private Boolean frozen;

    private LocalDateTime freezeTime;

    private List<ContestRankUserVO> rankings;
}

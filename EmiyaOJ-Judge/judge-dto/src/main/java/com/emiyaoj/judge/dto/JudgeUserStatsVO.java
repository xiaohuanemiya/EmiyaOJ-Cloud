package com.emiyaoj.judge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JudgeUserStatsVO implements Serializable {

    private Long userId;

    private Integer solvedCount;

    private Integer totalSubmitCount;

    private Integer acceptedSubmitCount;

    private BigDecimal passRate;

    private List<DifficultySolvedStatsVO> difficultyStats;
}

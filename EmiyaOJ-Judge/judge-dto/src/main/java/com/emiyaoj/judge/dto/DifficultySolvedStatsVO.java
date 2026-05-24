package com.emiyaoj.judge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DifficultySolvedStatsVO implements Serializable {

    private Integer difficulty;

    private String difficultyDesc;

    private Integer solvedCount;
}

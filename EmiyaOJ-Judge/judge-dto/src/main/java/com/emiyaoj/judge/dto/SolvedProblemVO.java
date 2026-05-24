package com.emiyaoj.judge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolvedProblemVO implements Serializable {

    private Long problemId;

    private String title;

    private Integer difficulty;

    private String difficultyDesc;

    private LocalDateTime acceptedAt;
}

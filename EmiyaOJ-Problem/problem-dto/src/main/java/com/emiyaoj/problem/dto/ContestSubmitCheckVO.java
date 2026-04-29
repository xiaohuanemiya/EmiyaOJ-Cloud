package com.emiyaoj.problem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContestSubmitCheckVO implements Serializable {

    private Boolean allowed;

    private Long contestProblemId;

    private String message;
}

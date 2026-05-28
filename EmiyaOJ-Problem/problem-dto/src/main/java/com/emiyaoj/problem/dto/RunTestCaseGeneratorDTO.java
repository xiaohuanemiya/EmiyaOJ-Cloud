package com.emiyaoj.problem.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class RunTestCaseGeneratorDTO implements Serializable {

    /**
     * APPEND by default. Use REPLACE to remove existing test cases before saving generated cases.
     */
    private String saveMode;
}

package com.emiyaoj.judge.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TestCaseGeneratorRunResultVO implements Serializable {

    private Boolean success;

    private String status;

    private String stdout;

    private String stderr;

    private String errorMessage;

    private Long timeUsed;

    private Long memoryUsed;
}

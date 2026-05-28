package com.emiyaoj.problem.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class RunTestCaseGeneratorVO implements Serializable {

    private Long problemId;

    private String saveMode;

    private Integer generatedCount;

    private Integer savedCount;

    private Long timeUsed;

    private Long memoryUsed;

    private List<TestCaseVO> testCases;
}

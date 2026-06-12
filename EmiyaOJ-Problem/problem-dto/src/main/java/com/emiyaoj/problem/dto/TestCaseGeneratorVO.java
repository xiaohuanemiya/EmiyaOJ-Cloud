package com.emiyaoj.problem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class TestCaseGeneratorVO implements Serializable {

    private Long id;

    private Long problemId;

    private String spec;

    private String generatorCode;

    private Long createBy;

    private Long updateBy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

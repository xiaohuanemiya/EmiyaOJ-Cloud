package com.emiyaoj.problem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 编程语言 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LanguageVO implements Serializable {

    private Long id;
    private String name;
    private String version;
    private String compileCommand;
    private String executeCommand;
    private String sourceFileExt;
    private String executableExt;
    private Integer isCompiled;
    private BigDecimal timeLimitMultiplier;
    private BigDecimal memoryLimitMultiplier;
    private Integer status;
}

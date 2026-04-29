package com.emiyaoj.problem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 编程语言配置 VO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LanguageVO implements Serializable {

    private Long id;
    private String name;
    private String version;
    private String languageVersion;
    private String compileFileName;
    private String sourceFileExt;
    private String executableFileName;
    private String compiledFileNames;
    private String compileCommand;
    private String runCommand;
    private String envVars;
    private Integer isCompiled;
    private BigDecimal timeLimitMultiplier;
    private BigDecimal memoryLimitMultiplier;
    private Integer compileTimeLimit;
    private Integer compileMemoryLimit;
    private Integer compileProcLimit;
    private Integer runProcLimit;
    private Integer status;
}

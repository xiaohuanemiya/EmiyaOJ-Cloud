package com.emiyaoj.problem.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 编程语言配置保存/更新 DTO。
 */
@Data
public class LanguageSaveDTO implements Serializable {

    /** 语言 ID，更新时必填 */
    private Long id;

    /** 语言名称，如 Java、C++ */
    private String name;

    /** 展示版本，如 C++20、Java 21 */
    private String version;

    /** 命令模板中的语言版本值，如 c++20、c11 */
    private String languageVersion;

    /** 源文件基础名，不含扩展名，如 main、Main */
    private String compileFileName;

    /** 源文件扩展名，不含点，如 cpp、java、py */
    private String sourceFileExt;

    /** 运行命令中的可执行目标名，如 main、Main、main.py */
    private String executableFileName;

    /** 编译产物文件名，多个用英文逗号分隔；为空时使用 executableFileName */
    private String compiledFileNames;

    /** 编译命令模板 */
    private String compileCommand;

    /** 运行命令模板 */
    private String runCommand;

    /** GoJudge 环境变量，逗号或换行分隔 */
    private String envVars;

    /** 是否需要编译：0-否，1-是 */
    private Integer isCompiled;

    /** 运行 CPU 时间限制倍数，默认 1.0 */
    private BigDecimal timeLimitMultiplier;

    /** 运行内存限制倍数，默认 1.0 */
    private BigDecimal memoryLimitMultiplier;

    /** 编译 CPU 时间限制（毫秒），默认 10000 */
    private Integer compileTimeLimit;

    /** 编译内存限制（MB），默认 512 */
    private Integer compileMemoryLimit;

    /** 编译进程数限制，默认 50 */
    private Integer compileProcLimit;

    /** 运行进程数限制，默认 1 */
    private Integer runProcLimit;

    /** 状态：0-禁用，1-启用，默认 1 */
    private Integer status;
}

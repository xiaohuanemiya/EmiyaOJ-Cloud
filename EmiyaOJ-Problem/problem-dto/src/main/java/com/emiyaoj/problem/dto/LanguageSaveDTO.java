package com.emiyaoj.problem.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 编程语言保存/更新 DTO
 */
@Data
public class LanguageSaveDTO implements Serializable {

    /** 语言 ID（更新时必填） */
    private Long id;

    /** 语言名称，如 Java、C++（新增时必填） */
    private String name;

    /** 版本号，如 17、14 */
    private String version;

    /** 编译命令（{src} 占位源文件，{out} 占位输出文件） */
    private String compileCommand;

    /** 执行命令（{exe} 占位可执行文件） */
    private String executeCommand;

    /** 源文件扩展名，如 java、cpp */
    private String sourceFileExt;

    /** 编译产物扩展名 */
    private String executableExt;

    /** 是否需要编译：0-否，1-是 */
    private Integer isCompiled;

    /** 时间限制乘数，默认 1.0 */
    private BigDecimal timeLimitMultiplier;

    /** 内存限制乘数，默认 1.0 */
    private BigDecimal memoryLimitMultiplier;

    /** 状态：0-禁用，1-启用，默认 1 */
    private Integer status;
}

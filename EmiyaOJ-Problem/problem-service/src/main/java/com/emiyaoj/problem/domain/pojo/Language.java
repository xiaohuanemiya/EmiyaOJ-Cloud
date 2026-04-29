package com.emiyaoj.problem.domain.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 编程语言配置实体。
 */
@Data
@TableName("language")
public class Language implements Serializable {

    @TableId(type = IdType.AUTO)
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

    /** 是否需要编译：0-否，1-是 */
    private Integer isCompiled;

    private BigDecimal timeLimitMultiplier;

    private BigDecimal memoryLimitMultiplier;

    /** 编译 CPU 时间限制（毫秒） */
    private Integer compileTimeLimit;

    /** 编译内存限制（MB） */
    private Integer compileMemoryLimit;

    /** 编译进程数限制 */
    private Integer compileProcLimit;

    /** 运行进程数限制 */
    private Integer runProcLimit;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

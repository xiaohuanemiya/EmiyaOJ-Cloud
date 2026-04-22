package com.emiyaoj.problem.domain.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 编程语言实体
 */
@Data
@TableName("language")
public class Language implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String version;

    private String compileCommand;

    private String executeCommand;

    private String sourceFileExt;

    private String executableExt;

    /** 是否需要编译：0-否，1-是 */
    private Integer isCompiled;

    private BigDecimal timeLimitMultiplier;

    private BigDecimal memoryLimitMultiplier;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

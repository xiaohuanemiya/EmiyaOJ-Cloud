package com.emiyaoj.judge.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 提交记录实体
 */
@Data
@TableName("submission")
public class Submission {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 题目ID */
    private Long problemId;

    /** 用户ID */
    private Long userId;

    /** 编程语言ID */
    private Long languageId;

    /** 提交的代码 */
    private String code;

    /**
     * 判题状态: 0-待判题, 1-判题中, 2-已完成, 3-编译错误, 4-系统错误
     */
    private Integer status;

    /** 得分 (0~100) */
    private Integer score;

    /** 最大运行时间(ms) */
    private Long timeUsed;

    /** 最大运行内存(KB) */
    private Long memoryUsed;

    /** 运行错误信息 */
    private String errorMessage;

    /** 编译错误信息 */
    private String compileMessage;

    /** 通过率 */
    private Double passRate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}

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
     * 判题状态:
     * 0-Pending(待判题), 1-Judging(判题中), 2-AC(通过), 3-CE(编译错误),
     * 4-SE(系统错误), 5-WA(答案错误), 6-TLE(时间超限), 7-MLE(内存超限),
     * 8-RE(运行错误), 9-OLE(输出超限), 10-PA(部分通过)
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

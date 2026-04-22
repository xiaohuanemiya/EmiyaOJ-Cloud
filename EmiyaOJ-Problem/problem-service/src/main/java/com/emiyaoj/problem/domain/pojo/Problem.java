package com.emiyaoj.problem.domain.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 题目实体
 */
@Data
@TableName("problem")
public class Problem implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    private String inputDescription;

    private String outputDescription;

    private String sampleInput;

    private String sampleOutput;

    private String hint;

    /** 难度：1-简单，2-中等，3-困难 */
    private Integer difficulty;

    /** CPU 时间限制（毫秒） */
    private Integer timeLimit;

    /** 内存限制（MB） */
    private Integer memoryLimit;

    /** 栈内存限制（MB） */
    private Integer stackLimit;

    private String source;

    private Long authorId;

    private Integer acceptCount;

    private Integer submitCount;

    /** 状态：0-隐藏，1-公开 */
    private Integer status;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long createBy;

    private Long updateBy;
}

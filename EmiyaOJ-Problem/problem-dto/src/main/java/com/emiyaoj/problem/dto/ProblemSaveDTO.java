package com.emiyaoj.problem.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 题目保存/更新 DTO
 */
@Data
public class ProblemSaveDTO implements Serializable {

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

    /** 题目来源 */
    private String source;

    /** 状态：0-隐藏，1-公开 */
    private Integer status;

    /** 标签 ID 列表 */
    private List<Long> tagIds;
}

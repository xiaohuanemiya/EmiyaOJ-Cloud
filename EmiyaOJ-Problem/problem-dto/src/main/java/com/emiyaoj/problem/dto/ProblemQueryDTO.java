package com.emiyaoj.problem.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 题目查询 DTO
 */
@Data
public class ProblemQueryDTO implements Serializable {

    /** 当前页码 */
    private Integer pageNum = 1;

    /** 每页条数 */
    private Integer pageSize = 10;

    /** 标题关键词 */
    private String title;

    /** 难度：1-简单，2-中等，3-困难 */
    private Integer difficulty;

    /** 标签 ID */
    private Long tagId;

    /** 状态：0-隐藏，1-公开 */
    private Integer status;
}

package com.emiyaoj.problem.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 测试用例保存/更新 DTO
 */
@Data
public class TestCaseSaveDTO implements Serializable {

    /** 测试用例 ID（更新时必填） */
    private Long id;

    /** 关联题目 ID（新增时必填） */
    private Long problemId;

    /** 输入数据 */
    private String input;

    /** 预期输出 */
    private String output;

    /** 是否为样例：0-否，1-是 */
    private Integer isSample;

    /** 分值（OI 模式），默认 0 */
    private Integer score;

    /** 排序权重，默认 0 */
    private Integer sortOrder;
}

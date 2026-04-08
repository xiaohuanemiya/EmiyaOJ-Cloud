package com.emiyaoj.problem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 测试用例 VO（供判题服务使用）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestCaseVO implements Serializable {

    private Long id;

    private Long problemId;

    private String input;

    private String output;

    private Integer isSample;

    private Integer score;

    private Integer sortOrder;
}

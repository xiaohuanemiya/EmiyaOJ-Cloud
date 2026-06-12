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

    /** 输入数据（可为 null，判题时按空标准输入处理） */
    private String input;

    /** 预期输出（可为 null，判题时按空期望输出处理） */
    private String output;

    private Integer isSample;

    private Integer score;

    private Integer sortOrder;
}

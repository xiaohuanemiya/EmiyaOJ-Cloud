package com.emiyaoj.judge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 提交详情 VO，包含汇总结果和测试用例明细。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SubmissionDetailVO extends SubmissionVO implements Serializable {

    private List<SubmissionCaseResultVO> caseResults;

    private JudgeFeedbackVO feedback;
}

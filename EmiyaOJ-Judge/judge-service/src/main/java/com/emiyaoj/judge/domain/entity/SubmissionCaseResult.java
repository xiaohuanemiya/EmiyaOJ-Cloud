package com.emiyaoj.judge.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 单个测试用例的判题明细结果。
 */
@Data
@TableName("submission_case_result")
public class SubmissionCaseResult {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long submissionId;

    private Long testCaseId;

    private Integer caseOrder;

    private Integer status;

    private Integer score;

    private Long timeUsed;

    private Long memoryUsed;

    private String errorMessage;

    private Integer isSample;

    private String inputPreview;

    private String expectedOutputPreview;

    private String actualOutputPreview;

    private String outputDiffSummary;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

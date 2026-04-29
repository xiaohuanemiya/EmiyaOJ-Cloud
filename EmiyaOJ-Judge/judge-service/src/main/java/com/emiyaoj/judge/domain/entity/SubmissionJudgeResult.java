package com.emiyaoj.judge.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 一次提交的判题汇总结果。
 */
@Data
@TableName("submission_judge_result")
public class SubmissionJudgeResult {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long submissionId;

    private Integer status;

    private Integer passedCaseCount;

    private Integer totalCaseCount;

    private Integer score;

    private Long maxTimeUsed;

    private Long maxMemoryUsed;

    private String errorMessage;

    private String compileMessage;

    private LocalDateTime finishTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

package com.emiyaoj.judge.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI Agent feedback generated for a judge submission.
 */
@Data
@TableName("judge_feedback")
public class JudgeFeedback {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long submissionId;

    private String status;

    private String content;

    private String source;

    private String model;

    private String agentType;

    private String traceId;

    private String errorMessage;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

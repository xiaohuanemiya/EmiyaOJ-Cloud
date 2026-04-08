package com.emiyaoj.judge.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 本地消息表 - 分布式事务可靠性保障
 */
@Data
@TableName("message_event")
public class MessageEvent {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 业务类型: JUDGE_SUBMIT */
    private String businessType;

    /** 关联业务ID (如 submissionId) */
    private Long businessId;

    /**
     * 消息状态: 0-待处理, 1-处理中, 2-处理成功, 3-处理失败
     */
    private Integer status;

    /** 已重试次数 */
    private Integer retryCount;

    /** 最大重试次数 */
    private Integer maxRetryCount;

    /** 消息内容 (JSON) */
    private String payload;

    /** 下次重试时间 */
    private LocalDateTime nextRetryTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

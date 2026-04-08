package com.emiyaoj.judge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 判题结果回调 DTO — 判题完成后用于更新提交记录状态
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JudgeResultDTO implements Serializable {

    /** 提交 ID */
    private Long submissionId;

    /** 判题状态 */
    private String status;

    /** 得分 */
    private Integer score;

    /** 最大使用时间（毫秒） */
    private Integer timeUsed;

    /** 最大使用内存（KB） */
    private Integer memoryUsed;

    /** 错误信息 */
    private String errorMessage;

    /** 编译信息 */
    private String compileMessage;

    /** 通过率，如 "8/10" */
    private String passRate;
}

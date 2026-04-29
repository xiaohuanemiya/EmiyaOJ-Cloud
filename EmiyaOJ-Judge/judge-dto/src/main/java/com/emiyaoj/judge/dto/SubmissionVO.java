package com.emiyaoj.judge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 提交记录列表 VO，包含提交元信息与汇总判题结果。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionVO implements Serializable {

    private Long id;

    private Long problemId;

    private Long contestId;

    private Long contestProblemId;

    private Long userId;

    private Long languageId;

    /**
     * 0-Pending, 1-Judging, 2-AC, 3-CE, 4-SE,
     * 5-WA, 6-TLE, 7-MLE, 8-RE, 9-OLE, 10-PA
     */
    private Integer status;

    private Integer passedCaseCount;

    private Integer totalCaseCount;

    private Integer score;

    /** 最高运行时间（毫秒） */
    private Long maxTimeUsed;

    /** 最高运行内存（KB） */
    private Long maxMemoryUsed;

    /** 错误信息 */
    private String errorMessage;

    /** 编译信息 */
    private String compileMessage;

    private LocalDateTime createTime;

    private LocalDateTime finishTime;
}

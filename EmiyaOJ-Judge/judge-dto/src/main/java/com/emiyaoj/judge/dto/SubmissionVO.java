package com.emiyaoj.judge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 提交记录 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionVO implements Serializable {

    private Long id;

    private Long problemId;

    private Long userId;

    private Long languageId;

    /** 判题状态 */
    private String status;

    private Integer score;

    /** 使用时间（毫秒） */
    private Integer timeUsed;

    /** 使用内存（KB） */
    private Integer memoryUsed;

    /** 错误信息 */
    private String errorMessage;

    /** 编译信息 */
    private String compileMessage;

    /** 通过率 */
    private String passRate;

    private LocalDateTime createTime;
}

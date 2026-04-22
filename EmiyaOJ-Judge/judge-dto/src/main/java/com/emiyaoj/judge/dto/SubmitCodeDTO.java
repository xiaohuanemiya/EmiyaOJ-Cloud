package com.emiyaoj.judge.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 提交代码请求 DTO
 */
@Data
public class SubmitCodeDTO implements Serializable {

    /** 题目 ID */
    private Long problemId;

    /** 编程语言 ID */
    private Long languageId;

    /** 源代码 */
    private String code;
}

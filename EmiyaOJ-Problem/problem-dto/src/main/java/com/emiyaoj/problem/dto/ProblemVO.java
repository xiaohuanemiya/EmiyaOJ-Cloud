package com.emiyaoj.problem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 题目详情 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemVO implements Serializable {

    private Long id;

    private String title;

    private String description;

    private String inputDescription;

    private String outputDescription;

    private String sampleInput;

    private String sampleOutput;

    private String hint;

    private Integer difficulty;

    private String difficultyDesc;

    private Integer timeLimit;

    private Integer memoryLimit;

    private Integer stackLimit;

    private String source;

    private Long authorId;

    private Integer acceptCount;

    private Integer submitCount;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /** 关联标签名称 */
    private List<String> tags;
}

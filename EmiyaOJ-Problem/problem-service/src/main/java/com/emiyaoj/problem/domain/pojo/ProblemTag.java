package com.emiyaoj.problem.domain.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 题目-标签 关联实体
 */
@Data
@TableName("problem_tag")
public class ProblemTag implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long problemId;

    private Long tagId;

    private LocalDateTime createTime;
}

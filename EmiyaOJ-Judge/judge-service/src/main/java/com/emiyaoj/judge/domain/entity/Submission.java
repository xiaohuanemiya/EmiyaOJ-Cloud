package com.emiyaoj.judge.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 提交记录实体，只保存提交元信息。
 */
@Data
@TableName("submission")
public class Submission {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 题目ID */
    private Long problemId;

    private Long contestId;

    private Long contestProblemId;

    /** 用户ID */
    private Long userId;

    /** 编程语言ID */
    private Long languageId;

    /** 提交的源代码 */
    private String code;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}

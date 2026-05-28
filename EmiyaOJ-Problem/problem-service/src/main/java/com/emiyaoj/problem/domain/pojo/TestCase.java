package com.emiyaoj.problem.domain.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 测试用例实体
 */
@Data
@TableName("test_case")
public class TestCase implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long problemId;

    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String input;

    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String output;

    /** 是否为样例：0-否，1-是 */
    private Integer isSample;

    private Integer score;

    private Integer sortOrder;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

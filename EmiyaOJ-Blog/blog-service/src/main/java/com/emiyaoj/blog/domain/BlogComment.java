package com.emiyaoj.blog.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 博客评论实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("blog_comment")
public class BlogComment implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long blogId;

    private Long userId;

    private String content;

    private Integer auditStatus;

    private String auditTaskId;

    private String auditReason;

    private String auditLabels;

    private LocalDateTime auditTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}

package com.emiyaoj.blog.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 博客评论查询 DTO
 */
@Data
public class CommentQueryDTO implements Serializable {

    private Long blogId;

    private LocalDateTime fromDay;

    private LocalDateTime toDay;

    private Integer auditStatus;
}

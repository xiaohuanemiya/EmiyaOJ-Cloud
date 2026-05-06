package com.emiyaoj.blog.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 博客评论 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    private String username;

    private String nickname;

    private String content;

    private Integer auditStatus;

    private String auditReason;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

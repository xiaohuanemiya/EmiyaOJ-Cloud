package com.emiyaoj.blog.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 分页条件查博客 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlogQueryDTO implements Serializable {

    @Size(max = 50, message = "标题长度不能超过50个字符")
    private String title;

    /** 0 normal blog, 1 problem solution. */
    private Integer blogType;

    private Long problemId;

    private Long tagId;

    /** createTime, updateTime, viewCount, likeCount. */
    private String sortBy;

    private LocalDateTime createTime;

    private Integer auditStatus;

    @NotNull
    private Integer pageNo;

    @NotNull
    private Integer pageSize;
}

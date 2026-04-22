package com.emiyaoj.blog.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 查询用户发表博客 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserBlogBlogsQueryDTO implements Serializable {

    @NotNull
    private Long userId;

    @NotNull
    private Integer pageNo;

    @NotNull
    private Integer pageSize;
}

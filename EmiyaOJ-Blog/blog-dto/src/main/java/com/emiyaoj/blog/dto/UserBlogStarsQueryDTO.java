package com.emiyaoj.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 查询用户收藏博客 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserBlogStarsQueryDTO implements Serializable {

    private Long userId;

    private Integer pageNo;

    private Integer pageSize;
}

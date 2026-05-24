package com.emiyaoj.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserBlogLikesQueryDTO implements Serializable {

    private Long userId;

    private Integer pageNo;

    private Integer pageSize;
}

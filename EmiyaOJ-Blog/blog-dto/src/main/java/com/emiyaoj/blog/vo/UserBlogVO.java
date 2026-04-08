package com.emiyaoj.blog.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户博客信息 VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserBlogVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    private String username;

    private String nickname;

    private Integer blogCount;

    private Integer starCount;
}

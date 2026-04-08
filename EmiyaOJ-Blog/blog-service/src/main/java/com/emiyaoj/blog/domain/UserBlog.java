package com.emiyaoj.blog.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户博客信息实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("user_blog")
public class UserBlog implements Serializable {

    public UserBlog(Long userId) {
        this.userId = userId;
        this.username = "";
        this.nickname = "";
        this.blogCount = this.starCount = 0;
        this.createTime = LocalDateTime.now();
    }

    @TableId(value = "user_id", type = IdType.INPUT)
    private Long userId;

    private String username;

    private String nickname;

    private Integer blogCount;

    private Integer starCount;

    private LocalDateTime createTime;
}

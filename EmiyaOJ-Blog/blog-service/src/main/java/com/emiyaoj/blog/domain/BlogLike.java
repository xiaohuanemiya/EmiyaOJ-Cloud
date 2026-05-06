package com.emiyaoj.blog.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Blog like relation.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("blog_like")
public class BlogLike implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long blogId;

    private LocalDateTime createTime;

    private Integer deleted;
}

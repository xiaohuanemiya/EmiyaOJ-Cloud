package com.emiyaoj.blog.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 博客图片实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("blog_picture")
public class BlogPicture implements Serializable {

    @TableId("url")
    private String url;

    private Integer deleted;
}

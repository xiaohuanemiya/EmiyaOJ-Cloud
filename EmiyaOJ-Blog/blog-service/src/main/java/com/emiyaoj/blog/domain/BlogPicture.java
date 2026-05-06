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
 * Blog image metadata.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("blog_picture")
public class BlogPicture implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long blogId;

    private String objectName;

    private String url;

    private String contentType;

    private Long size;

    private String originalFilename;

    private LocalDateTime createTime;

    private Integer deleted;
}

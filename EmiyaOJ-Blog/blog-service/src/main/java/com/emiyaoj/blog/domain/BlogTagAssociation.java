package com.emiyaoj.blog.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 博客标签关联实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@TableName("blog_tag_association")
public class BlogTagAssociation implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long blogId;

    private Long tagId;
}

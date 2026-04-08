package com.emiyaoj.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 修改博客基本信息 DTO
 */
@Data
public class BlogEditDTO implements Serializable {

    /** 博客id */
    private Long id;

    @NotBlank(message = "标题不能为空")
    @Size(max = 50, message = "标题长度不能超过50个字符")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Size(max = 10000, message = "内容长度不能超过10000个字符")
    private String content;
}

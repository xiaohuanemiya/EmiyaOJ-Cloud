package com.emiyaoj.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 博客标签保存/更新 DTO
 */
@Data
public class BlogTagSaveDTO implements Serializable {

    /** 标签 ID（更新时使用） */
    private Long id;

    @NotBlank(message = "标签名称不能为空")
    @Size(max = 255, message = "标签名称长度不能超过255个字符")
    private String name;

    @NotBlank(message = "标签描述不能为空")
    @Size(max = 255, message = "标签描述长度不能超过255个字符")
    private String desc;
}

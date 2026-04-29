package com.emiyaoj.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 用户发布博客 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlogSaveDTO implements Serializable {

    @NotBlank(message = "标题不能为空")
    @Size(max = 50, message = "标题长度不能超过50")
    private String title;

    @NotBlank
    @Size(max = 10000, message = "内容长度不能超过10000")
    private String content;

    private List<Long> tagIds;
}

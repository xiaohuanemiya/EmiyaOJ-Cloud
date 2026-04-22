package com.emiyaoj.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 发表评论 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlogCommentSaveDTO implements Serializable {

    @NotBlank
    @Size(max = 200, message = "评论长度不能超过200")
    private String content;
}

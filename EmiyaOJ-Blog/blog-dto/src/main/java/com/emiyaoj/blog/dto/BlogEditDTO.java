package com.emiyaoj.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Blog edit DTO.
 */
@Data
public class BlogEditDTO implements Serializable {

    private Long id;

    @NotBlank(message = "title must not be blank")
    @Size(max = 50, message = "title length must not exceed 50")
    private String title;

    @NotBlank(message = "content must not be blank")
    @Size(max = 10000, message = "content length must not exceed 10000")
    private String content;

    private List<Long> pictureIds;
}

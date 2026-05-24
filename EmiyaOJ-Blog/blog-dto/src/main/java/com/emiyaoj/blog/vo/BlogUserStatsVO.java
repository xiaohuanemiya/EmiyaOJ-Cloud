package com.emiyaoj.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlogUserStatsVO implements Serializable {

    private Long userId;

    private Integer blogCount;

    private Integer starCount;

    private Integer likedBlogCount;
}

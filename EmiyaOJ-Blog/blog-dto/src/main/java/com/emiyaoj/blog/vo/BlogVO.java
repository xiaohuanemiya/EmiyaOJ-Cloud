package com.emiyaoj.blog.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 博客 VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlogVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    private String title;

    private String content;

    private Integer blogType;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long problemId;

    private String problemTitle;

    private Integer viewCount;

    private Integer likeCount;

    private Boolean liked;

    private Integer auditStatus;

    private String auditReason;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private List<BlogTagVO> tags;

    private List<BlogPictureVO> pictures;
}

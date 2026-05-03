package com.emiyaoj.blog.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Blog image metadata for frontend use.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlogPictureVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long blogId;

    private String url;

    private String contentType;

    private Long size;

    private String originalFilename;

    private LocalDateTime createTime;
}

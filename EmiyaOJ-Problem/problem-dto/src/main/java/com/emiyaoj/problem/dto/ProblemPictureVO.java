package com.emiyaoj.problem.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Problem image metadata for frontend use.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProblemPictureVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long problemId;

    private String url;

    private String contentType;

    private Long size;

    private String originalFilename;

    private LocalDateTime createTime;
}

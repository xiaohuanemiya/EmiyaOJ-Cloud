package com.emiyaoj.problem.domain.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Problem image metadata.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("problem_picture")
public class ProblemPicture implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long problemId;

    private String objectName;

    private String url;

    private String contentType;

    private Long size;

    private String originalFilename;

    private LocalDateTime createTime;

    private Integer deleted;
}

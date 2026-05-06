package com.emiyaoj.problem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ContestRegistrationVO implements Serializable {

    private Long id;

    private Long contestId;

    private Long userId;

    private LocalDateTime createTime;
}

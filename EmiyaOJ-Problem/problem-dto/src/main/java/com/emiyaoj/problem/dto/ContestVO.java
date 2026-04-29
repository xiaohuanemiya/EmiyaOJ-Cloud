package com.emiyaoj.problem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ContestVO implements Serializable {

    private Long id;

    private String title;

    private String description;

    /** 1-ACM/ICPC, 2-IOI, 3-Codeforces. */
    private Integer ruleType;

    private String ruleTypeDesc;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer freezeBeforeMinutes;

    private String inviteCode;

    /** 0-draft, 1-published, 2-cancelled. */
    private Integer status;

    private Long creatorId;

    private Boolean registered;

    private Boolean admin;

    private Integer registrationCount;

    private List<Long> adminUserIds;

    private List<ContestProblemVO> problems;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

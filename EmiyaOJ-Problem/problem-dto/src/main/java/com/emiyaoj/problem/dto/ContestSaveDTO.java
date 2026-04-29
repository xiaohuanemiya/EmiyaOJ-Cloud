package com.emiyaoj.problem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ContestSaveDTO implements Serializable {

    private Long id;

    private String title;

    private String description;

    /** 1-ACM/ICPC, 2-IOI, 3-Codeforces. */
    private Integer ruleType;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer freezeBeforeMinutes;

    private String inviteCode;

    /** 0-draft, 1-published, 2-cancelled. */
    private Integer status;

    private List<ContestProblemDTO> problems;
}

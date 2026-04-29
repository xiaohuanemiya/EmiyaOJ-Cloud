package com.emiyaoj.problem.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ContestQueryDTO implements Serializable {

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    private String title;

    /** 1-ACM/ICPC, 2-IOI, 3-Codeforces. */
    private Integer ruleType;

    /** 0-draft, 1-published, 2-cancelled. */
    private Integer status;

    private LocalDateTime startFrom;

    private LocalDateTime startTo;
}

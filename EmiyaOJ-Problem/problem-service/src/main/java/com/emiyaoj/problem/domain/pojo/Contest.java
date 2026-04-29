package com.emiyaoj.problem.domain.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("contest")
public class Contest implements Serializable {

    @TableId(type = IdType.AUTO)
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

    private Long creatorId;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long createBy;

    private Long updateBy;
}

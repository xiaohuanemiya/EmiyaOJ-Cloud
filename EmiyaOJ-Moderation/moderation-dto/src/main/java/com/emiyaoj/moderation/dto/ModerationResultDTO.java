package com.emiyaoj.moderation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModerationResultDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String taskId;

    private ModerationTargetType targetType;

    private Long targetId;

    private Integer auditStatus;

    private String suggestion;

    private String labels;

    private String reason;

    private LocalDateTime auditTime;
}

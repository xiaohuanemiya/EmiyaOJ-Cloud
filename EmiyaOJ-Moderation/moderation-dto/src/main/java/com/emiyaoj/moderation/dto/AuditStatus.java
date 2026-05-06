package com.emiyaoj.moderation.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuditStatus {

    PENDING(0),
    APPROVED(1),
    REJECTED(2),
    MANUAL_REVIEW(3);

    private final int code;

    public static AuditStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AuditStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}

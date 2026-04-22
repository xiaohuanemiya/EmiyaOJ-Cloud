package com.emiyaoj.judge.domain.gojudge;

/**
 * GoJudge 运行状态枚举
 */
public enum GoJudgeStatus {

    /** 正常完成 */
    ACCEPTED("Accepted"),

    /** 内存超限 */
    MEMORY_LIMIT_EXCEEDED("Memory Limit Exceeded"),

    /** 时间超限 */
    TIME_LIMIT_EXCEEDED("Time Limit Exceeded"),

    /** 输出超限 */
    OUTPUT_LIMIT_EXCEEDED("Output Limit Exceeded"),

    /** 运行时错误 (非零退出码) */
    NONZERO_EXIT_STATUS("Nonzero Exit Status"),

    /** 被信号终止 */
    SIGNALLED("Signalled"),

    /** 内部错误 */
    INTERNAL_ERROR("Internal Error");

    private final String value;

    GoJudgeStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static GoJudgeStatus fromValue(String value) {
        for (GoJudgeStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        return INTERNAL_ERROR;
    }
}

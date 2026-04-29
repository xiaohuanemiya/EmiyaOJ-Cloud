package com.emiyaoj.judge.domain;

/**
 * 判题状态码。
 */
public final class JudgeStatus {

    public static final int PENDING = 0;
    public static final int JUDGING = 1;
    public static final int ACCEPTED = 2;
    public static final int COMPILE_ERROR = 3;
    public static final int SYSTEM_ERROR = 4;
    public static final int WRONG_ANSWER = 5;
    public static final int TIME_LIMIT_EXCEEDED = 6;
    public static final int MEMORY_LIMIT_EXCEEDED = 7;
    public static final int RUNTIME_ERROR = 8;
    public static final int OUTPUT_LIMIT_EXCEEDED = 9;
    public static final int PARTIAL_ACCEPTED = 10;

    private JudgeStatus() {
    }
}

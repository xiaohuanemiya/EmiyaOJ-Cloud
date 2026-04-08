package com.emiyaoj.common.exception;

/**
 * 基础业务异常
 */
public class BaseException extends RuntimeException {

    private final int code;

    public BaseException(String message) {
        super(message);
        this.code = 500;
    }

    public BaseException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

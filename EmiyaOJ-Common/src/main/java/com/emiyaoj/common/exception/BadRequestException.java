package com.emiyaoj.common.exception;

/**
 * 请求参数异常（HTTP 400）
 */
public class BadRequestException extends BaseException {

    public BadRequestException(String message) {
        super(400, message);
    }
}

package com.emiyaoj.common.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * 自定义认证异常
 */
public class CustomerAuthenticationException extends AuthenticationException {

    public CustomerAuthenticationException(String message) {
        super(message);
    }
}

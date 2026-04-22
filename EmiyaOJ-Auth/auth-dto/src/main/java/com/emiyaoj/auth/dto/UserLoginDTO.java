package com.emiyaoj.auth.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录请求 DTO
 */
@Data
public class UserLoginDTO implements Serializable {

    /** 用户名 */
    private String username;

    /** 密码 */
    private String password;
}

package com.emiyaoj.common.constant;

/**
 * JWT Claims 常量
 */
public class JwtClaimsConstant {

    private JwtClaimsConstant() {
    }

    /** 存储在 JWT 中的用户 ID */
    public static final String USER_ID = "userId";

    /** 存储在 JWT 中的用户名 */
    public static final String USERNAME = "username";

    /** 存储在 JWT 中的权限列表 */
    public static final String PERMISSIONS = "permissions";
}

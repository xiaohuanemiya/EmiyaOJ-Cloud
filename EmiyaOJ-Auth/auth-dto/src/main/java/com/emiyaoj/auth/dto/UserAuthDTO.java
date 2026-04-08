package com.emiyaoj.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 用户认证信息 DTO — 用于网关解析 Token 后返回给下游服务的用户信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthDTO implements Serializable {

    /** 用户 ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 权限编码列表 */
    private List<String> permissions;
}

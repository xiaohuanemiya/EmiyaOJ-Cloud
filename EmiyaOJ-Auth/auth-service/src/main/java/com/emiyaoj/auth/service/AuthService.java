package com.emiyaoj.auth.service;

import com.emiyaoj.auth.domain.LoginUser;
import com.emiyaoj.auth.dto.UserAuthDTO;
import com.emiyaoj.auth.dto.UserLoginDTO;
import com.emiyaoj.auth.dto.UserLoginVO;
import com.emiyaoj.common.constant.JwtClaimsConstant;
import com.emiyaoj.common.properties.JwtProperties;
import com.emiyaoj.common.utils.JwtUtil;
import com.emiyaoj.common.utils.RedisUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 认证服务 — 登录、登出、Token 解析
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtProperties jwtProperties;
    private final RedisUtil redisUtil;

    /**
     * 用户登录
     */
    public UserLoginVO login(UserLoginDTO loginDTO) {
        // 1. 使用 Spring Security 进行身份校验
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword());
        Authentication authentication = authenticationManager.authenticate(authToken);

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 2. 获取认证后的用户信息
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        if (loginUser.getUser().getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        Long userId = loginUser.getUser().getId();
        String username = loginUser.getUser().getUsername();
        List<String> permissions = loginUser.getPermissions();

        log.info("用户 {} 登录成功", username);

        // 3. 生成 JWT Token（载荷仅存 userId、username、permissions，不再存整个对象的 JSON）
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, userId);
        claims.put(JwtClaimsConstant.USERNAME, username);
        claims.put(JwtClaimsConstant.PERMISSIONS, permissions);

        String token = JwtUtil.createJWT(jwtProperties.getSecretKey(), jwtProperties.getTtl(), claims);

        // 4. Token 白名单存入 Redis（key: token_{userId}）
        String tokenKey = "token_" + userId;
        redisUtil.set(tokenKey, token, jwtProperties.getTtl());

        // 5. 构建返回对象
        return UserLoginVO.builder()
                .id(userId)
                .username(username)
                .nickname(loginUser.getUser().getNickname())
                .token(token)
                .build();
    }

    /**
     * 用户登出
     */
    public void logout(Long userId) {
        if (userId != null) {
            redisUtil.delete("token_" + userId);
            log.info("用户 {} 退出登录", userId);
        }
    }

    /**
     * 解析 Token 并返回用户认证信息
     */
    @SuppressWarnings("unchecked")
    public UserAuthDTO parseToken(String token) {
        Claims claims = JwtUtil.parseJWT(jwtProperties.getSecretKey(), token);

        Long userId = claims.get(JwtClaimsConstant.USER_ID, Long.class);
        String username = claims.get(JwtClaimsConstant.USERNAME, String.class);
        List<String> permissions = claims.get(JwtClaimsConstant.PERMISSIONS, List.class);

        // 验证 Redis 白名单
        String tokenKey = "token_" + userId;
        if (!redisUtil.hasKey(tokenKey)) {
            throw new RuntimeException("Token 已过期或已注销");
        }

        // 刷新 Token 有效期
        redisUtil.expire(tokenKey, jwtProperties.getTtl());

        return UserAuthDTO.builder()
                .userId(userId)
                .username(username)
                .permissions(permissions != null ? permissions : new ArrayList<>())
                .build();
    }
}

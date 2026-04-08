package com.emiyaoj.auth.controller;

import com.emiyaoj.auth.dto.UserAuthDTO;
import com.emiyaoj.auth.dto.UserLoginDTO;
import com.emiyaoj.auth.dto.UserLoginVO;
import com.emiyaoj.auth.service.AuthService;
import com.emiyaoj.common.domain.ResponseResult;
import com.emiyaoj.common.utils.BaseContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器 — 处理登录、登出、Token 解析
 */
@Tag(name = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public ResponseResult<UserLoginVO> login(@RequestBody UserLoginDTO loginDTO) {
        log.info("登录请求: {}", loginDTO.getUsername());
        UserLoginVO loginVO = authService.login(loginDTO);
        return ResponseResult.success(loginVO);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    public ResponseResult<?> logout(@RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        // 优先从网关传递的 X-User-Id 获取，其次从 ThreadLocal 获取
        Long userId = headerUserId != null ? headerUserId : BaseContext.getCurrentId();
        authService.logout(userId);
        BaseContext.remove();
        return ResponseResult.success("登出成功");
    }

    /**
     * 解析 Token — 供网关和其他微服务 Feign 调用
     */
    @GetMapping("/user/parse-token")
    @Operation(summary = "解析Token获取用户信息")
    public ResponseResult<UserAuthDTO> parseToken(@RequestParam("token") String token) {
        try {
            UserAuthDTO userAuthDTO = authService.parseToken(token);
            return ResponseResult.success(userAuthDTO);
        } catch (Exception e) {
            log.warn("Token 解析失败: {}", e.getMessage());
            return ResponseResult.fail(401, "Token 无效或已过期");
        }
    }
}

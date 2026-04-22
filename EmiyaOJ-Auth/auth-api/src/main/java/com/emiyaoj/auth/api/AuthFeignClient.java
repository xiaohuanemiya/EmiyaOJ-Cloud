package com.emiyaoj.auth.api;

import com.emiyaoj.auth.dto.UserAuthDTO;
import com.emiyaoj.common.domain.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 认证服务 Feign 客户端 — 供网关和其他服务调用
 */
@FeignClient(name = "auth-service", path = "/auth")
public interface AuthFeignClient {

    /**
     * 解析 Token，返回用户认证信息
     *
     * @param token JWT Token（不含 Bearer 前缀）
     * @return 用户认证信息
     */
    @GetMapping("/user/parse-token")
    ResponseResult<UserAuthDTO> parseToken(@RequestParam("token") String token);
}

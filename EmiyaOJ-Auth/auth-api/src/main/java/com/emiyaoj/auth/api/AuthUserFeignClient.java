package com.emiyaoj.auth.api;

import com.emiyaoj.auth.vo.UserVO;
import com.emiyaoj.common.domain.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(contextId = "authUserFeignClient", name = "auth-service", path = "/user")
public interface AuthUserFeignClient {

    @GetMapping("/permission-users")
    ResponseResult<List<UserVO>> listUsersByPermission(@RequestParam("permissionCode") String permissionCode);

    @GetMapping("/{id}/has-permission")
    ResponseResult<Boolean> hasPermission(@PathVariable("id") Long id,
                                          @RequestParam("permissionCode") String permissionCode);
}

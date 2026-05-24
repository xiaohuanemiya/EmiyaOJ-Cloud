package com.emiyaoj.auth.controller;

import com.emiyaoj.auth.service.ProfileCenterService;
import com.emiyaoj.auth.vo.ProfileCenterVO;
import com.emiyaoj.common.domain.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "个人中心")
@RestController
@RequestMapping("/user/center")
@RequiredArgsConstructor
public class ProfileCenterController {

    private final ProfileCenterService profileCenterService;

    @Operation(summary = "查询当前用户个人中心总览")
    @GetMapping("/me")
    public ResponseResult<ProfileCenterVO> me(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        ProfileCenterVO vo = profileCenterService.getProfileCenter(userId);
        return vo == null ? ResponseResult.fail(404, "用户不存在") : ResponseResult.success(vo);
    }

    @Operation(summary = "查询指定用户公开个人中心总览")
    @GetMapping("/{userId}")
    public ResponseResult<ProfileCenterVO> getByUserId(@PathVariable Long userId) {
        ProfileCenterVO vo = profileCenterService.getProfileCenter(userId);
        return vo == null ? ResponseResult.fail(404, "用户不存在") : ResponseResult.success(vo);
    }
}

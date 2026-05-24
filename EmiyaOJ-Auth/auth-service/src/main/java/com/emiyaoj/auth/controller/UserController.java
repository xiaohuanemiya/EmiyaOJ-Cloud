package com.emiyaoj.auth.controller;

import com.emiyaoj.auth.dto.UserSaveDTO;
import com.emiyaoj.auth.service.IUserService;
import com.emiyaoj.auth.vo.UserVO;
import com.emiyaoj.common.domain.PageDTO;
import com.emiyaoj.common.domain.PageVO;
import com.emiyaoj.common.domain.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    /**
     * 分页查询用户列表
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询用户列表")
    public ResponseResult<PageVO<UserVO>> page(@RequestBody PageDTO pageDTO) {
        return ResponseResult.success(userService.selectUserPage(pageDTO));
    }

    @GetMapping("/batch")
    @Operation(summary = "批量查询用户")
    public ResponseResult<List<UserVO>> batch(@RequestParam List<Long> ids) {
        return ResponseResult.success(userService.selectUsersByIds(ids));
    }

    /**
     * 根据ID查询用户详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询用户详情")
    public ResponseResult<UserVO> getById(@Parameter(description = "用户ID") @PathVariable Long id) {
        UserVO user = userService.selectUserById(id);
        if (user == null) {
            return ResponseResult.fail("用户不存在");
        }
        return ResponseResult.success(user);
    }

    /**
     * 新增用户
     */
    @PostMapping
    @Operation(summary = "新增用户")
    public ResponseResult<Void> save(@Valid @RequestBody UserSaveDTO saveDTO) {
        userService.saveUser(saveDTO);
        return ResponseResult.success();
    }

    /**
     * 修改用户
     */
    @PutMapping
    @Operation(summary = "修改用户")
    public ResponseResult<Void> update(@Valid @RequestBody UserSaveDTO saveDTO) {
        userService.updateUser(saveDTO);
        return ResponseResult.success();
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    public ResponseResult<Void> delete(@Parameter(description = "用户ID") @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseResult.success();
    }

    /**
     * 批量删除用户
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除用户")
    public ResponseResult<Void> deleteBatch(@RequestBody List<Long> ids) {
        userService.deleteUsers(ids);
        return ResponseResult.success();
    }

    /**
     * 重置用户密码
     */
    @PutMapping("/{id}/reset-password")
    @Operation(summary = "重置用户密码")
    public ResponseResult<Void> resetPassword(@Parameter(description = "用户ID") @PathVariable Long id) {
        userService.resetPassword(id, "123456");
        return ResponseResult.success();
    }

    /**
     * 更新用户状态
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "更新用户状态")
    public ResponseResult<Void> updateStatus(@Parameter(description = "用户ID") @PathVariable Long id,
                                              @Parameter(description = "状态值") @RequestParam Integer status) {
        userService.updateUserStatus(id, status);
        return ResponseResult.success();
    }

    /**
     * 为用户分配角色
     */
    @PutMapping("/{id}/roles")
    @Operation(summary = "为用户分配角色")
    public ResponseResult<Void> assignRoles(@Parameter(description = "用户ID") @PathVariable Long id,
                                             @RequestBody List<Long> roleIds) {
        userService.assignRoles(id, roleIds);
        return ResponseResult.success();
    }

    /**
     * 获取用户权限列表
     */
    @GetMapping("/{id}/permissions")
    @Operation(summary = "获取用户权限列表")
    public ResponseResult<List<String>> getUserPermissions(@Parameter(description = "用户ID") @PathVariable Long id) {
        return ResponseResult.success(userService.getUserPermissions(id));
    }

    /**
     * 判断用户是否拥有某个权限
     */
    @GetMapping("/{id}/has-permission")
    @Operation(summary = "判断用户是否拥有某个权限")
    public ResponseResult<Boolean> hasPermission(@Parameter(description = "用户ID") @PathVariable Long id,
                                                  @RequestParam String permissionCode) {
        return ResponseResult.success(userService.hasPermission(id, permissionCode));
    }

    @GetMapping("/permission-users")
    @Operation(summary = "根据权限编码查询用户")
    public ResponseResult<List<UserVO>> listUsersByPermission(@RequestParam String permissionCode) {
        return ResponseResult.success(userService.listUsersByPermission(permissionCode));
    }

    /**
     * 判断用户是否拥有某个角色
     */
    @GetMapping("/{id}/has-role")
    @Operation(summary = "判断用户是否拥有某个角色")
    public ResponseResult<Boolean> hasRole(@Parameter(description = "用户ID") @PathVariable Long id,
                                           @RequestParam String roleCode) {
        return ResponseResult.success(userService.hasRole(id, roleCode));
    }
}

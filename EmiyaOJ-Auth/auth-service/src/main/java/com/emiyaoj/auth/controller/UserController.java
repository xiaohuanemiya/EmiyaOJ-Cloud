package com.emiyaoj.auth.controller;

import com.emiyaoj.auth.dto.UserSaveDTO;
import com.emiyaoj.auth.service.IUserService;
import com.emiyaoj.auth.vo.UserVO;
import com.emiyaoj.common.domain.PageDTO;
import com.emiyaoj.common.domain.PageVO;
import com.emiyaoj.common.domain.ResponseResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    /**
     * 分页查询用户列表
     */
    @PostMapping("/page")
    public ResponseResult<PageVO<UserVO>> page(@RequestBody PageDTO pageDTO) {
        return ResponseResult.success(userService.selectUserPage(pageDTO));
    }

    /**
     * 根据ID查询用户详情
     */
    @GetMapping("/{id}")
    public ResponseResult<UserVO> getById(@PathVariable Long id) {
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
    public ResponseResult<Void> save(@Valid @RequestBody UserSaveDTO saveDTO) {
        userService.saveUser(saveDTO);
        return ResponseResult.success();
    }

    /**
     * 修改用户
     */
    @PutMapping
    public ResponseResult<Void> update(@Valid @RequestBody UserSaveDTO saveDTO) {
        userService.updateUser(saveDTO);
        return ResponseResult.success();
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseResult.success();
    }

    /**
     * 批量删除用户
     */
    @DeleteMapping("/batch")
    public ResponseResult<Void> deleteBatch(@RequestBody List<Long> ids) {
        userService.deleteUsers(ids);
        return ResponseResult.success();
    }

    /**
     * 重置用户密码
     */
    @PutMapping("/{id}/reset-password")
    public ResponseResult<Void> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return ResponseResult.success();
    }

    /**
     * 更新用户状态
     */
    @PutMapping("/{id}/status")
    public ResponseResult<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateUserStatus(id, status);
        return ResponseResult.success();
    }

    /**
     * 为用户分配角色
     */
    @PutMapping("/{id}/roles")
    public ResponseResult<Void> assignRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        userService.assignRoles(id, roleIds);
        return ResponseResult.success();
    }

    /**
     * 获取用户权限列表
     */
    @GetMapping("/{id}/permissions")
    public ResponseResult<List<String>> getUserPermissions(@PathVariable Long id) {
        return ResponseResult.success(userService.getUserPermissions(id));
    }

    /**
     * 判断用户是否拥有某个权限
     */
    @GetMapping("/{id}/has-permission")
    public ResponseResult<Boolean> hasPermission(@PathVariable Long id,
                                                  @RequestParam String permissionCode) {
        return ResponseResult.success(userService.hasPermission(id, permissionCode));
    }

    /**
     * 判断用户是否拥有某个角色
     */
    @GetMapping("/{id}/has-role")
    public ResponseResult<Boolean> hasRole(@PathVariable Long id,
                                           @RequestParam String roleCode) {
        return ResponseResult.success(userService.hasRole(id, roleCode));
    }
}

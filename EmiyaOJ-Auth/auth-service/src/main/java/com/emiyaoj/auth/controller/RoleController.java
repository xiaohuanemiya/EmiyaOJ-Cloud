package com.emiyaoj.auth.controller;

import com.emiyaoj.auth.dto.RoleQueryDTO;
import com.emiyaoj.auth.dto.RoleSaveDTO;
import com.emiyaoj.auth.service.IRoleService;
import com.emiyaoj.auth.vo.RoleVO;
import com.emiyaoj.common.domain.PageVO;
import com.emiyaoj.common.domain.ResponseResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 */
@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
public class RoleController {

    private final IRoleService roleService;

    /**
     * 分页查询角色列表
     */
    @PostMapping("/page")
    public ResponseResult<PageVO<RoleVO>> page(@RequestBody RoleQueryDTO queryDTO) {
        return ResponseResult.success(roleService.selectRolePage(queryDTO));
    }

    /**
     * 查询所有角色（下拉选择用）
     */
    @GetMapping("/list")
    public ResponseResult<List<RoleVO>> list() {
        return ResponseResult.success(roleService.selectAllRoles());
    }

    /**
     * 根据ID查询角色详情
     */
    @GetMapping("/{id}")
    public ResponseResult<RoleVO> getById(@PathVariable Long id) {
        RoleVO role = roleService.selectRoleById(id);
        if (role == null) {
            return ResponseResult.fail("角色不存在");
        }
        return ResponseResult.success(role);
    }

    /**
     * 新增角色
     */
    @PostMapping
    public ResponseResult<Void> save(@Valid @RequestBody RoleSaveDTO saveDTO) {
        roleService.saveRole(saveDTO);
        return ResponseResult.success();
    }

    /**
     * 修改角色
     */
    @PutMapping
    public ResponseResult<Void> update(@Valid @RequestBody RoleSaveDTO saveDTO) {
        roleService.updateRole(saveDTO);
        return ResponseResult.success();
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseResult.success();
    }

    /**
     * 批量删除角色
     */
    @DeleteMapping("/batch")
    public ResponseResult<Void> deleteBatch(@RequestBody List<Long> ids) {
        roleService.deleteRoles(ids);
        return ResponseResult.success();
    }

    /**
     * 更新角色状态
     */
    @PutMapping("/{id}/status")
    public ResponseResult<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        roleService.updateRoleStatus(id, status);
        return ResponseResult.success();
    }

    /**
     * 为角色分配权限
     */
    @PutMapping("/{id}/permissions")
    public ResponseResult<Void> assignPermissions(@PathVariable Long id,
                                                   @RequestBody List<Long> permissionIds) {
        roleService.assignPermissions(id, permissionIds);
        return ResponseResult.success();
    }

    /**
     * 获取角色已分配的权限ID列表
     */
    @GetMapping("/{id}/permissions")
    public ResponseResult<List<Long>> getRolePermissions(@PathVariable Long id) {
        return ResponseResult.success(roleService.getRolePermissionIds(id));
    }

    /**
     * 检查角色编码是否已存在
     */
    @GetMapping("/exists")
    public ResponseResult<Boolean> exists(@RequestParam String roleCode,
                                          @RequestParam(required = false) Long excludeId) {
        return ResponseResult.success(roleService.existsRoleCode(roleCode, excludeId));
    }

    /**
     * 根据用户ID查询角色列表
     */
    @GetMapping("/user/{userId}")
    public ResponseResult<List<RoleVO>> getRolesByUserId(@PathVariable Long userId) {
        return ResponseResult.success(roleService.selectRolesByUserId(userId));
    }
}

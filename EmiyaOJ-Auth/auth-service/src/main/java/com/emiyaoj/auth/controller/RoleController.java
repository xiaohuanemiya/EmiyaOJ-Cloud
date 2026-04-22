package com.emiyaoj.auth.controller;

import com.emiyaoj.auth.dto.RoleQueryDTO;
import com.emiyaoj.auth.dto.RoleSaveDTO;
import com.emiyaoj.auth.service.IRoleService;
import com.emiyaoj.auth.vo.RoleVO;
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
 * 角色管理控制器
 */
@Tag(name = "角色管理")
@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
public class RoleController {

    private final IRoleService roleService;

    /**
     * 分页查询角色列表
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询角色列表")
    public ResponseResult<PageVO<RoleVO>> page(@RequestBody RoleQueryDTO queryDTO) {
        return ResponseResult.success(PageVO.of(roleService.selectRolePage(queryDTO)));
    }

    /**
     * 查询所有角色（下拉选择用）
     */
    @GetMapping("/list")
    @Operation(summary = "查询所有角色")
    public ResponseResult<List<RoleVO>> list() {
        return ResponseResult.success(roleService.selectAllRoles());
    }

    /**
     * 根据ID查询角色详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询角色详情")
    public ResponseResult<RoleVO> getById(@Parameter(description = "角色ID") @PathVariable Long id) {
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
    @Operation(summary = "新增角色")
    public ResponseResult<Void> save(@Valid @RequestBody RoleSaveDTO saveDTO) {
        roleService.saveRole(saveDTO);
        return ResponseResult.success();
    }

    /**
     * 修改角色
     */
    @PutMapping
    @Operation(summary = "修改角色")
    public ResponseResult<Void> update(@Valid @RequestBody RoleSaveDTO saveDTO) {
        roleService.updateRole(saveDTO);
        return ResponseResult.success();
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色")
    public ResponseResult<Void> delete(@Parameter(description = "角色ID") @PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseResult.success();
    }

    /**
     * 批量删除角色
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除角色")
    public ResponseResult<Void> deleteBatch(@RequestBody List<Long> ids) {
        roleService.deleteRoles(ids);
        return ResponseResult.success();
    }

    /**
     * 更新角色状态
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "更新角色状态")
    public ResponseResult<Void> updateStatus(@Parameter(description = "角色ID") @PathVariable Long id,
                                              @Parameter(description = "状态值") @RequestParam Integer status) {
        roleService.updateRoleStatus(id, status);
        return ResponseResult.success();
    }

    /**
     * 为角色分配权限
     */
    @PutMapping("/{id}/permissions")
    @Operation(summary = "为角色分配权限")
    public ResponseResult<Void> assignPermissions(@Parameter(description = "角色ID") @PathVariable Long id,
                                                   @RequestBody List<Long> permissionIds) {
        roleService.assignPermissions(id, permissionIds);
        return ResponseResult.success();
    }

    /**
     * 获取角色已分配的权限ID列表
     */
    @GetMapping("/{id}/permissions")
    @Operation(summary = "获取角色已分配的权限ID列表")
    public ResponseResult<List<Long>> getRolePermissions(@Parameter(description = "角色ID") @PathVariable Long id) {
        return ResponseResult.success(roleService.getRolePermissionIds(id));
    }

    /**
     * 检查角色编码是否已存在
     */
    @GetMapping("/exists")
    @Operation(summary = "检查角色编码是否已存在")
    public ResponseResult<Boolean> exists(@Parameter(description = "角色编码") @RequestParam String roleCode,
                                          @Parameter(description = "排除的角色ID") @RequestParam(required = false) Long excludeId) {
        return ResponseResult.success(roleService.existsRoleCode(roleCode, excludeId));
    }

    /**
     * 根据用户ID查询角色列表
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "根据用户ID查询角色列表")
    public ResponseResult<List<RoleVO>> getRolesByUserId(@Parameter(description = "用户ID") @PathVariable Long userId) {
        return ResponseResult.success(roleService.selectRolesByUserId(userId));
    }
}

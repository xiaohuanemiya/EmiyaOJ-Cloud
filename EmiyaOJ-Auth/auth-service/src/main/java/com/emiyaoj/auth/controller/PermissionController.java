package com.emiyaoj.auth.controller;

import com.emiyaoj.auth.dto.PermissionQueryDTO;
import com.emiyaoj.auth.dto.PermissionSaveDTO;
import com.emiyaoj.auth.service.IPermissionService;
import com.emiyaoj.auth.vo.PermissionVO;
import com.emiyaoj.common.domain.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限管理控制器
 */
@Tag(name = "权限管理")
@RestController
@RequestMapping("/permission")
@RequiredArgsConstructor
public class PermissionController {

    private final IPermissionService permissionService;

    @Operation(summary = "查询权限列表", description = "平铺方式查询权限列表")
    @PostMapping("/list")
    public ResponseResult<List<PermissionVO>> list(@RequestBody PermissionQueryDTO queryDTO) {
        return ResponseResult.success(permissionService.selectPermissionList(queryDTO));
    }

    @Operation(summary = "查询权限树", description = "树形结构查询权限列表")
    @PostMapping("/tree")
    public ResponseResult<List<PermissionVO>> tree(@RequestBody PermissionQueryDTO queryDTO) {
        return ResponseResult.success(permissionService.selectPermissionTree(queryDTO));
    }

    @Operation(summary = "根据ID查询权限详情")
    @GetMapping("/{id}")
    public ResponseResult<PermissionVO> getById(@Parameter(description = "权限ID") @PathVariable Long id) {
        PermissionVO permission = permissionService.selectPermissionById(id);
        if (permission == null) {
            return ResponseResult.fail("权限不存在");
        }
        return ResponseResult.success(permission);
    }

    @Operation(summary = "新增权限")
    @PostMapping
    public ResponseResult<Void> save(@Valid @RequestBody PermissionSaveDTO saveDTO) {
        permissionService.savePermission(saveDTO);
        return ResponseResult.success();
    }

    @Operation(summary = "修改权限")
    @PutMapping
    public ResponseResult<Void> update(@Valid @RequestBody PermissionSaveDTO saveDTO) {
        permissionService.updatePermission(saveDTO);
        return ResponseResult.success();
    }

    @Operation(summary = "删除权限")
    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@Parameter(description = "权限ID") @PathVariable Long id) {
        permissionService.deletePermission(id);
        return ResponseResult.success();
    }

    @Operation(summary = "批量删除权限")
    @DeleteMapping("/batch")
    public ResponseResult<Void> deleteBatch(@RequestBody List<Long> ids) {
        permissionService.deletePermissions(ids);
        return ResponseResult.success();
    }

    @Operation(summary = "更新权限状态")
    @PutMapping("/{id}/status")
    public ResponseResult<Void> updateStatus(@Parameter(description = "权限ID") @PathVariable Long id,
                                             @Parameter(description = "状态值") @RequestParam Integer status) {
        permissionService.updatePermissionStatus(id, status);
        return ResponseResult.success();
    }

    @Operation(summary = "检查权限编码是否已存在")
    @GetMapping("/exists")
    public ResponseResult<Boolean> exists(@Parameter(description = "权限编码") @RequestParam String permissionCode,
                                          @Parameter(description = "排除的权限ID") @RequestParam(required = false) Long excludeId) {
        return ResponseResult.success(permissionService.existsPermissionCode(permissionCode, excludeId));
    }

    @Operation(summary = "根据角色ID查询权限列表")
    @GetMapping("/role/{roleId}")
    public ResponseResult<List<PermissionVO>> getPermissionsByRoleId(@Parameter(description = "角色ID") @PathVariable Long roleId) {
        return ResponseResult.success(permissionService.selectPermissionsByRoleId(roleId));
    }

    @Operation(summary = "根据用户ID查询权限列表")
    @GetMapping("/user/{userId}")
    public ResponseResult<List<PermissionVO>> getPermissionsByUserId(@Parameter(description = "用户ID") @PathVariable Long userId) {
        return ResponseResult.success(permissionService.selectPermissionsByUserId(userId));
    }
}

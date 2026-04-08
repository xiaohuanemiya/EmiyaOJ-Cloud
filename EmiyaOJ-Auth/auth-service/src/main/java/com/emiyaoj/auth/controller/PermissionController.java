package com.emiyaoj.auth.controller;

import com.emiyaoj.auth.dto.PermissionQueryDTO;
import com.emiyaoj.auth.dto.PermissionSaveDTO;
import com.emiyaoj.auth.service.IPermissionService;
import com.emiyaoj.auth.vo.PermissionVO;
import com.emiyaoj.common.domain.ResponseResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限管理控制器
 */
@RestController
@RequestMapping("/permission")
@RequiredArgsConstructor
public class PermissionController {

    private final IPermissionService permissionService;

    /**
     * 查询权限列表（平铺）
     */
    @PostMapping("/list")
    public ResponseResult<List<PermissionVO>> list(@RequestBody PermissionQueryDTO queryDTO) {
        return ResponseResult.success(permissionService.selectPermissionList(queryDTO));
    }

    /**
     * 查询权限树
     */
    @PostMapping("/tree")
    public ResponseResult<List<PermissionVO>> tree(@RequestBody PermissionQueryDTO queryDTO) {
        return ResponseResult.success(permissionService.selectPermissionTree(queryDTO));
    }

    /**
     * 根据ID查询权限详情
     */
    @GetMapping("/{id}")
    public ResponseResult<PermissionVO> getById(@PathVariable Long id) {
        PermissionVO permission = permissionService.selectPermissionById(id);
        if (permission == null) {
            return ResponseResult.fail("权限不存在");
        }
        return ResponseResult.success(permission);
    }

    /**
     * 新增权限
     */
    @PostMapping
    public ResponseResult<Void> save(@Valid @RequestBody PermissionSaveDTO saveDTO) {
        permissionService.savePermission(saveDTO);
        return ResponseResult.success();
    }

    /**
     * 修改权限
     */
    @PutMapping
    public ResponseResult<Void> update(@Valid @RequestBody PermissionSaveDTO saveDTO) {
        permissionService.updatePermission(saveDTO);
        return ResponseResult.success();
    }

    /**
     * 删除权限
     */
    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return ResponseResult.success();
    }

    /**
     * 批量删除权限
     */
    @DeleteMapping("/batch")
    public ResponseResult<Void> deleteBatch(@RequestBody List<Long> ids) {
        permissionService.deletePermissions(ids);
        return ResponseResult.success();
    }

    /**
     * 更新权限状态
     */
    @PutMapping("/{id}/status")
    public ResponseResult<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        permissionService.updatePermissionStatus(id, status);
        return ResponseResult.success();
    }

    /**
     * 检查权限编码是否已存在
     */
    @GetMapping("/exists")
    public ResponseResult<Boolean> exists(@RequestParam String permissionCode,
                                          @RequestParam(required = false) Long excludeId) {
        return ResponseResult.success(permissionService.existsPermissionCode(permissionCode, excludeId));
    }

    /**
     * 根据角色ID查询权限列表
     */
    @GetMapping("/role/{roleId}")
    public ResponseResult<List<PermissionVO>> getPermissionsByRoleId(@PathVariable Long roleId) {
        return ResponseResult.success(permissionService.selectPermissionsByRoleId(roleId));
    }

    /**
     * 根据用户ID查询权限列表
     */
    @GetMapping("/user/{userId}")
    public ResponseResult<List<PermissionVO>> getPermissionsByUserId(@PathVariable Long userId) {
        return ResponseResult.success(permissionService.selectPermissionsByUserId(userId));
    }
}

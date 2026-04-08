package com.emiyaoj.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.emiyaoj.auth.dto.RoleQueryDTO;
import com.emiyaoj.auth.dto.RoleSaveDTO;
import com.emiyaoj.auth.vo.RoleVO;

import java.util.List;

/**
 * 角色管理服务接口
 */
public interface IRoleService {

    Page<RoleVO> selectRolePage(RoleQueryDTO queryDTO);

    RoleVO selectRoleById(Long id);

    List<RoleVO> selectAllRoles();

    boolean saveRole(RoleSaveDTO saveDTO);

    boolean updateRole(RoleSaveDTO saveDTO);

    boolean deleteRole(Long id);

    boolean deleteRoles(List<Long> ids);

    boolean updateRoleStatus(Long id, Integer status);

    boolean assignPermissions(Long roleId, List<Long> permissionIds);

    List<Long> getRolePermissionIds(Long roleId);

    boolean existsRoleCode(String roleCode, Long excludeId);

    List<RoleVO> selectRolesByUserId(Long userId);
}

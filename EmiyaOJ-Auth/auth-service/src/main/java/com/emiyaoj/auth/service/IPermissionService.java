package com.emiyaoj.auth.service;

import com.emiyaoj.auth.dto.PermissionQueryDTO;
import com.emiyaoj.auth.dto.PermissionSaveDTO;
import com.emiyaoj.auth.vo.PermissionVO;

import java.util.List;

/**
 * 权限管理服务接口
 */
public interface IPermissionService {

    List<PermissionVO> selectPermissionList(PermissionQueryDTO queryDTO);

    List<PermissionVO> selectPermissionTree(PermissionQueryDTO queryDTO);

    PermissionVO selectPermissionById(Long id);

    boolean savePermission(PermissionSaveDTO saveDTO);

    boolean updatePermission(PermissionSaveDTO saveDTO);

    boolean deletePermission(Long id);

    boolean deletePermissions(List<Long> ids);

    boolean updatePermissionStatus(Long id, Integer status);

    boolean existsPermissionCode(String permissionCode, Long excludeId);

    List<PermissionVO> selectPermissionsByRoleId(Long roleId);

    List<PermissionVO> selectPermissionsByUserId(Long userId);

    List<PermissionVO> buildPermissionTree(List<PermissionVO> permissions);
}

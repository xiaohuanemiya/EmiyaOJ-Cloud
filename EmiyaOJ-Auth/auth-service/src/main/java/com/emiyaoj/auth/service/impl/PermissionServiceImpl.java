package com.emiyaoj.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.emiyaoj.auth.domain.pojo.Permission;
import com.emiyaoj.auth.dto.PermissionQueryDTO;
import com.emiyaoj.auth.dto.PermissionSaveDTO;
import com.emiyaoj.auth.mapper.PermissionMapper;
import com.emiyaoj.auth.mapper.RolePermissionMapper;
import com.emiyaoj.auth.mapper.UserRoleMapper;
import com.emiyaoj.auth.service.IPermissionService;
import com.emiyaoj.auth.vo.PermissionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限管理服务实现类
 */
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements IPermissionService {

    private final RolePermissionMapper rolePermissionMapper;
    private final UserRoleMapper userRoleMapper;

    @Override
    public List<PermissionVO> selectPermissionList(PermissionQueryDTO queryDTO) {
        LambdaQueryWrapper<Permission> wrapper = buildQueryWrapper(queryDTO);
        List<Permission> permissions = this.list(wrapper);
        return permissions.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<PermissionVO> selectPermissionTree(PermissionQueryDTO queryDTO) {
        List<PermissionVO> permissionList = selectPermissionList(queryDTO);
        return buildPermissionTree(permissionList);
    }

    @Override
    public PermissionVO selectPermissionById(Long id) {
        Permission permission = this.getById(id);
        if (permission == null || permission.getDeleted() == 1) {
            return null;
        }
        return convertToVO(permission);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean savePermission(PermissionSaveDTO saveDTO) {
        if (existsPermissionCode(saveDTO.getPermissionCode(), null)) {
            throw new RuntimeException("权限编码已存在");
        }

        Permission permission = new Permission();
        BeanUtils.copyProperties(saveDTO, permission);
        permission.setParentId(saveDTO.getParentId() != null ? saveDTO.getParentId() : 0L);
        permission.setStatus(saveDTO.getStatus() != null ? saveDTO.getStatus() : 1);
        permission.setSortOrder(saveDTO.getSortOrder() != null ? saveDTO.getSortOrder() : 0);
        permission.setCreateTime(LocalDateTime.now());
        permission.setUpdateTime(LocalDateTime.now());

        return this.save(permission);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePermission(PermissionSaveDTO saveDTO) {
        Permission existPermission = this.getById(saveDTO.getId());
        if (existPermission == null || existPermission.getDeleted() == 1) {
            throw new RuntimeException("权限不存在");
        }

        if (existsPermissionCode(saveDTO.getPermissionCode(), saveDTO.getId())) {
            throw new RuntimeException("权限编码已存在");
        }

        Permission permission = new Permission();
        BeanUtils.copyProperties(saveDTO, permission);
        permission.setUpdateTime(LocalDateTime.now());

        return this.updateById(permission);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePermission(Long id) {
        // 检查是否有子权限
        long childCount = this.count(new LambdaQueryWrapper<Permission>()
                .eq(Permission::getParentId, id)
                .eq(Permission::getDeleted, 0));
        if (childCount > 0) {
            throw new RuntimeException("该权限下还有子权限，无法删除");
        }

        // 检查是否有角色绑定
        List<Long> roleIds = rolePermissionMapper.selectRoleIdsByPermissionId(id);
        if (!CollectionUtils.isEmpty(roleIds)) {
            throw new RuntimeException("该权限已被角色使用，无法删除");
        }

        Permission permission = new Permission();
        permission.setId(id);
        permission.setDeleted(1);
        permission.setUpdateTime(LocalDateTime.now());

        return this.updateById(permission);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePermissions(List<Long> ids) {
        for (Long id : ids) {
            deletePermission(id);
        }
        return true;
    }

    @Override
    public boolean updatePermissionStatus(Long id, Integer status) {
        Permission permission = new Permission();
        permission.setId(id);
        permission.setStatus(status);
        permission.setUpdateTime(LocalDateTime.now());
        return this.updateById(permission);
    }

    @Override
    public boolean existsPermissionCode(String permissionCode, Long excludeId) {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getPermissionCode, permissionCode)
                .eq(Permission::getDeleted, 0);
        if (excludeId != null) {
            wrapper.ne(Permission::getId, excludeId);
        }
        return this.count(wrapper) > 0;
    }

    @Override
    public List<PermissionVO> selectPermissionsByRoleId(Long roleId) {
        List<Long> permissionIds = rolePermissionMapper.selectPermissionIdsByRoleId(roleId);
        if (CollectionUtils.isEmpty(permissionIds)) {
            return List.of();
        }

        return this.listByIds(permissionIds).stream()
                .filter(p -> p.getStatus() == 1 && p.getDeleted() == 0)
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PermissionVO> selectPermissionsByUserId(Long userId) {
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
        Set<Long> permissionIds = new HashSet<>();

        if (!CollectionUtils.isEmpty(roleIds)) {
            List<Long> rolePermissionIds = rolePermissionMapper.selectPermissionIdsByRoleIds(roleIds);
            permissionIds.addAll(rolePermissionIds);
        }

        if (permissionIds.isEmpty()) {
            return List.of();
        }

        return this.listByIds(permissionIds).stream()
                .filter(p -> p.getStatus() == 1 && p.getDeleted() == 0)
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PermissionVO> buildPermissionTree(List<PermissionVO> permissions) {
        if (CollectionUtils.isEmpty(permissions)) {
            return List.of();
        }

        Map<Long, PermissionVO> permissionMap = permissions.stream()
                .collect(Collectors.toMap(PermissionVO::getId, p -> p));

        List<PermissionVO> rootPermissions = new ArrayList<>();

        for (PermissionVO permission : permissions) {
            if (permission.getParentId() == null || permission.getParentId() == 0
                    || permission.getParentId() == -1) {
                rootPermissions.add(permission);
            } else {
                PermissionVO parent = permissionMap.get(permission.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(permission);
                }
            }
        }

        sortPermissionTree(rootPermissions);
        return rootPermissions;
    }

    // ==================== 私有方法 ====================

    private LambdaQueryWrapper<Permission> buildQueryWrapper(PermissionQueryDTO queryDTO) {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getPermissionCode()),
                        Permission::getPermissionCode, queryDTO.getPermissionCode())
                .like(StringUtils.hasText(queryDTO.getPermissionName()),
                        Permission::getPermissionName, queryDTO.getPermissionName())
                .eq(queryDTO.getPermissionType() != null,
                        Permission::getPermissionType, queryDTO.getPermissionType())
                .eq(queryDTO.getStatus() != null, Permission::getStatus, queryDTO.getStatus())
                .eq(queryDTO.getParentId() != null, Permission::getParentId, queryDTO.getParentId())
                .eq(Permission::getDeleted, 0)
                .orderByAsc(Permission::getSortOrder)
                .orderByDesc(Permission::getCreateTime);
        return wrapper;
    }

    private void sortPermissionTree(List<PermissionVO> permissions) {
        if (CollectionUtils.isEmpty(permissions)) return;

        permissions.sort(Comparator.comparing(PermissionVO::getSortOrder,
                Comparator.nullsLast(Comparator.naturalOrder())));

        for (PermissionVO permission : permissions) {
            if (!CollectionUtils.isEmpty(permission.getChildren())) {
                sortPermissionTree(permission.getChildren());
            }
        }
    }

    private PermissionVO convertToVO(Permission permission) {
        PermissionVO vo = new PermissionVO();
        BeanUtils.copyProperties(permission, vo);

        // 设置权限类型描述
        if (permission.getPermissionType() != null) {
            switch (permission.getPermissionType()) {
                case 1 -> vo.setPermissionTypeDesc("菜单");
                case 2 -> vo.setPermissionTypeDesc("按钮");
                case 3 -> vo.setPermissionTypeDesc("接口");
                default -> vo.setPermissionTypeDesc("未知");
            }
        }

        vo.setStatusDesc(permission.getStatus() == 1 ? "启用" : "禁用");
        return vo;
    }
}

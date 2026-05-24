package com.emiyaoj.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.emiyaoj.auth.domain.pojo.Permission;
import com.emiyaoj.auth.domain.pojo.Role;
import com.emiyaoj.auth.domain.pojo.RolePermission;
import com.emiyaoj.auth.dto.RoleQueryDTO;
import com.emiyaoj.auth.dto.RoleSaveDTO;
import com.emiyaoj.auth.mapper.PermissionMapper;
import com.emiyaoj.auth.mapper.RoleMapper;
import com.emiyaoj.auth.mapper.RolePermissionMapper;
import com.emiyaoj.auth.mapper.UserRoleMapper;
import com.emiyaoj.auth.service.IRoleService;
import com.emiyaoj.auth.vo.RoleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色管理服务实现类
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements IRoleService {

    private final RolePermissionMapper rolePermissionMapper;
    private final UserRoleMapper userRoleMapper;
    private final PermissionMapper permissionMapper;

    @Override
    public Page<RoleVO> selectRolePage(RoleQueryDTO queryDTO) {
        Page<Role> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getRoleCode()), Role::getRoleCode, queryDTO.getRoleCode())
                .like(StringUtils.hasText(queryDTO.getRoleName()), Role::getRoleName, queryDTO.getRoleName())
                .eq(queryDTO.getStatus() != null, Role::getStatus, queryDTO.getStatus())
                .orderByDesc(Role::getCreateTime);

        Page<Role> rolePage = this.page(page, wrapper);

        List<RoleVO> roleVOList = rolePage.getRecords().stream()
                .map(this::convertToVO).collect(Collectors.toList());

        Page<RoleVO> result = new Page<>();
        BeanUtils.copyProperties(rolePage, result);
        result.setRecords(roleVOList);
        return result;
    }

    @Override
    public RoleVO selectRoleById(Long id) {
        Role role = this.getById(id);
        if (role == null) {
            return null;
        }
        return convertToVO(role);
    }

    @Override
    public List<RoleVO> selectAllRoles() {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getStatus, 1)
                .orderByAsc(Role::getCreateTime);

        return this.list(wrapper).stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveRole(RoleSaveDTO saveDTO) {
        if (existsRoleCode(saveDTO.getRoleCode(), null)) {
            throw new RuntimeException("角色编码已存在");
        }

        Role role = new Role();
        BeanUtils.copyProperties(saveDTO, role);
        role.setStatus(saveDTO.getStatus() != null ? saveDTO.getStatus() : 1);
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());

        boolean result = this.save(role);

        if (result && !CollectionUtils.isEmpty(saveDTO.getPermissionIds())) {
            assignPermissions(role.getId(), saveDTO.getPermissionIds());
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRole(RoleSaveDTO saveDTO) {
        Role existRole = this.getById(saveDTO.getId());
        if (existRole == null) {
            throw new RuntimeException("角色不存在");
        }

        if (existsRoleCode(saveDTO.getRoleCode(), saveDTO.getId())) {
            throw new RuntimeException("角色编码已存在");
        }

        Role role = new Role();
        BeanUtils.copyProperties(saveDTO, role);
        role.setUpdateTime(LocalDateTime.now());

        boolean result = this.updateById(role);

        if (result && saveDTO.getPermissionIds() != null) {
            assignPermissions(saveDTO.getId(), saveDTO.getPermissionIds());
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRole(Long id) {
        List<Long> userIds = userRoleMapper.selectUserIdsByRoleId(id);
        if (!CollectionUtils.isEmpty(userIds)) {
            throw new RuntimeException("该角色下还有用户，无法删除");
        }

        rolePermissionMapper.deleteByRoleId(id);
        return this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRoles(List<Long> ids) {
        return this.removeByIds(ids);
    }

    @Override
    public boolean updateRoleStatus(Long id, Integer status) {
        Role role = new Role();
        role.setId(id);
        role.setStatus(status);
        role.setUpdateTime(LocalDateTime.now());
        return this.updateById(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignPermissions(Long roleId, List<Long> permissionIds) {
        rolePermissionMapper.deleteByRoleId(roleId);

        if (!CollectionUtils.isEmpty(permissionIds)) {
            List<RolePermission> rolePermissions = permissionIds.stream().map(permissionId -> {
                RolePermission rp = new RolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(permissionId);
                rp.setCreateTime(LocalDateTime.now());
                return rp;
            }).collect(Collectors.toList());

            rolePermissionMapper.batchInsert(rolePermissions);
        }

        return true;
    }

    @Override
    public List<Long> getRolePermissionIds(Long roleId) {
        List<Long> permissionIds = rolePermissionMapper.selectPermissionIdsByRoleId(roleId);
        if (CollectionUtils.isEmpty(permissionIds)) {
            return List.of();
        }

        List<Permission> permissions = permissionMapper.selectByIds(permissionIds);
        return permissions.stream()
                .filter(p -> p.getStatus() == 1)
                .map(Permission::getId)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsRoleCode(String roleCode, Long excludeId) {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getRoleCode, roleCode)
                .eq(Role::getDeleted, 0);
        if (excludeId != null) {
            wrapper.ne(Role::getId, excludeId);
        }
        return this.count(wrapper) > 0;
    }

    @Override
    public List<RoleVO> selectRolesByUserId(Long userId) {
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
        if (CollectionUtils.isEmpty(roleIds)) {
            return List.of();
        }

        return this.listByIds(roleIds).stream()
                .filter(r -> r.getStatus() == 1 && r.getDeleted() == 0)
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    private RoleVO convertToVO(Role role) {
        RoleVO roleVO = new RoleVO();
        BeanUtils.copyProperties(role, roleVO);
        roleVO.setStatusDesc(role.getStatus() == 1 ? "启用" : "禁用");
        return roleVO;
    }
}

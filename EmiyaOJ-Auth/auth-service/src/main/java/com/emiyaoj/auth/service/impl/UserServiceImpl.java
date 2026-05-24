package com.emiyaoj.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.emiyaoj.auth.domain.pojo.*;
import com.emiyaoj.auth.dto.UserSaveDTO;
import com.emiyaoj.auth.mapper.*;
import com.emiyaoj.auth.service.IUserService;
import com.emiyaoj.auth.vo.UserVO;
import com.emiyaoj.common.domain.PageDTO;
import com.emiyaoj.common.domain.PageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PageVO<UserVO> selectUserPage(PageDTO query) {
        Page<User> page = query.toMpPageDefaultSortByCreateTimeDesc();

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        page(page, wrapper);

        return PageVO.of(page, this::convertToVO);
    }

    @Override
    public UserVO selectUserById(Long id) {
        User user = this.getById(id);
        if (user == null) {
            return null;
        }
        return convertToVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveUser(UserSaveDTO saveDTO) {
        // 检查用户名是否已存在
        User existingUser = selectUserByUsernameNoMatterDeleted(saveDTO.getUsername());
        if (existingUser != null && existingUser.getDeleted() == 0) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        BeanUtils.copyProperties(saveDTO, user);
        if (existingUser != null) {
            user.setId(existingUser.getId() == null ? IdWorker.getId() : existingUser.getId());
        } else {
            user.setId(IdWorker.getId());
        }
        user.setPassword(passwordEncoder.encode(saveDTO.getPassword()));
        user.setStatus(saveDTO.getStatus() != null ? saveDTO.getStatus() : 1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setDeleted(0);

        boolean result = this.saveOrUpdate(user);

        // 分配角色
        if (result && !CollectionUtils.isEmpty(saveDTO.getRoleIds())) {
            assignRoles(user.getId(), saveDTO.getRoleIds());
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(UserSaveDTO saveDTO) {
        User existUser = this.getById(saveDTO.getId());
        if (existUser == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查用户名是否已被其他用户使用
        User userWithSameUsername = selectUserByUsername(saveDTO.getUsername());
        if (userWithSameUsername != null && !userWithSameUsername.getId().equals(saveDTO.getId())) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        BeanUtils.copyProperties(saveDTO, user);

        if (StringUtils.hasText(saveDTO.getPassword())) {
            user.setPassword(passwordEncoder.encode(saveDTO.getPassword()));
        } else {
            user.setPassword(null);
        }

        user.setUpdateTime(LocalDateTime.now());
        boolean result = this.updateById(user);

        // 重新分配角色
        if (result && saveDTO.getRoleIds() != null) {
            assignRoles(saveDTO.getId(), saveDTO.getRoleIds());
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long id) {
        userRoleMapper.deleteByUserId(id);
        return this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUsers(List<Long> ids) {
        return this.removeByIds(ids);
    }

    @Override
    public boolean resetPassword(Long id, String newPassword) {
        User user = new User();
        user.setId(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        return this.updateById(user);
    }

    @Override
    public boolean updateUserStatus(Long id, Integer status) {
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        return this.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.deleteByUserId(userId);

        if (!CollectionUtils.isEmpty(roleIds)) {
            List<UserRole> userRoles = roleIds.stream().map(roleId -> {
                UserRole userRole = new UserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRole.setCreateTime(LocalDateTime.now());
                return userRole;
            }).collect(Collectors.toList());

            userRoleMapper.batchInsert(userRoles);
        }

        return true;
    }

    @Override
    public List<String> getUserPermissions(Long userId) {
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
        List<Long> rolePermissionIds = new ArrayList<>();
        if (!CollectionUtils.isEmpty(roleIds)) {
            rolePermissionIds = rolePermissionMapper.selectPermissionIdsByRoleIds(roleIds);
        }

        if (CollectionUtils.isEmpty(rolePermissionIds)) {
            return new ArrayList<>();
        }

        List<Permission> permissions = permissionMapper.selectList(
                new LambdaQueryWrapper<Permission>().in(Permission::getId, rolePermissionIds)
        );
        return permissions.stream()
                .filter(p -> p.getStatus() == 1)
                .map(Permission::getPermissionCode)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasPermission(Long userId, String permissionCode) {
        return getUserPermissions(userId).contains(permissionCode);
    }

    @Override
    public boolean hasRole(Long userId, String roleCode) {
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
        if (CollectionUtils.isEmpty(roleIds)) {
            return false;
        }

        List<Role> roles = roleMapper.selectList(
                new LambdaQueryWrapper<Role>().in(Role::getId, roleIds)
        );
        return roles.stream()
                .filter(r -> r.getStatus() == 1)
                .anyMatch(r -> r.getRoleCode().equals(roleCode));
    }

    @Override
    public List<UserVO> listUsersByPermission(String permissionCode) {
        if (!StringUtils.hasText(permissionCode)) {
            return List.of();
        }
        return baseMapper.selectUsersByPermissionCode(permissionCode).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    // ==================== 私有方法 ====================

    private User selectUserByUsername(String username) {
        return this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
    }

    private User selectUserByUsernameNoMatterDeleted(String username) {
        return this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
    }

    private UserVO convertToVO(User user) {
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        userVO.setStatusDesc(user.getStatus() == 1 ? "启用" : "禁用");
        return userVO;
    }
}

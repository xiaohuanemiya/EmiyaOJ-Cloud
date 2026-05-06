package com.emiyaoj.auth.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.emiyaoj.auth.domain.pojo.Permission;
import com.emiyaoj.auth.domain.pojo.Role;
import com.emiyaoj.auth.domain.pojo.RolePermission;
import com.emiyaoj.auth.domain.pojo.User;
import com.emiyaoj.auth.domain.pojo.UserRole;
import com.emiyaoj.auth.mapper.PermissionMapper;
import com.emiyaoj.auth.mapper.RoleMapper;
import com.emiyaoj.auth.mapper.RolePermissionMapper;
import com.emiyaoj.auth.mapper.UserMapper;
import com.emiyaoj.auth.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultAdminInitializer implements ApplicationRunner {

    private static final String ADMIN_ROLE_CODE = "admin";
    private static final String ADMIN_ROLE_NAME = "admin";
    private static final String DEFAULT_ADMIN_USERNAME = "DefaultAdmin";

    private final PermissionMapper permissionMapper;
    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${auth.init.default-admin-password:123456}")
    private String defaultAdminPassword;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(ApplicationArguments args) {
        ensurePermissions();
        Role adminRole = ensureAdminRole();
        ensureAdminRolePermissions(adminRole.getId());
        User defaultAdmin = ensureDefaultAdminUser();
        ensureDefaultAdminUserRole(defaultAdmin.getId(), adminRole.getId());
    }

    private void ensurePermissions() {
        List<PermissionSeed> seeds = permissionSeeds();
        List<String> permissionCodes = seeds.stream()
                .map(PermissionSeed::permissionCode)
                .toList();

        Map<String, Permission> permissionMap = permissionMapper.selectList(
                        new LambdaQueryWrapper<Permission>().in(Permission::getPermissionCode, permissionCodes)
                ).stream()
                .collect(Collectors.toMap(Permission::getPermissionCode, permission -> permission, (a, b) -> a));

        int created = 0;
        for (PermissionSeed seed : seeds) {
            if (permissionMap.containsKey(seed.permissionCode())) {
                continue;
            }

            Permission permission = new Permission();
            permission.setParentId(resolveParentId(permissionMap, seed.parentCode()));
            permission.setPermissionCode(seed.permissionCode());
            permission.setPermissionName(seed.permissionName());
            permission.setPermissionType(seed.permissionType());
            permission.setPath(seed.path());
            permission.setComponent(seed.component());
            permission.setIcon(seed.icon());
            permission.setSortOrder(seed.sortOrder());
            permission.setStatus(1);
            permission.setDeleted(0);
            permission.setCreateTime(LocalDateTime.now());
            permission.setUpdateTime(LocalDateTime.now());
            permissionMapper.insert(permission);
            permissionMap.put(permission.getPermissionCode(), permission);
            created++;
        }

        if (created > 0) {
            log.info("Initialized {} missing permissions", created);
        }
    }

    private Long resolveParentId(Map<String, Permission> permissionMap, String parentCode) {
        if (parentCode == null) {
            return 0L;
        }
        Permission parent = permissionMap.get(parentCode);
        return parent == null ? 0L : parent.getId();
    }

    private Role ensureAdminRole() {
        Role role = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getRoleCode, ADMIN_ROLE_CODE)
                .last("LIMIT 1"));
        if (role != null) {
            return role;
        }

        Role adminRole = new Role();
        adminRole.setRoleCode(ADMIN_ROLE_CODE);
        adminRole.setRoleName(ADMIN_ROLE_NAME);
        adminRole.setDescription("Default administrator role");
        adminRole.setStatus(1);
        adminRole.setDeleted(0);
        adminRole.setCreateTime(LocalDateTime.now());
        adminRole.setUpdateTime(LocalDateTime.now());
        roleMapper.insert(adminRole);
        log.info("Initialized default role {}", ADMIN_ROLE_CODE);
        return adminRole;
    }

    private void ensureAdminRolePermissions(Long adminRoleId) {
        List<Long> permissionIds = permissionMapper.selectList(new LambdaQueryWrapper<Permission>()
                        .eq(Permission::getDeleted, 0))
                .stream()
                .map(Permission::getId)
                .toList();
        if (permissionIds.isEmpty()) {
            return;
        }

        Set<Long> existingPermissionIds = new HashSet<>(rolePermissionMapper.selectPermissionIdsByRoleId(adminRoleId));
        List<RolePermission> missingRolePermissions = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (Long permissionId : permissionIds) {
            if (existingPermissionIds.contains(permissionId)) {
                continue;
            }
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(adminRoleId);
            rolePermission.setPermissionId(permissionId);
            rolePermission.setCreateTime(now);
            missingRolePermissions.add(rolePermission);
        }

        if (!missingRolePermissions.isEmpty()) {
            rolePermissionMapper.batchInsert(missingRolePermissions);
            log.info("Bound {} missing permissions to role {}", missingRolePermissions.size(), ADMIN_ROLE_CODE);
        }
    }

    private User ensureDefaultAdminUser() {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, DEFAULT_ADMIN_USERNAME)
                .last("LIMIT 1"));
        if (user != null) {
            return user;
        }

        User defaultAdmin = new User();
        defaultAdmin.setId(IdWorker.getId());
        defaultAdmin.setUsername(DEFAULT_ADMIN_USERNAME);
        defaultAdmin.setPassword(passwordEncoder.encode(defaultAdminPassword));
        defaultAdmin.setNickname("Default Admin");
        defaultAdmin.setStatus(1);
        defaultAdmin.setDeleted(0);
        defaultAdmin.setCreateTime(LocalDateTime.now());
        defaultAdmin.setUpdateTime(LocalDateTime.now());
        userMapper.insert(defaultAdmin);
        log.info("Initialized default user {}", DEFAULT_ADMIN_USERNAME);
        return defaultAdmin;
    }

    private void ensureDefaultAdminUserRole(Long userId, Long roleId) {
        Long existingCount = userRoleMapper.selectCount(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId)
                .eq(UserRole::getRoleId, roleId));
        if (existingCount > 0) {
            return;
        }

        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        userRole.setCreateTime(LocalDateTime.now());
        userRoleMapper.insert(userRole);
        log.info("Bound default user {} to role {}", DEFAULT_ADMIN_USERNAME, ADMIN_ROLE_CODE);
    }

    private List<PermissionSeed> permissionSeeds() {
        return List.of(
                new PermissionSeed(null, "DASHBOARD", "Dashboard", 1, "/dashboard", "views/dashboard/index.vue", "HomeFilled", 0),
                new PermissionSeed(null, "USER.LIST", "User Management", 1, "/user", "views/user/index.vue", "User", 1),
                new PermissionSeed("USER.LIST", "USER.ADD", "Add User", 2, null, null, null, 0),
                new PermissionSeed("USER.LIST", "USER.EDIT", "Edit User", 2, null, null, null, 1),
                new PermissionSeed("USER.LIST", "USER.DELETE", "Delete User", 2, null, null, null, 2),
                new PermissionSeed(null, "ROLE.LIST", "Role Management", 1, "/role", "views/role/index.vue", "UserFilled", 2),
                new PermissionSeed("ROLE.LIST", "ROLE.ADD", "Add Role", 2, null, null, null, 0),
                new PermissionSeed("ROLE.LIST", "ROLE.EDIT", "Edit Role", 2, null, null, null, 1),
                new PermissionSeed("ROLE.LIST", "ROLE.DELETE", "Delete Role", 2, null, null, null, 2),
                new PermissionSeed("ROLE.LIST", "ROLE.ASSIGN", "Assign Permission", 2, null, null, null, 3),
                new PermissionSeed(null, "PERMISSION.LIST", "Permission Management", 1, "/permission", "views/permission/index.vue", "Lock", 3),
                new PermissionSeed("PERMISSION.LIST", "PERMISSION.ADD", "Add Permission", 2, null, null, null, 0),
                new PermissionSeed("PERMISSION.LIST", "PERMISSION.EDIT", "Edit Permission", 2, null, null, null, 1),
                new PermissionSeed("PERMISSION.LIST", "PERMISSION.DELETE", "Delete Permission", 2, null, null, null, 2),
                new PermissionSeed(null, "PROBLEM.LIST", "Problem Management", 1, "/problem", "views/problem/index.vue", "Document", 4),
                new PermissionSeed("PROBLEM.LIST", "PROBLEM.ADD", "Add Problem", 2, null, null, null, 0),
                new PermissionSeed("PROBLEM.LIST", "PROBLEM.EDIT", "Edit Problem", 2, null, null, null, 1),
                new PermissionSeed("PROBLEM.LIST", "PROBLEM.DELETE", "Delete Problem", 2, null, null, null, 2),
                new PermissionSeed("PROBLEM.LIST", "TESTCASE.LIST", "View Test Case", 2, null, null, null, 3),
                new PermissionSeed("PROBLEM.LIST", "TESTCASE.ADD", "Add Test Case", 2, null, null, null, 4),
                new PermissionSeed("PROBLEM.LIST", "TESTCASE.EDIT", "Edit Test Case", 2, null, null, null, 5),
                new PermissionSeed("PROBLEM.LIST", "TESTCASE.DELETE", "Delete Test Case", 2, null, null, null, 6),
                new PermissionSeed(null, "PROBLEM_SET.LIST", "Problem Set Management", 1, "/problem-set", "views/problemSet/index.vue", "Collection", 5),
                new PermissionSeed(null, "CONTEST.LIST", "Contest Management", 1, "/contest", "views/contest/index.vue", "Trophy", 6),
                new PermissionSeed(null, "LANGUAGE.LIST", "Language Management", 1, "/language", "views/language/index.vue", "Setting", 7),
                new PermissionSeed("LANGUAGE.LIST", "LANGUAGE.ADD", "Add Language", 2, null, null, null, 0),
                new PermissionSeed("LANGUAGE.LIST", "LANGUAGE.EDIT", "Edit Language", 2, null, null, null, 1),
                new PermissionSeed("LANGUAGE.LIST", "LANGUAGE.DELETE", "Delete Language", 2, null, null, null, 2),
                new PermissionSeed(null, "BLOG.LIST", "Blog Management", 1, "/blog", "views/blog/index.vue", "EditPen", 8),
                new PermissionSeed("BLOG.LIST", "BLOG.ADD", "Add Blog", 2, null, null, null, 0),
                new PermissionSeed("BLOG.LIST", "BLOG.EDIT", "Edit Blog", 2, null, null, null, 1),
                new PermissionSeed("BLOG.LIST", "BLOG.DELETE", "Delete Blog", 2, null, null, null, 2),
                new PermissionSeed("BLOG.LIST", "BLOG.TAG.LIST", "Blog Tag List", 2, null, null, null, 3),
                new PermissionSeed("BLOG.LIST", "BLOG.TAG.ADD", "Add Blog Tag", 2, null, null, null, 4),
                new PermissionSeed("BLOG.LIST", "BLOG.TAG.EDIT", "Edit Blog Tag", 2, null, null, null, 5),
                new PermissionSeed("BLOG.LIST", "BLOG.TAG.DELETE", "Delete Blog Tag", 2, null, null, null, 6),
                new PermissionSeed(null, "SUBMISSION.LIST", "Submission Management", 1, "/submission", "views/submission/index.vue", "List", 9),
                new PermissionSeed(null, "CONTEST", "Contest Management", 1, "/contest", "contest/index", "trophy", 40),
                new PermissionSeed(null, "BLOG_MODERATION_MANAGE", "Blog Moderation Manage", 3, "/blog/moderation/**", null, null, 41)
        );
    }

    private record PermissionSeed(
            String parentCode,
            String permissionCode,
            String permissionName,
            Integer permissionType,
            String path,
            String component,
            String icon,
            Integer sortOrder
    ) {
    }
}

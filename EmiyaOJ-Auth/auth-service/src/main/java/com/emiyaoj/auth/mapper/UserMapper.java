package com.emiyaoj.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.emiyaoj.auth.domain.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户 Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("""
            SELECT DISTINCT u.*
            FROM user u
            INNER JOIN user_role ur ON u.id = ur.user_id
            INNER JOIN role r ON ur.role_id = r.id
            INNER JOIN role_permission rp ON r.id = rp.role_id
            INNER JOIN permission p ON rp.permission_id = p.id
            WHERE p.permission_code = #{permissionCode}
              AND p.status = 1
              AND p.deleted = 0
              AND r.status = 1
              AND r.deleted = 0
              AND u.status = 1
              AND u.deleted = 0
            ORDER BY u.id ASC
            """)
    List<User> selectUsersByPermissionCode(@Param("permissionCode") String permissionCode);
}

package com.emiyaoj.auth.service;

import com.emiyaoj.auth.dto.UserSaveDTO;
import com.emiyaoj.auth.vo.UserVO;
import com.emiyaoj.common.domain.PageDTO;
import com.emiyaoj.common.domain.PageVO;

import java.util.List;

/**
 * 用户管理服务接口
 */
public interface IUserService {

    PageVO<UserVO> selectUserPage(PageDTO query);

    UserVO selectUserById(Long id);

    boolean saveUser(UserSaveDTO saveDTO);

    boolean updateUser(UserSaveDTO saveDTO);

    boolean deleteUser(Long id);

    boolean deleteUsers(List<Long> ids);

    boolean resetPassword(Long id, String newPassword);

    boolean updateUserStatus(Long id, Integer status);

    boolean assignRoles(Long userId, List<Long> roleIds);

    List<String> getUserPermissions(Long userId);

    boolean hasPermission(Long userId, String permissionCode);

    boolean hasRole(Long userId, String roleCode);
}

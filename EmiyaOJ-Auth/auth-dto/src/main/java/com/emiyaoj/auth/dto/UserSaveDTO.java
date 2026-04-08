package com.emiyaoj.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用户新增/修改 DTO
 */
@Data
public class UserSaveDTO implements Serializable {

    /** 用户ID（修改时必传） */
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度为3-50个字符")
    private String username;

    @Size(min = 6, max = 50, message = "密码长度为6-50个字符")
    private String password;

    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String phone;

    private String avatar;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

    /** 角色ID列表 */
    private List<Long> roleIds;
}

package com.emiyaoj.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 角色新增/修改 DTO
 */
@Data
public class RoleSaveDTO implements Serializable {

    /** 角色ID（修改时必传） */
    private Long id;

    @NotBlank(message = "角色编码不能为空")
    @Size(max = 50, message = "角色编码长度不能超过50个字符")
    private String roleCode;

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过50个字符")
    private String roleName;

    @Size(max = 255, message = "角色描述长度不能超过255个字符")
    private String description;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

    /** 权限ID列表 */
    private List<Long> permissionIds;
}

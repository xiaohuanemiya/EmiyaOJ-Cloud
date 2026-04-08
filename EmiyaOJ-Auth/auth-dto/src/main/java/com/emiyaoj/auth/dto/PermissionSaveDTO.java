package com.emiyaoj.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 权限新增/修改 DTO
 */
@Data
public class PermissionSaveDTO implements Serializable {

    /** 权限ID（修改时必传） */
    private Long id;

    /** 父权限ID */
    private Long parentId;

    @NotBlank(message = "权限编码不能为空")
    @Size(max = 100, message = "权限编码长度不能超过100个字符")
    private String permissionCode;

    @NotBlank(message = "权限名称不能为空")
    @Size(max = 50, message = "权限名称长度不能超过50个字符")
    private String permissionName;

    /** 权限类型：1-菜单，2-按钮，3-接口 */
    @NotNull(message = "权限类型不能为空")
    private Integer permissionType;

    /** 路由路径 */
    private String path;

    /** 组件路径 */
    private String component;

    /** 图标 */
    private String icon;

    /** 排序 */
    private Integer sortOrder;

    /** 状态：0-禁用，1-启用 */
    private Integer status;
}

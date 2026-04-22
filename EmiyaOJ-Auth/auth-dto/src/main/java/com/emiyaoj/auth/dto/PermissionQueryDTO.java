package com.emiyaoj.auth.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 权限查询 DTO
 */
@Data
public class PermissionQueryDTO implements Serializable {

    private String permissionCode;

    private String permissionName;

    /** 权限类型：1-菜单，2-按钮，3-接口 */
    private Integer permissionType;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

    /** 父权限ID */
    private Long parentId;
}

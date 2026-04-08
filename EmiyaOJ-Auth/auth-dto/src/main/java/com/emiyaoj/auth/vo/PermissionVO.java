package com.emiyaoj.auth.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 权限信息 VO
 */
@Data
public class PermissionVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long parentId;

    private String permissionCode;

    private String permissionName;

    /** 权限类型：1-菜单，2-按钮，3-接口 */
    private Integer permissionType;

    private String permissionTypeDesc;

    private String path;

    private String component;

    private String icon;

    private Integer sortOrder;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

    private String statusDesc;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 子权限列表（用于构建权限树） */
    private List<PermissionVO> children;
}

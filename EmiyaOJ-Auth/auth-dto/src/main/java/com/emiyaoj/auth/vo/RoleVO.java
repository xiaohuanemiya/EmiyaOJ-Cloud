package com.emiyaoj.auth.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色信息 VO
 */
@Data
public class RoleVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String roleCode;

    private String roleName;

    private String description;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

    private String statusDesc;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    private List<PermissionVO> permissions;
}

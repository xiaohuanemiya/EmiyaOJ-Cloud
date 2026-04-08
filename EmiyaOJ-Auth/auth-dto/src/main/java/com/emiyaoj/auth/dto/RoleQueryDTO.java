package com.emiyaoj.auth.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 角色查询 DTO
 */
@Data
public class RoleQueryDTO implements Serializable {

    private String roleCode;

    private String roleName;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

    private String createTimeStart;

    private String createTimeEnd;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}

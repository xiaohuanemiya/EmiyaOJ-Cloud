package com.emiyaoj.common.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.RequiredArgsConstructor;

/**
 * 权限类型枚举
 */
@RequiredArgsConstructor
public enum PermissionTypeEnum {
    MENU(1, "菜单"),
    BUTTON(2, "按钮"),
    LINK(3, "接口");

    @EnumValue
    public final Integer value;
    public final String desc;
}

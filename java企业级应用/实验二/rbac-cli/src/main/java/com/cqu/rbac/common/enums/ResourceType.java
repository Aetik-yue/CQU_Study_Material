package com.cqu.rbac.common.enums;

import com.cqu.rbac.common.exception.BusinessException;

/**
 * 资源类型枚举：菜单 / 文件 / 接口 / 数据。
 */
public enum ResourceType {

    MENU("菜单"),
    FILE("文件"),
    API("接口"),
    DATA("数据");

    private final String description;

    ResourceType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 按名称解析资源类型。
     *
     * @throws BusinessException 名称非法时抛出
     */
    public static ResourceType of(String name) {
        if (name != null) {
            for (ResourceType type : values()) {
                if (type.name().equalsIgnoreCase(name.trim())) {
                    return type;
                }
            }
        }
        throw new BusinessException("E1002", "非法的资源类型：" + name + "（可选：MENU / FILE / API / DATA）");
    }
}

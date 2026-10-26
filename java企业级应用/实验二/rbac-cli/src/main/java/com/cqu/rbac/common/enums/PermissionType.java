package com.cqu.rbac.common.enums;

import com.cqu.rbac.common.exception.BusinessException;

/**
 * 权限类型枚举。
 * <ul>
 *   <li>MENU：菜单权限——控制菜单/功能入口是否可见</li>
 *   <li>OPERATION：操作权限——控制是否能执行某个动作</li>
 *   <li>RESOURCE：资源权限——控制是否能访问某个具体资源</li>
 * </ul>
 */
public enum PermissionType {

    MENU("菜单权限"),
    OPERATION("操作权限"),
    RESOURCE("资源权限");

    private final String description;

    PermissionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 按名称解析权限类型（数据库中以枚举名存储）。
     *
     * @throws BusinessException 名称非法时抛出
     */
    public static PermissionType of(String name) {
        if (name != null) {
            for (PermissionType type : values()) {
                if (type.name().equalsIgnoreCase(name.trim())) {
                    return type;
                }
            }
        }
        throw new BusinessException("E1001", "非法的权限类型：" + name + "（可选：MENU / OPERATION / RESOURCE）");
    }
}

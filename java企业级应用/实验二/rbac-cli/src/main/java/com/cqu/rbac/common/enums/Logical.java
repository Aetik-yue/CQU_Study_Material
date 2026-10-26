package com.cqu.rbac.common.enums;

/**
 * 权限组合逻辑：AND（全部满足）/ OR（任意满足）。
 * <p>用于 {@code @RequiresPermission} 注解与 {@code PermissionChecker} 的多权限判定。</p>
 */
public enum Logical {

    AND("全部满足"),
    OR("任意满足");

    private final String description;

    Logical(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

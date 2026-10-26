package com.cqu.rbac.common.enums;

/**
 * 通用启用/禁用状态枚举（与表中 status 列对应）。
 */
public enum CommonStatus {

    ENABLED(1, "启用"),
    DISABLED(0, "禁用");

    private final int code;
    private final String description;

    CommonStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /** 由数据库状态码解析枚举，非法值默认按禁用处理 */
    public static CommonStatus of(Integer code) {
        if (code != null && code == ENABLED.code) {
            return ENABLED;
        }
        return DISABLED;
    }

    /** 状态码转显示文本 */
    public static String textOf(Integer code) {
        return of(code).getDescription();
    }
}

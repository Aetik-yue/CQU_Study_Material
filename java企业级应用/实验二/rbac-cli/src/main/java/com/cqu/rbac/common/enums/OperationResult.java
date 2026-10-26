package com.cqu.rbac.common.enums;

/**
 * 操作结果枚举（审计日志 result 列取值）。
 */
public enum OperationResult {

    SUCCESS("成功"),
    FAILURE("失败");

    private final String description;

    OperationResult(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

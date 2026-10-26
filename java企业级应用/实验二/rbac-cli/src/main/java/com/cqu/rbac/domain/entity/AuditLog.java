package com.cqu.rbac.domain.entity;

import com.cqu.rbac.common.constants.Constants;

/**
 * 审计日志实体（对应表 sys_audit_log）。
 * <p>记录"谁在何时对什么做了什么、结果如何"。</p>
 * <p>设计模式：建造者 Builder——通过静态内部类 {@link Builder} 链式构建（体现内部类）。</p>
 */
public class AuditLog extends BaseEntity {

    /** 操作人 id（未登录时为空） */
    private Long userId;
    /** 操作人用户名 */
    private String username;
    /** 模块：用户管理/角色管理/权限管理/资源管理/认证/权限校验 */
    private String module;
    /** 操作：LOGIN/CREATE/UPDATE/DELETE/ASSIGN_ROLE... */
    private String operation;
    /** 操作对象 */
    private String targetObject;
    /** 操作详情（已脱敏，不记录密码等敏感信息） */
    private String detail;
    /** 结果：SUCCESS / FAILURE */
    private String result;
    /** 耗时（毫秒） */
    private Long durationMs;
    /** 失败原因 */
    private String errorMessage;
    /** 客户端标识 */
    private String client = Constants.CLIENT_CLI;

    public AuditLog() {
    }

    private AuditLog(Builder builder) {
        this.userId = builder.userId;
        this.username = builder.username;
        this.module = builder.module;
        this.operation = builder.operation;
        this.targetObject = builder.targetObject;
        this.detail = builder.detail;
        this.result = builder.result;
        this.durationMs = builder.durationMs;
        this.errorMessage = builder.errorMessage;
        this.client = builder.client;
        this.createTime = builder.createTime;
    }

    // ---------------- 建造者 ----------------

    public static Builder builder() {
        return new Builder();
    }

    /** 建造者静态内部类（设计模式：Builder + 内部类） */
    public static class Builder {
        private Long userId;
        private String username;
        private String module;
        private String operation;
        private String targetObject;
        private String detail;
        private String result;
        private Long durationMs;
        private String errorMessage;
        private String client = Constants.CLIENT_CLI;
        private java.time.LocalDateTime createTime;

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder module(String module) {
            this.module = module;
            return this;
        }

        public Builder operation(String operation) {
            this.operation = operation;
            return this;
        }

        public Builder targetObject(String targetObject) {
            this.targetObject = targetObject;
            return this;
        }

        public Builder detail(String detail) {
            this.detail = detail;
            return this;
        }

        public Builder result(String result) {
            this.result = result;
            return this;
        }

        public Builder durationMs(Long durationMs) {
            this.durationMs = durationMs;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder client(String client) {
            this.client = client;
            return this;
        }

        public Builder createTime(java.time.LocalDateTime createTime) {
            this.createTime = createTime;
            return this;
        }

        public AuditLog build() {
            return new AuditLog(this);
        }
    }

    // ---------------- getter / setter ----------------

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getTargetObject() {
        return targetObject;
    }

    public void setTargetObject(String targetObject) {
        this.targetObject = targetObject;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    @Override
    public String toString() {
        return "AuditLog{username='" + username + "', module='" + module + "', operation='" + operation
                + "', result='" + result + "'}";
    }
}

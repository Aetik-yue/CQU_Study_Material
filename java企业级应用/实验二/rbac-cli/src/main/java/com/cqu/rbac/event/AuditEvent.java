package com.cqu.rbac.event;

import com.cqu.rbac.domain.entity.AuditLog;

/**
 * 审计事件：由动态代理在方法执行后构造并投递到事件总线。
 * <p>观察者模式：事件的"生产者"（代理）与"消费者"（审计监听器落库）解耦。</p>
 */
public class AuditEvent {

    /** 封装好的审计日志 */
    private final AuditLog auditLog;

    public AuditEvent(AuditLog auditLog) {
        this.auditLog = auditLog;
    }

    public AuditLog getAuditLog() {
        return auditLog;
    }

    @Override
    public String toString() {
        return "AuditEvent{" + auditLog + "}";
    }
}

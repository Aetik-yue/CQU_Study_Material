package com.cqu.rbac.event;

import com.cqu.rbac.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 审计事件监听器：收到 {@link AuditEvent} 后把审计日志落库（sys_audit_log）。
 * <p>观察者模式中的具体观察者；落库失败仅记日志，绝不反向影响业务主流程。</p>
 */
public class AuditEventListener implements EventListener {

    private static final Logger log = LoggerFactory.getLogger(AuditEventListener.class);

    private final AuditService auditService;

    public AuditEventListener(AuditService auditService) {
        this.auditService = auditService;
    }

    @Override
    public void onEvent(Object event) {
        if (!(event instanceof AuditEvent)) {
            return;
        }
        AuditEvent auditEvent = (AuditEvent) event;
        try {
            auditService.record(auditEvent.getAuditLog());
        } catch (Exception e) {
            // 审计落库失败不能影响业务：仅记录错误日志
            log.error("审计日志落库失败：{}", e.getMessage(), e);
        }
    }
}

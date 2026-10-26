package com.cqu.rbac.service;

import com.cqu.rbac.common.result.PageResult;
import com.cqu.rbac.domain.entity.AuditLog;

import java.time.LocalDateTime;

/**
 * 审计日志服务：落库 + 多条件分页查询。
 * <p>权限注解 / 审计注解标注在实现类方法上。</p>
 */
public interface AuditService {

    /** 写入一条审计日志（内部调用，由事件监听器触发） */
    void record(AuditLog auditLog);

    /** 多条件分页查询（条件均可为 null 表示不限） */
    PageResult<AuditLog> page(String module, String username, String result,
                              LocalDateTime from, LocalDateTime to, int page, int size);

    /** 登录日志（认证模块）分页查询 */
    PageResult<AuditLog> listLoginLogs(int page, int size);

    /** 操作日志（排除认证模块）分页查询 */
    PageResult<AuditLog> listOperationLogs(int page, int size);
}

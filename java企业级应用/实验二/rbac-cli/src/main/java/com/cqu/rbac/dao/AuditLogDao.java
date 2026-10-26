package com.cqu.rbac.dao;

import com.cqu.rbac.domain.entity.AuditLog;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计日志 DAO：写入 + 多条件分页查询。
 */
public interface AuditLogDao extends BaseDao<AuditLog, Long> {

    /**
     * 多条件分页查询（条件均可为 null 表示不限）。
     *
     * @param module   模块精确匹配
     * @param username 用户名模糊匹配
     * @param result   结果精确匹配（SUCCESS/FAILURE）
     * @param from     起始时间（含）
     * @param to       截止时间（含）
     */
    List<AuditLog> findPage(String module, String username, String result,
                            LocalDateTime from, LocalDateTime to, int offset, int size);

    /** 多条件计数（与 findPage 条件一致） */
    long countByCondition(String module, String username, String result,
                          LocalDateTime from, LocalDateTime to);

    /** 分页查询"排除某模块"的日志（操作日志 = 排除认证模块） */
    List<AuditLog> findPageExcluding(String excludeModule, int offset, int size);

    /** 计数"排除某模块"的日志 */
    long countExcluding(String excludeModule);
}

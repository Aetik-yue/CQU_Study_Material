package com.cqu.rbac.service.impl;

import com.cqu.rbac.common.annotation.RequiresPermission;
import com.cqu.rbac.common.constants.Constants;
import com.cqu.rbac.common.result.PageQuery;
import com.cqu.rbac.common.result.PageResult;
import com.cqu.rbac.dao.AuditLogDao;
import com.cqu.rbac.domain.entity.AuditLog;
import com.cqu.rbac.service.AuditService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计日志服务实现：落库 + 多条件分页查询 + 登录/操作日志快捷视图。
 */
public class AuditServiceImpl implements AuditService {

    private final AuditLogDao auditLogDao;

    public AuditServiceImpl(AuditLogDao auditLogDao) {
        this.auditLogDao = auditLogDao;
    }

    /**
     * 写入审计日志（内部调用，无权限校验；由 EventBus 监听器在业务方法执行后被触发）。
     */
    @Override
    public void record(AuditLog auditLog) {
        if (auditLog == null) {
            return;
        }
        auditLogDao.insert(auditLog);
    }

    @Override
    @RequiresPermission("audit:list")
    @com.cqu.rbac.common.annotation.AuditLog(module = Constants.MODULE_AUDIT, operation = "QUERY")
    public PageResult<AuditLog> page(String module, String username, String result,
                                     LocalDateTime from, LocalDateTime to, int page, int size) {
        PageQuery query = new PageQuery(page, size);
        long total = auditLogDao.countByCondition(module, username, result, from, to);
        if (total == 0) {
            return PageResult.empty(query.getPage(), query.getSize());
        }
        List<AuditLog> records = auditLogDao.findPage(module, username, result, from, to,
                query.getOffset(), query.getSize());
        return PageResult.of(records, total, query.getPage(), query.getSize());
    }

    @Override
    @RequiresPermission("audit:login")
    public PageResult<AuditLog> listLoginLogs(int page, int size) {
        PageQuery query = new PageQuery(page, size);
        String module = Constants.MODULE_AUTH;
        long total = auditLogDao.countByCondition(module, null, null, null, null);
        if (total == 0) {
            return PageResult.empty(query.getPage(), query.getSize());
        }
        List<AuditLog> records = auditLogDao.findPage(module, null, null, null, null,
                query.getOffset(), query.getSize());
        return PageResult.of(records, total, query.getPage(), query.getSize());
    }

    @Override
    @RequiresPermission("audit:operation")
    public PageResult<AuditLog> listOperationLogs(int page, int size) {
        PageQuery query = new PageQuery(page, size);
        String excludeModule = Constants.MODULE_AUTH;
        long total = auditLogDao.countExcluding(excludeModule);
        if (total == 0) {
            return PageResult.empty(query.getPage(), query.getSize());
        }
        List<AuditLog> records = auditLogDao.findPageExcluding(excludeModule,
                query.getOffset(), query.getSize());
        return PageResult.of(records, total, query.getPage(), query.getSize());
    }
}

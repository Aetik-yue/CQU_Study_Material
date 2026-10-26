package com.cqu.rbac.dao.impl;

import com.cqu.rbac.dao.AuditLogDao;
import com.cqu.rbac.domain.entity.AuditLog;
import com.cqu.rbac.jdbc.JdbcTemplate;
import com.cqu.rbac.jdbc.RowMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 审计日志 DAO 实现：多条件动态分页查询。
 */
public class AuditLogDaoImpl extends AbstractJdbcDao<AuditLog, Long> implements AuditLogDao {

    public AuditLogDaoImpl(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    // ---------------- 模板方法原语 ----------------

    @Override
    protected String getTableName() {
        return "sys_audit_log";
    }

    @Override
    protected RowMapper<AuditLog> getRowMapper() {
        return AuditLogDaoImpl::mapAuditLog;
    }

    @Override
    protected String getInsertSql() {
        return "INSERT INTO sys_audit_log (user_id, username, module, operation, target_object, detail, "
                + "result, duration_ms, error_message, client, create_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSql() {
        // 审计日志不允许修改（仅提供接口一致性）
        throw new UnsupportedOperationException("审计日志不允许修改");
    }

    @Override
    protected Object[] getInsertParams(AuditLog log) {
        return new Object[]{
                log.getUserId(), log.getUsername(), log.getModule(), log.getOperation(),
                log.getTargetObject(), log.getDetail(), log.getResult(),
                log.getDurationMs() == null ? 0L : log.getDurationMs(),
                log.getErrorMessage(),
                log.getClient() == null ? "CLI" : log.getClient(),
                log.getCreateTime() == null ? LocalDateTime.now() : log.getCreateTime()
        };
    }

    @Override
    protected Object[] getUpdateParams(AuditLog entity) {
        throw new UnsupportedOperationException("审计日志不允许修改");
    }

    @Override
    protected void assignGeneratedId(AuditLog entity, Number id) {
        entity.setId(id == null ? null : id.longValue());
    }

    /** 结果集行 → AuditLog 实体 */
    static AuditLog mapAuditLog(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        AuditLog log = AuditLog.builder()
                .userId(rs.getObject("user_id") == null ? null : rs.getLong("user_id"))
                .username(rs.getString("username"))
                .module(rs.getString("module"))
                .operation(rs.getString("operation"))
                .targetObject(rs.getString("target_object"))
                .detail(rs.getString("detail"))
                .result(rs.getString("result"))
                .durationMs(rs.getLong("duration_ms"))
                .errorMessage(rs.getString("error_message"))
                .client(rs.getString("client"))
                .createTime(UserDaoImpl.toLocalDateTime(rs.getTimestamp("create_time")))
                .build();
        log.setId(rs.getLong("id"));
        return log;
    }

    // ---------------- 分页查询 ----------------

    @Override
    public List<AuditLog> findPage(String module, String username, String result,
                                   LocalDateTime from, LocalDateTime to, int offset, int size) {
        StringBuilder sql = new StringBuilder("SELECT * FROM sys_audit_log WHERE 1 = 1");
        List<Object> args = new ArrayList<>();
        appendConditions(sql, args, module, username, result, from, to);
        sql.append(" ORDER BY id DESC LIMIT ?, ?");
        args.add(offset);
        args.add(size);
        return jdbcTemplate.query(sql.toString(), getRowMapper(), args.toArray());
    }

    @Override
    public long countByCondition(String module, String username, String result,
                                 LocalDateTime from, LocalDateTime to) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM sys_audit_log WHERE 1 = 1");
        List<Object> args = new ArrayList<>();
        appendConditions(sql, args, module, username, result, from, to);
        return jdbcTemplate.count(sql.toString(), args.toArray());
    }

    /** 拼接动态查询条件 */
    private void appendConditions(StringBuilder sql, List<Object> args, String module, String username,
                                  String result, LocalDateTime from, LocalDateTime to) {
        if (module != null && !module.trim().isEmpty()) {
            // 模块使用模糊匹配：输入"用户"即可命中"用户管理"
            sql.append(" AND module LIKE ?");
            args.add("%" + module.trim() + "%");
        }
        if (username != null && !username.trim().isEmpty()) {
            sql.append(" AND username LIKE ?");
            args.add("%" + username.trim() + "%");
        }
        if (result != null && !result.trim().isEmpty()) {
            sql.append(" AND result = ?");
            args.add(result.trim().toUpperCase());
        }
        if (from != null) {
            sql.append(" AND create_time >= ?");
            args.add(from);
        }
        if (to != null) {
            sql.append(" AND create_time <= ?");
            args.add(to);
        }
    }

    @Override
    public List<AuditLog> findPageExcluding(String excludeModule, int offset, int size) {
        return jdbcTemplate.query(
                "SELECT * FROM sys_audit_log WHERE module IS NULL OR module NOT LIKE ? ORDER BY id DESC LIMIT ?, ?",
                getRowMapper(), "%" + excludeModule + "%", offset, size);
    }

    @Override
    public long countExcluding(String excludeModule) {
        return jdbcTemplate.count(
                "SELECT COUNT(*) FROM sys_audit_log WHERE module IS NULL OR module NOT LIKE ?",
                "%" + excludeModule + "%");
    }
}

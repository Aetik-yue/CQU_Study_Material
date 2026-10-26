package com.cqu.rbac.dao.impl;

import com.cqu.rbac.dao.PermissionDao;
import com.cqu.rbac.domain.entity.Permission;
import com.cqu.rbac.jdbc.JdbcTemplate;
import com.cqu.rbac.jdbc.RowMapper;

import java.util.Collection;
import java.util.List;

/**
 * 权限 DAO 实现：含按角色批量查询（IN 子句占位符防注入）与引用计数。
 */
public class PermissionDaoImpl extends AbstractJdbcDao<Permission, Long> implements PermissionDao {

    public PermissionDaoImpl(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    // ---------------- 模板方法原语 ----------------

    @Override
    protected String getTableName() {
        return "sys_permission";
    }

    @Override
    protected RowMapper<Permission> getRowMapper() {
        return PermissionDaoImpl::mapPermission;
    }

    @Override
    protected String getInsertSql() {
        return "INSERT INTO sys_permission (permission_name, permission_code, permission_type, parent_id, "
                + "resource_id, path, sort_order, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSql() {
        return "UPDATE sys_permission SET permission_name = ?, permission_code = ?, permission_type = ?, "
                + "parent_id = ?, resource_id = ?, path = ?, sort_order = ?, status = ? WHERE id = ?";
    }

    @Override
    protected Object[] getInsertParams(Permission p) {
        return new Object[]{
                p.getPermissionName(), p.getPermissionCode(), p.getPermissionType(),
                p.getParentId() == null ? 0L : p.getParentId(),
                p.getResourceId(), p.getPath(),
                p.getSortOrder() == null ? 0 : p.getSortOrder(),
                p.getStatus() == null ? 1 : p.getStatus()
        };
    }

    @Override
    protected Object[] getUpdateParams(Permission p) {
        return new Object[]{
                p.getPermissionName(), p.getPermissionCode(), p.getPermissionType(),
                p.getParentId() == null ? 0L : p.getParentId(),
                p.getResourceId(), p.getPath(),
                p.getSortOrder() == null ? 0 : p.getSortOrder(),
                p.getStatus(), p.getId()
        };
    }

    @Override
    protected void assignGeneratedId(Permission entity, Number id) {
        entity.setId(id == null ? null : id.longValue());
    }

    /** 结果集行 → Permission 实体 */
    static Permission mapPermission(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        Permission p = new Permission(rs.getString("permission_name"),
                rs.getString("permission_code"), rs.getString("permission_type"));
        p.setId(rs.getLong("id"));
        p.setParentId(rs.getLong("parent_id"));
        long resourceId = rs.getLong("resource_id");
        p.setResourceId(rs.wasNull() ? null : resourceId);
        p.setPath(rs.getString("path"));
        p.setSortOrder(rs.getInt("sort_order"));
        p.setStatus(rs.getInt("status"));
        p.setCreateTime(UserDaoImpl.toLocalDateTime(rs.getTimestamp("create_time")));
        p.setUpdateTime(UserDaoImpl.toLocalDateTime(rs.getTimestamp("update_time")));
        return p;
    }

    // ---------------- 权限专属查询 ----------------

    @Override
    public Permission findByCode(String code) {
        return jdbcTemplate.queryForObjectOrNull(
                "SELECT * FROM sys_permission WHERE permission_code = ?", getRowMapper(), code);
    }

    @Override
    public List<Permission> findByType(String type) {
        return jdbcTemplate.query(
                "SELECT * FROM sys_permission WHERE permission_type = ? AND status = 1 "
                        + "ORDER BY sort_order, id", getRowMapper(), type);
    }

    @Override
    public List<Permission> findByRoleIds(Collection<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        // IN 子句使用动态占位符，参数仍走 PreparedStatement，杜绝拼接注入
        String sql = "SELECT DISTINCT p.* FROM sys_permission p "
                + "JOIN sys_role_permission rp ON rp.permission_id = p.id "
                + "WHERE rp.role_id IN (" + JdbcTemplate.placeholders(roleIds.size()) + ") "
                + "AND p.status = 1 ORDER BY p.sort_order, p.id";
        return jdbcTemplate.query(sql, getRowMapper(), roleIds.toArray());
    }

    @Override
    public long countChildren(Long parentId) {
        return jdbcTemplate.count("SELECT COUNT(*) FROM sys_permission WHERE parent_id = ?", parentId);
    }

    @Override
    public long countRoleReferences(Long permissionId) {
        return jdbcTemplate.count("SELECT COUNT(*) FROM sys_role_permission WHERE permission_id = ?", permissionId);
    }

    @Override
    public List<Permission> findByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        String sql = "SELECT * FROM sys_permission WHERE id IN (" + JdbcTemplate.placeholders(ids.size())
                + ") ORDER BY sort_order, id";
        return jdbcTemplate.query(sql, getRowMapper(), ids.toArray());
    }

    @Override
    public List<Permission> findByResourceId(Long resourceId) {
        return jdbcTemplate.query(
                "SELECT * FROM sys_permission WHERE resource_id = ? AND status = 1", getRowMapper(), resourceId);
    }

    @Override
    public long countByResourceId(Long resourceId) {
        return jdbcTemplate.count("SELECT COUNT(*) FROM sys_permission WHERE resource_id = ?", resourceId);
    }
}

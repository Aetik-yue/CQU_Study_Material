package com.cqu.rbac.dao.impl;

import com.cqu.rbac.dao.RoleDao;
import com.cqu.rbac.domain.entity.Role;
import com.cqu.rbac.jdbc.JdbcTemplate;
import com.cqu.rbac.jdbc.RowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 角色 DAO 实现：含角色继承链查询（沿 parent_id 向上递归）。
 */
public class RoleDaoImpl extends AbstractJdbcDao<Role, Long> implements RoleDao {

    private static final Logger log = LoggerFactory.getLogger(RoleDaoImpl.class);

    /** 继承链最大深度（防环兜底） */
    private static final int MAX_CHAIN_DEPTH = 32;

    public RoleDaoImpl(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    // ---------------- 模板方法原语 ----------------

    @Override
    protected String getTableName() {
        return "sys_role";
    }

    @Override
    protected RowMapper<Role> getRowMapper() {
        return RoleDaoImpl::mapRole;
    }

    @Override
    protected String getInsertSql() {
        return "INSERT INTO sys_role (role_name, role_code, parent_id, description, status) "
                + "VALUES (?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSql() {
        return "UPDATE sys_role SET role_name = ?, role_code = ?, parent_id = ?, description = ?, status = ? "
                + "WHERE id = ?";
    }

    @Override
    protected Object[] getInsertParams(Role r) {
        return new Object[]{
                r.getRoleName(), r.getRoleCode(),
                r.getParentId() == null ? 0L : r.getParentId(),
                r.getDescription(),
                r.getStatus() == null ? 1 : r.getStatus()
        };
    }

    @Override
    protected Object[] getUpdateParams(Role r) {
        return new Object[]{
                r.getRoleName(), r.getRoleCode(),
                r.getParentId() == null ? 0L : r.getParentId(),
                r.getDescription(), r.getStatus(), r.getId()
        };
    }

    @Override
    protected void assignGeneratedId(Role entity, Number id) {
        entity.setId(id == null ? null : id.longValue());
    }

    /** 结果集行 → Role 实体 */
    static Role mapRole(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        Role role = new Role(rs.getString("role_name"), rs.getString("role_code"));
        role.setId(rs.getLong("id"));
        role.setParentId(rs.getLong("parent_id"));
        role.setDescription(rs.getString("description"));
        role.setStatus(rs.getInt("status"));
        role.setCreateTime(UserDaoImpl.toLocalDateTime(rs.getTimestamp("create_time")));
        role.setUpdateTime(UserDaoImpl.toLocalDateTime(rs.getTimestamp("update_time")));
        return role;
    }

    // ---------------- 角色专属查询 ----------------

    @Override
    public Role findByCode(String code) {
        return jdbcTemplate.queryForObjectOrNull(
                "SELECT * FROM sys_role WHERE role_code = ?", getRowMapper(), code);
    }

    @Override
    public List<Long> findPermissionIds(Long roleId) {
        return jdbcTemplate.query(
                "SELECT permission_id FROM sys_role_permission WHERE role_id = ? ORDER BY permission_id",
                (rs, rowNum) -> rs.getLong(1), roleId);
    }

    @Override
    public void insertRolePermissions(Long roleId, List<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return;
        }
        StringBuilder sql = new StringBuilder("INSERT INTO sys_role_permission (role_id, permission_id) VALUES ");
        List<Object> args = new ArrayList<>();
        for (int i = 0; i < permissionIds.size(); i++) {
            sql.append(i == 0 ? "(?, ?)" : ", (?, ?)");
            args.add(roleId);
            args.add(permissionIds.get(i));
        }
        jdbcTemplate.update(sql.toString(), args.toArray());
    }

    @Override
    public void deleteRolePermissions(Long roleId) {
        jdbcTemplate.update("DELETE FROM sys_role_permission WHERE role_id = ?", roleId);
    }

    @Override
    public long countUsersByRole(Long roleId) {
        return jdbcTemplate.count("SELECT COUNT(*) FROM sys_user_role WHERE role_id = ?", roleId);
    }

    @Override
    public long countChildren(Long roleId) {
        return jdbcTemplate.count("SELECT COUNT(*) FROM sys_role WHERE parent_id = ?", roleId);
    }

    /**
     * 角色继承链：包含自身与全部祖先。
     * <p>算法：循环沿 parent_id 上溯；visited 集合防环；深度上限兜底。</p>
     */
    @Override
    public List<Long> findRoleChainIds(Long roleId) {
        Set<Long> result = new LinkedHashSet<>();
        Set<Long> visited = new HashSet<>();
        Long current = roleId;
        int depth = 0;
        while (current != null && current > 0 && depth < MAX_CHAIN_DEPTH) {
            if (!visited.add(current)) {
                log.warn("角色继承链检测到环：roleId={}，已中断", roleId);
                break;
            }
            result.add(current);
            Role role = findById(current);
            if (role == null) {
                break;
            }
            current = role.getParentId();
            depth++;
        }
        return new ArrayList<>(result);
    }
}

package com.cqu.rbac.dao.impl;

import com.cqu.rbac.dao.ResourceDao;
import com.cqu.rbac.domain.entity.Resource;
import com.cqu.rbac.jdbc.JdbcTemplate;
import com.cqu.rbac.jdbc.RowMapper;

/**
 * 资源 DAO 实现。
 */
public class ResourceDaoImpl extends AbstractJdbcDao<Resource, Long> implements ResourceDao {

    public ResourceDaoImpl(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    // ---------------- 模板方法原语 ----------------

    @Override
    protected String getTableName() {
        return "sys_resource";
    }

    @Override
    protected RowMapper<Resource> getRowMapper() {
        return ResourceDaoImpl::mapResource;
    }

    @Override
    protected String getInsertSql() {
        return "INSERT INTO sys_resource (resource_name, resource_code, resource_type, resource_path, "
                + "description, status) VALUES (?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSql() {
        return "UPDATE sys_resource SET resource_name = ?, resource_code = ?, resource_type = ?, "
                + "resource_path = ?, description = ?, status = ? WHERE id = ?";
    }

    @Override
    protected Object[] getInsertParams(Resource r) {
        return new Object[]{
                r.getResourceName(), r.getResourceCode(), r.getResourceType(),
                r.getResourcePath(), r.getDescription(),
                r.getStatus() == null ? 1 : r.getStatus()
        };
    }

    @Override
    protected Object[] getUpdateParams(Resource r) {
        return new Object[]{
                r.getResourceName(), r.getResourceCode(), r.getResourceType(),
                r.getResourcePath(), r.getDescription(), r.getStatus(), r.getId()
        };
    }

    @Override
    protected void assignGeneratedId(Resource entity, Number id) {
        entity.setId(id == null ? null : id.longValue());
    }

    /** 结果集行 → Resource 实体 */
    static Resource mapResource(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        Resource r = new Resource(rs.getString("resource_name"),
                rs.getString("resource_code"), rs.getString("resource_type"));
        r.setId(rs.getLong("id"));
        r.setResourcePath(rs.getString("resource_path"));
        r.setDescription(rs.getString("description"));
        r.setStatus(rs.getInt("status"));
        r.setCreateTime(UserDaoImpl.toLocalDateTime(rs.getTimestamp("create_time")));
        r.setUpdateTime(UserDaoImpl.toLocalDateTime(rs.getTimestamp("update_time")));
        return r;
    }

    @Override
    public Resource findByCode(String code) {
        return jdbcTemplate.queryForObjectOrNull(
                "SELECT * FROM sys_resource WHERE resource_code = ?", getRowMapper(), code);
    }
}

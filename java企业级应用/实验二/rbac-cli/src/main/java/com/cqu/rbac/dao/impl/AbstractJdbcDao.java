package com.cqu.rbac.dao.impl;

import com.cqu.rbac.dao.BaseDao;
import com.cqu.rbac.jdbc.JdbcTemplate;
import com.cqu.rbac.jdbc.KeyHolder;
import com.cqu.rbac.jdbc.RowMapper;

import java.util.List;

/**
 * 抽象 JDBC DAO 基类。
 * <p>设计模式：模板方法 Template Method——本类固化通用 CRUD 的执行骨架（final 方法），
 * 子类只需提供"抽象原语"：表名、行映射器、插入/更新 SQL 与参数、主键回填方式。</p>
 *
 * @param <T>  实体类型
 * @param <ID> 主键类型
 */
public abstract class AbstractJdbcDao<T, ID> implements BaseDao<T, ID> {

    /** JDBC 模板：所有子类共享 */
    protected final JdbcTemplate jdbcTemplate;

    protected AbstractJdbcDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ---------------- 子类提供的抽象原语 ----------------

    /** 表名（如 sys_user） */
    protected abstract String getTableName();

    /** 行映射器：结果集行 → 实体 */
    protected abstract RowMapper<T> getRowMapper();

    /** 插入 SQL（列与占位符顺序需与 getInsertParams 一致） */
    protected abstract String getInsertSql();

    /** 更新 SQL（WHERE id=? 由子类在 SQL 中给出） */
    protected abstract String getUpdateSql();

    /** 插入参数 */
    protected abstract Object[] getInsertParams(T entity);

    /** 更新参数 */
    protected abstract Object[] getUpdateParams(T entity);

    /** 将数据库生成的主键赋回实体 */
    protected abstract void assignGeneratedId(T entity, Number id);

    // ---------------- 模板方法（骨架固定，子类不可改写） ----------------

    @Override
    public int insert(T entity) {
        KeyHolder keyHolder = new KeyHolder();
        int rows = jdbcTemplate.update(getInsertSql(), keyHolder, getInsertParams(entity));
        if (keyHolder.hasKey()) {
            assignGeneratedId(entity, keyHolder.getKey());
        }
        return rows;
    }

    @Override
    public int update(T entity) {
        return jdbcTemplate.update(getUpdateSql(), getUpdateParams(entity));
    }

    @Override
    public int deleteById(ID id) {
        return jdbcTemplate.update("DELETE FROM " + getTableName() + " WHERE id = ?", id);
    }

    @Override
    public T findById(ID id) {
        return jdbcTemplate.queryForObjectOrNull(
                "SELECT * FROM " + getTableName() + " WHERE id = ?", getRowMapper(), id);
    }

    @Override
    public List<T> findAll() {
        return jdbcTemplate.query("SELECT * FROM " + getTableName() + " ORDER BY id", getRowMapper());
    }

    @Override
    public long count() {
        return jdbcTemplate.count("SELECT COUNT(*) FROM " + getTableName());
    }
}

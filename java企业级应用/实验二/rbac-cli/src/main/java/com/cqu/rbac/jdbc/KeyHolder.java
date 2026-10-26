package com.cqu.rbac.jdbc;

/**
 * 自增主键回填持有者（内部状态类）。
 * <p>插入后由 {@link JdbcTemplate} 通过 getGeneratedKeys 回填生成的 id，
 * DAO 再把它赋回实体（体现"内部类/状态持有对象"的设计）。</p>
 */
public class KeyHolder {

    /** 生成的键值（可能为 null，表示数据库未返回） */
    private Number key;

    public Number getKey() {
        return key;
    }

    public void setKey(Number key) {
        this.key = key;
    }

    /** 是否已拿到键值 */
    public boolean hasKey() {
        return key != null;
    }

    @Override
    public String toString() {
        return "KeyHolder{key=" + key + "}";
    }
}

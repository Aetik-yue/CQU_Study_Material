package com.cqu.rbac.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 行映射函数式接口（泛型）。
 * <p>Lambda / 函数式接口落地点：查询结果到实体的映射由调用方以 Lambda 提供。</p>
 *
 * @param <T> 映射目标类型
 */
@FunctionalInterface
public interface RowMapper<T> {

    /**
     * 将当前结果集行映射为对象。
     *
     * @param rs     结果集（游标已定位在当前行）
     * @param rowNum 行号（从 0 开始）
     */
    T map(ResultSet rs, int rowNum) throws SQLException;
}

package com.cqu.rbac.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 参数绑定函数式接口。
 * <p>当参数需要特殊类型处理（如 LocalDateTime、NULL、流式写入）时，
 * 用 Lambda 直接操作 {@link PreparedStatement} 绑定参数。</p>
 */
@FunctionalInterface
public interface PreparedStatementSetter {

    /**
     * 为 PreparedStatement 绑定参数。
     *
     * @param ps 预编译语句（参数索引从 1 开始）
     */
    void setValues(PreparedStatement ps) throws SQLException;
}

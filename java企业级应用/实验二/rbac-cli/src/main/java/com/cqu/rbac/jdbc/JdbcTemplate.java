package com.cqu.rbac.jdbc;

import com.cqu.rbac.common.exception.DaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 自封装 JDBC 模板（核心基础设施）。
 * <p>职责：获取/释放连接、参数绑定（防 SQL 注入：全部使用 PreparedStatement 占位参数）、
 * 结果映射、自增主键回填。不含任何业务语义。</p>
 * <p>事务支持：查询/更新时优先使用 {@link TransactionTemplate} 通过 ThreadLocal 绑定的连接，
 * 否则从连接池借用并在使用后归还。</p>
 */
public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    /**
     * 事务上下文：当前线程绑定的事务连接。
     * <p>由 TransactionTemplate 写入/清除；JdbcTemplate 读取以复用同一连接。</p>
     */
    private static final ThreadLocal<Connection> TX_CONNECTION = new ThreadLocal<>();

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // ---------------- 事务连接辅助 ----------------

    /** 获取当前线程绑定的事务连接（无事务时为 null） */
    static Connection currentTransactionConnection() {
        return TX_CONNECTION.get();
    }

    /** 绑定事务连接到当前线程（仅 TransactionTemplate 调用） */
    static void bindTransactionConnection(Connection connection) {
        TX_CONNECTION.set(connection);
    }

    /** 解绑当前线程的事务连接 */
    static void unbindTransactionConnection() {
        TX_CONNECTION.remove();
    }

    /**
     * 获取连接：优先使用事务上下文中绑定的连接，否则向连接池借用。
     * <p>注意：只有非事务借用方式获取的连接，调用方才负责关闭。</p>
     */
    public Connection getConnection() throws SQLException {
        Connection txConn = TX_CONNECTION.get();
        if (txConn != null) {
            return txConn;
        }
        return dataSource.getConnection();
    }

    // ---------------- 更新（INSERT / UPDATE / DELETE） ----------------

    /**
     * 执行更新语句（占位参数 ? 形式，防 SQL 注入）。
     *
     * @return 受影响行数
     */
    public int update(String sql, Object... args) {
        return doUpdate(sql, null, null, args);
    }

    /**
     * 执行更新语句（自定义参数绑定，适合复杂类型）。
     */
    public int update(String sql, PreparedStatementSetter setter) {
        return doUpdate(sql, setter, null, (Object[]) null);
    }

    /**
     * 执行插入语句并回填自增主键。
     *
     * @param keyHolder 主键持有者，插入后可从其中取到生成的 id
     * @return 受影响行数
     */
    public int update(String sql, KeyHolder keyHolder, Object... args) {
        return doUpdate(sql, null, keyHolder, args);
    }

    /** 统一的更新执行逻辑 */
    private int doUpdate(String sql, PreparedStatementSetter setter, KeyHolder keyHolder, Object[] args) {
        Connection conn = null;
        boolean borrowed = false;
        try {
            conn = getConnection();
            borrowed = currentTransactionConnection() == null;
            try (PreparedStatement ps = keyHolder != null
                    ? conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
                    : conn.prepareStatement(sql)) {
                if (setter != null) {
                    setter.setValues(ps);
                } else {
                    bindArgs(ps, args);
                }
                int rows = ps.executeUpdate();
                if (keyHolder != null && rows > 0) {
                    fillKeyHolder(ps, keyHolder);
                }
                return rows;
            }
        } catch (SQLException e) {
            log.error("SQL 执行失败: {} - {}", sql, e.getMessage());
            throw new DaoException("SQL 执行失败：" + e.getMessage(), e);
        } finally {
            closeIfBorrowed(conn, borrowed);
        }
    }

    /** 从 Statement.getGeneratedKeys() 回填主键 */
    private void fillKeyHolder(PreparedStatement ps, KeyHolder keyHolder) throws SQLException {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                keyHolder.setKey(rs.getLong(1));
            }
        }
    }

    // ---------------- 查询 ----------------

    /**
     * 查询单行并映射；无结果时抛出 {@link DaoException}。
     */
    public <T> T queryForObject(String sql, RowMapper<T> mapper, Object... args) {
        T result = queryForObjectOrNull(sql, mapper, args);
        if (result == null) {
            throw new DaoException("查询结果为空：" + sql);
        }
        return result;
    }

    /**
     * 查询单行并映射；无结果时返回 null。
     */
    public <T> T queryForObjectOrNull(String sql, RowMapper<T> mapper, Object... args) {
        List<T> list = query(sql, mapper, args);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 查询多行并映射（泛型集合）。
     */
    public <T> List<T> query(String sql, RowMapper<T> mapper, Object... args) {
        Connection conn = null;
        boolean borrowed = false;
        try {
            conn = getConnection();
            borrowed = currentTransactionConnection() == null;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                bindArgs(ps, args);
                try (ResultSet rs = ps.executeQuery()) {
                    List<T> result = new ArrayList<>();
                    int rowNum = 0;
                    while (rs.next()) {
                        result.add(mapper.map(rs, rowNum++));
                    }
                    return result;
                }
            }
        } catch (SQLException e) {
            log.error("SQL 查询失败: {} - {}", sql, e.getMessage());
            throw new DaoException("SQL 查询失败：" + e.getMessage(), e);
        } finally {
            closeIfBorrowed(conn, borrowed);
        }
    }

    /**
     * 统计查询：执行形如 SELECT COUNT(*) 的语句并返回 long。
     */
    public long count(String sql, Object... args) {
        return queryForObject(sql, (rs, rowNum) -> rs.getLong(1), args);
    }

    // ---------------- 内部工具 ----------------

    /**
     * 绑定占位参数。
     * <p>注意：null 值显式 setNull，LocalDateTime 依赖 JDBC 4.2 的 setObject 支持。</p>
     */
    private void bindArgs(PreparedStatement ps, Object[] args) throws SQLException {
        if (args == null || args.length == 0) {
            return;
        }
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            int index = i + 1;
            if (arg == null) {
                ps.setNull(index, Types.NULL);
            } else {
                // 使用 setObject 以便支持 LocalDateTime/LocalDate、枚举等；数字/字符串同样适用
                ps.setObject(index, arg);
            }
        }
    }

    /** 仅关闭"从池中借用"的连接；事务连接由 TransactionTemplate 统一管理 */
    private void closeIfBorrowed(Connection conn, boolean borrowed) {
        if (conn != null && borrowed) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.warn("关闭数据库连接失败: {}", e.getMessage());
            }
        }
    }

    /** 供 DAO 构建 IN 子句占位符（如 "?,?,?"）的小工具 */
    public static String placeholders(int count) {
        if (count <= 0) {
            return "";
        }
        return String.join(",", Collections.nCopies(count, "?"));
    }
}

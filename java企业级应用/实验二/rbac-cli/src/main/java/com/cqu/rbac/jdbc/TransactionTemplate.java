package com.cqu.rbac.jdbc;

import com.cqu.rbac.common.exception.DaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 编程式事务模板：ThreadLocal 绑定连接 + commit/rollback + 资源清理。
 * <p>核心约定：</p>
 * <ol>
 *   <li>开启事务：取连接 setAutoCommit(false)，并绑定到当前线程（JdbcTemplate 优先复用该连接）</li>
 *   <li>回调成功 → commit；抛异常 → rollback，并保留原始异常类型向上抛出</li>
 *   <li>finally：解绑 ThreadLocal、恢复自动提交、归还连接到池</li>
 *   <li>支持嵌套调用：已处于事务中时直接复用外层事务（不重复开启）</li>
 * </ol>
 */
public class TransactionTemplate {

    private static final Logger log = LoggerFactory.getLogger(TransactionTemplate.class);

    /** 事务回调（函数式接口 + 泛型） */
    @FunctionalInterface
    public interface TransactionCallback<T> {
        T doInTransaction() throws Exception;
    }

    private final DataSource dataSource;

    public TransactionTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 在事务中执行回调。
     *
     * @param action 事务内动作
     * @return 回调返回值
     */
    public <T> T execute(TransactionCallback<T> action) {
        // 嵌套场景：已在外层事务中，直接复用（避免内层提前提交/回滚）
        if (JdbcTemplate.currentTransactionConnection() != null) {
            try {
                return action.doInTransaction();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new DaoException("事务执行失败：" + e.getMessage(), e);
            }
        }

        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            JdbcTemplate.bindTransactionConnection(conn);

            T result = action.doInTransaction();

            conn.commit();
            return result;
        } catch (Exception e) {
            rollbackQuietly(conn);
            if (e instanceof RuntimeException) {
                // 保留原始异常类型（BusinessException / AuthException / DaoException...）
                throw (RuntimeException) e;
            }
            throw new DaoException("事务执行失败：" + e.getMessage(), e);
        } finally {
            JdbcTemplate.unbindTransactionConnection();
            restoreAndClose(conn);
        }
    }

    /** 静默回滚（回滚失败仅记日志，不掩盖原始异常） */
    private void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
                log.warn("事务已回滚");
            } catch (SQLException e) {
                log.error("事务回滚失败: {}", e.getMessage(), e);
            }
        }
    }

    /** 恢复自动提交并归还连接 */
    private void restoreAndClose(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            log.warn("恢复自动提交失败: {}", e.getMessage());
        }
        try {
            conn.close();
        } catch (SQLException e) {
            log.warn("关闭事务连接失败: {}", e.getMessage());
        }
    }
}

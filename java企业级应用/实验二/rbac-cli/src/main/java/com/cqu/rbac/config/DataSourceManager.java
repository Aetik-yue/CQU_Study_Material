package com.cqu.rbac.config;

import com.cqu.rbac.common.exception.DaoException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 数据源管理器（HikariCP 连接池）。
 * <p>设计模式：单例 Singleton——双重校验锁 + volatile 保证线程安全。</p>
 * <p>特色功能：首次启动时若目标数据库不存在，自动用服务器级连接创建数据库，
 * 实现"开箱即用"（无需手工执行 CREATE DATABASE）。</p>
 */
public final class DataSourceManager {

    private static final Logger log = LoggerFactory.getLogger(DataSourceManager.class);

    /** MySQL "未知数据库"错误码 */
    private static final int ERR_UNKNOWN_DATABASE = 1049;
    /** MySQL 常用连接参数 */
    private static final String DEFAULT_DB_CHARSET = "utf8mb4";

    private static volatile DataSourceManager instance;

    private volatile HikariDataSource dataSource;

    private DataSourceManager() {
    }

    /**
     * 获取单例（设计模式：单例——双重校验锁）。
     */
    public static DataSourceManager getInstance() {
        if (instance == null) {
            synchronized (DataSourceManager.class) {
                if (instance == null) {
                    instance = new DataSourceManager();
                }
            }
        }
        return instance;
    }

    /**
     * 获取数据源（HikariDataSource），首次调用时初始化并自动建库。
     */
    public DataSource getDataSource() {
        if (dataSource == null || dataSource.isClosed()) {
            synchronized (this) {
                if (dataSource == null || dataSource.isClosed()) {
                    init();
                }
            }
        }
        return dataSource;
    }

    /** 初始化连接池：先保证数据库存在，再创建 Hikari 池 */
    private void init() {
        ConfigManager config = ConfigManager.getInstance();

        // 1) 确保目标数据库存在（不存在则自动创建）
        ensureDatabaseExists(config);

        // 2) 构建 HikariCP 连接池
        HikariConfig hikari = new HikariConfig();
        hikari.setPoolName("RbacHikariPool");
        hikari.setDriverClassName(config.getString("jdbc.driver", "com.mysql.cj.jdbc.Driver"));
        hikari.setJdbcUrl(config.getString("jdbc.url"));
        hikari.setUsername(config.getString("jdbc.username"));
        hikari.setPassword(config.getString("jdbc.password"));
        hikari.setMaximumPoolSize(config.getInt("pool.maximumPoolSize", 10));
        hikari.setMinimumIdle(config.getInt("pool.minimumIdle", 2));
        hikari.setConnectionTimeout(config.getInt("pool.connectionTimeout", 10000));
        // 连接有效性检测
        hikari.setConnectionTestQuery("SELECT 1");

        try {
            this.dataSource = new HikariDataSource(hikari);
            log.info("数据库连接池初始化完成（HikariCP，最大连接数 {}）", hikari.getMaximumPoolSize());
        } catch (Exception e) {
            throw new DaoException("初始化数据库连接池失败：" + e.getMessage()
                    + "（请确认 MySQL 已启动、db.properties 账号密码正确）", e);
        }
    }

    /**
     * 确保 jdbc.url 中指定的数据库存在；若不存在则连接服务器并创建。
     * <p>这是"首次启动自动建库"特色功能的实现点。</p>
     */
    private void ensureDatabaseExists(ConfigManager config) {
        String url = config.getString("jdbc.url");
        String user = config.getString("jdbc.username");
        String password = config.getString("jdbc.password");
        String driver = config.getString("jdbc.driver", "com.mysql.cj.jdbc.Driver");

        String dbName = extractDatabaseName(url);
        if (dbName == null || dbName.isEmpty()) {
            return; // 未指定库名，跳过（连接失败时由连接池给出错误）
        }

        try {
            Class.forName(driver); // 触发驱动注册
        } catch (ClassNotFoundException e) {
            throw new DaoException("MySQL 驱动未找到：" + driver, e);
        }

        // 先尝试直连目标库
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            log.debug("目标数据库 [{}] 已存在，跳过自动建库", dbName);
            return;
        } catch (SQLException e) {
            if (e.getErrorCode() != ERR_UNKNOWN_DATABASE) {
                throw new DaoException("连接数据库失败：" + e.getMessage()
                        + "（请确认 MySQL 已启动、账号密码正确）", e);
            }
            log.warn("数据库 [{}] 不存在，正在自动创建...", dbName);
        }

        // 连接服务器（去掉库名）并创建数据库
        String serverUrl = stripDatabase(url);
        String createSql = "CREATE DATABASE IF NOT EXISTS `" + dbName + "` DEFAULT CHARACTER SET "
                + DEFAULT_DB_CHARSET + " COLLATE " + DEFAULT_DB_CHARSET + "_general_ci";
        try (Connection conn = DriverManager.getConnection(serverUrl, user, password);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createSql);
            log.info("数据库 [{}] 创建成功", dbName);
        } catch (SQLException e) {
            throw new DaoException("自动创建数据库 [" + dbName + "] 失败：" + e.getMessage()
                    + "（请手工执行 CREATE DATABASE " + dbName + " 或检查账号权限）", e);
        }
    }

    /**
     * 从 JDBC URL 中提取数据库名。
     * <p>如 jdbc:mysql://localhost:3306/rbac_db?xx=1 → rbac_db；无库名时返回空串。</p>
     */
    private String extractDatabaseName(String url) {
        if (url == null) {
            return "";
        }
        int schemeIdx = url.indexOf("://");
        int hostEnd = schemeIdx < 0 ? 0 : schemeIdx + 3;
        int slashAfterHost = url.indexOf('/', hostEnd);
        if (slashAfterHost < 0) {
            return "";
        }
        int question = url.indexOf('?', slashAfterHost);
        String path = question < 0 ? url.substring(slashAfterHost + 1)
                : url.substring(slashAfterHost + 1, question);
        return path.trim();
    }

    /**
     * 去掉 JDBC URL 中的数据库名（连接服务器级 URL）。
     * <p>如 jdbc:mysql://localhost:3306/rbac_db?xx=1 → jdbc:mysql://localhost:3306/?xx=1</p>
     */
    private String stripDatabase(String url) {
        int schemeIdx = url.indexOf("://");
        int hostEnd = schemeIdx < 0 ? 0 : schemeIdx + 3;
        int slashAfterHost = url.indexOf('/', hostEnd);
        if (slashAfterHost < 0) {
            return url;
        }
        int question = url.indexOf('?', slashAfterHost);
        if (question < 0) {
            return url.substring(0, slashAfterHost + 1);
        }
        return url.substring(0, slashAfterHost + 1) + url.substring(question);
    }

    /** 获取底层 Hikari 数据源（供需要 Hikari 特有 API 的场景使用） */
    public HikariDataSource getHikariDataSource() {
        getDataSource();
        return dataSource;
    }

    /** 关闭连接池（程序退出前调用） */
    public synchronized void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            log.info("数据库连接池已关闭");
        }
    }
}

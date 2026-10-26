package com.cqu.rbac.common.exception;

/**
 * 数据访问异常：包装 JDBC 相关的 SQLException 等底层异常。
 * <p>业务层/CLI 层不需要感知底层 SQL 细节，统一转为本异常上抛。</p>
 */
public class DaoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 默认错误码 */
    public static final String DEFAULT_CODE = "DAO_ERROR";

    private final String code;

    public DaoException(String message) {
        this(DEFAULT_CODE, message);
    }

    public DaoException(String message, Throwable cause) {
        this(DEFAULT_CODE, message, cause);
    }

    public DaoException(String code, String message) {
        super(message);
        this.code = code;
    }

    public DaoException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

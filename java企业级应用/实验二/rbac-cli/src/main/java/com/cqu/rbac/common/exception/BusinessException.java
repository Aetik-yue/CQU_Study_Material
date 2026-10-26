package com.cqu.rbac.common.exception;

/**
 * 业务异常：业务规则被违反时抛出（如重名、绑定冲突、删除保护等）。
 * <p>携带错误码 + 可读信息，由 CLI 层统一捕获并友好展示，不打印堆栈。</p>
 * <p>可选携带 {@code field}（出错字段名）：CLI 表单层据此实现"只重填出错项"，
 * 见 {@code AbstractScreen#submitWithRefill}。</p>
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 默认错误码 */
    public static final String DEFAULT_CODE = "BIZ_ERROR";

    /** 错误码 */
    private final String code;
    /** 出错字段名（可为 null，表示与具体字段无关） */
    private final String field;

    public BusinessException(String message) {
        this(DEFAULT_CODE, message);
    }

    public BusinessException(String message, Throwable cause) {
        this(DEFAULT_CODE, message, cause);
    }

    public BusinessException(String code, String message) {
        this(code, message, (String) null);
    }

    public BusinessException(String code, String message, String field) {
        super(message);
        this.code = code;
        this.field = field;
    }

    public BusinessException(String code, String message, Throwable cause) {
        this(code, message, null, cause);
    }

    public BusinessException(String code, String message, String field, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.field = field;
    }

    public String getCode() {
        return code;
    }

    /** 出错字段名（可能为 null） */
    public String getField() {
        return field;
    }
}

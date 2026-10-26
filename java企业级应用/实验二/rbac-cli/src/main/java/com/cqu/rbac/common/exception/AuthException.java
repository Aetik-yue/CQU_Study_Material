package com.cqu.rbac.common.exception;

/**
 * 认证/鉴权异常：未登录、账号锁定、账号禁用、密码错误、权限不足等场景。
 * <p>继承 {@link BusinessException}，便于 CLI 层统一捕获；动态代理层单独识别本类型以记录"鉴权失败"审计。</p>
 */
public class AuthException extends BusinessException {

    private static final long serialVersionUID = 1L;

    /** 错误码：未登录 */
    public static final String CODE_NOT_LOGIN = "AUTH_NOT_LOGIN";
    /** 错误码：权限不足 */
    public static final String CODE_NO_PERMISSION = "AUTH_NO_PERMISSION";
    /** 错误码：账号锁定 */
    public static final String CODE_ACCOUNT_LOCKED = "AUTH_ACCOUNT_LOCKED";
    /** 错误码：账号禁用 */
    public static final String CODE_ACCOUNT_DISABLED = "AUTH_ACCOUNT_DISABLED";
    /** 错误码：登录失败（用户名或密码错误） */
    public static final String CODE_LOGIN_FAILED = "AUTH_LOGIN_FAILED";

    public AuthException(String code, String message) {
        super(code, message);
    }

    public AuthException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }
}

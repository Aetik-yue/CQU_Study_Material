package com.cqu.rbac.common.result;

/**
 * 统一返回体（泛型）。
 * <p>用于 CLI 层对"服务调用结果 + 提示文案"的统一封装与展示。</p>
 *
 * @param <T> 数据载荷类型
 */
public class Result<T> {

    /** 成功状态码 */
    public static final String CODE_SUCCESS = "0";
    /** 失败状态码 */
    public static final String CODE_FAILURE = "1";

    private boolean success;
    private String code;
    private String message;
    private T data;

    public Result() {
    }

    public Result(boolean success, String code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // ---------------- 静态工厂 ----------------

    public static <T> Result<T> success() {
        return new Result<>(true, CODE_SUCCESS, "操作成功", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(true, CODE_SUCCESS, "操作成功", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(true, CODE_SUCCESS, message, data);
    }

    /** 仅携带提示语的成功结果（常用于"创建用户 [x] 成功"这类场景） */
    public static <T> Result<T> message(String message) {
        return new Result<>(true, CODE_SUCCESS, message, null);
    }

    public static <T> Result<T> failure(String message) {
        return new Result<>(false, CODE_FAILURE, message, null);
    }

    public static <T> Result<T> failure(String code, String message) {
        return new Result<>(false, code, message, null);
    }

    // ---------------- getter / setter ----------------

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Result{success=" + success + ", code='" + code + "', message='" + message + "'}";
    }
}

package com.cqu.rbac.common.util;

import com.cqu.rbac.common.exception.BusinessException;

import java.util.regex.Pattern;

/**
 * 输入校验工具：统一的非空/长度/格式校验，校验失败抛出业务异常并携带友好提示。
 * <p>每个校验方法提供一个"带字段键"的重载：异常中携带 {@code field}（如 username/email），
 * CLI 表单层可据此只重填出错字段，无需整表重输。</p>
 */
public final class ValidateUtil {

    private ValidateUtil() {
    }

    /** 是否为空（null 或纯空白） */
    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /** 去空白；空串返回 null */
    public static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    // ---------------- 非空 + 长度 ----------------

    /**
     * 必填 + 长度校验。
     */
    public static void requireNonBlank(String value, String field, int min, int max) {
        if (isBlank(value)) {
            throw new BusinessException("E0901", field + "不能为空");
        }
        int len = value.trim().length();
        if (len < min || len > max) {
            throw new BusinessException("E0902", field + "长度必须在 " + min + "-" + max + " 个字符之间");
        }
    }

    /**
     * 必填 + 长度校验（带字段键，供表单定位重填）。
     */
    public static void requireNonBlank(String value, String fieldKey, String fieldLabel, int min, int max) {
        if (isBlank(value)) {
            throw new BusinessException("E0901", fieldLabel + "不能为空", fieldKey);
        }
        int len = value.trim().length();
        if (len < min || len > max) {
            throw new BusinessException("E0902", fieldLabel + "长度必须在 " + min + "-" + max + " 个字符之间", fieldKey);
        }
    }

    // ---------------- 可空 + 长度上限 ----------------

    /**
     * 可空字段的长度上限校验（为空则跳过）。
     */
    public static void optionalMaxLength(String value, String field, int max) {
        if (!isBlank(value) && value.trim().length() > max) {
            throw new BusinessException("E0903", field + "长度不能超过 " + max + " 个字符");
        }
    }

    /**
     * 可空字段的长度上限校验（带字段键）。
     */
    public static void optionalMaxLength(String value, String fieldKey, String fieldLabel, int max) {
        if (!isBlank(value) && value.trim().length() > max) {
            throw new BusinessException("E0903", fieldLabel + "长度不能超过 " + max + " 个字符", fieldKey);
        }
    }

    // ---------------- 格式 ----------------

    /**
     * 必填字段格式校验。
     */
    public static void requireMatch(String value, String field, Pattern pattern, String hint) {
        if (isBlank(value) || !pattern.matcher(value.trim()).matches()) {
            throw new BusinessException("E0904", field + "格式不正确（" + hint + "）");
        }
    }

    /**
     * 必填字段格式校验（带字段键）。
     */
    public static void requireMatch(String value, String fieldKey, String fieldLabel,
                                    Pattern pattern, String hint) {
        if (isBlank(value) || !pattern.matcher(value.trim()).matches()) {
            throw new BusinessException("E0904", fieldLabel + "格式不正确（" + hint + "）", fieldKey);
        }
    }

    /**
     * 可空字段格式校验（为空则跳过）。
     */
    public static void optionalMatch(String value, String field, Pattern pattern, String hint) {
        if (isBlank(value)) {
            return;
        }
        if (!pattern.matcher(value.trim()).matches()) {
            throw new BusinessException("E0904", field + "格式不正确（" + hint + "）");
        }
    }

    /**
     * 可空字段格式校验（带字段键）。
     */
    public static void optionalMatch(String value, String fieldKey, String fieldLabel,
                                     Pattern pattern, String hint) {
        if (isBlank(value)) {
            return;
        }
        if (!pattern.matcher(value.trim()).matches()) {
            throw new BusinessException("E0904", fieldLabel + "格式不正确（" + hint + "）", fieldKey);
        }
    }
}

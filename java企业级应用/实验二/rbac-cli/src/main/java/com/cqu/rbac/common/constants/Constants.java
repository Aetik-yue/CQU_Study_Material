package com.cqu.rbac.common.constants;

/**
 * 全局常量。
 * <p>集中定义应用中多处使用的固定值，避免魔法数字/字符串散落。</p>
 */
public final class Constants {

    private Constants() {
        // 常量类不允许实例化
    }

    /** 应用名称 */
    public static final String APP_NAME = "RBAC 权限管理系统";
    /** 应用版本 */
    public static final String APP_VERSION = "1.0.0";

    // ---------------- 内置角色编码 ----------------
    /** 超级管理员 */
    public static final String ROLE_CODE_ADMIN = "ADMIN";
    /** 用户管理员 */
    public static final String ROLE_CODE_USER_MANAGER = "USER_MANAGER";
    /** 审计员 */
    public static final String ROLE_CODE_AUDITOR = "AUDITOR";
    /** 普通用户 */
    public static final String ROLE_CODE_USER = "USER";
    /** 审计主管（角色继承演示：父角色为 AUDITOR） */
    public static final String ROLE_CODE_AUDITOR_LEAD = "AUDITOR_LEAD";

    // ---------------- 默认账号 ----------------
    /** 默认管理员账号 */
    public static final String DEFAULT_ADMIN_USERNAME = "admin";
    /** 默认管理员密码（首次启动自动创建） */
    public static final String DEFAULT_ADMIN_PASSWORD = "admin123";
    /** 演示账号统一密码 */
    public static final String DEFAULT_DEMO_PASSWORD = "123456";

    // ---------------- 登录安全策略 ----------------
    /** 连续登录失败最大次数 */
    public static final int MAX_LOGIN_FAIL = 5;
    /** 锁定时长（分钟） */
    public static final int LOCK_MINUTES = 10;
    /** 登录页单次会话最大尝试次数 */
    public static final int MAX_LOGIN_ATTEMPTS = 5;

    // ---------------- 分页 ----------------
    /** 默认分页大小 */
    public static final int DEFAULT_PAGE_SIZE = 10;
    /** 分页大小上限 */
    public static final int MAX_PAGE_SIZE = 100;

    // ---------------- 审计模块名 ----------------
    public static final String MODULE_AUTH = "认证";
    public static final String MODULE_USER = "用户管理";
    public static final String MODULE_ROLE = "角色管理";
    public static final String MODULE_PERMISSION = "权限管理";
    public static final String MODULE_RESOURCE = "资源管理";
    /** 审计日志查询模块 */
    public static final String MODULE_AUDIT = "审计管理";
    /** 个人中心（修改本人密码等） */
    public static final String MODULE_PROFILE = "个人中心";
    /** 权限校验失败专用审计模块 */
    public static final String MODULE_AUTHZ = "权限校验";

    /** 审计客户端标识 */
    public static final String CLIENT_CLI = "CLI";

    // ---------------- 状态值 ----------------
    /** 启用 */
    public static final int STATUS_ENABLED = 1;
    /** 禁用 */
    public static final int STATUS_DISABLED = 0;

    /** 根节点的 parent_id 约定值 */
    public static final long ROOT_PARENT_ID = 0L;

    /** 审计失败原因最大存储长度 */
    public static final int AUDIT_ERROR_MAX_LEN = 400;
    /** 审计操作对象最大存储长度 */
    public static final int AUDIT_TARGET_MAX_LEN = 190;
}

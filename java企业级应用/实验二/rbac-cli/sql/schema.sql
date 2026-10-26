-- ==========================================================
-- RBAC 权限管理系统 - 建表脚本（幂等：IF NOT EXISTS，可重复执行）
-- 由程序启动时自动执行；也可手工在 MySQL 客户端执行
-- ==========================================================
-- 数据库（如未创建，请先执行）：
-- CREATE DATABASE IF NOT EXISTS rbac_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
-- USE rbac_db;

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username         VARCHAR(50)  NOT NULL COMMENT '登录名',
    password         VARCHAR(200) NOT NULL COMMENT '密码（PBKDF2加盐哈希）',
    salt             VARCHAR(64)  NOT NULL COMMENT '盐值',
    real_name        VARCHAR(50)           DEFAULT NULL COMMENT '姓名',
    email            VARCHAR(100)          DEFAULT NULL COMMENT '邮箱',
    phone            VARCHAR(20)           DEFAULT NULL COMMENT '电话',
    status           TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
    login_fail_count INT          NOT NULL DEFAULT 0 COMMENT '连续登录失败次数',
    lock_until       DATETIME              DEFAULT NULL COMMENT '锁定截止时间',
    create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    role_name   VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code   VARCHAR(50) NOT NULL COMMENT '角色编码',
    parent_id   BIGINT               DEFAULT 0 COMMENT '父角色id（0表示无，角色继承）',
    description VARCHAR(200)         DEFAULT NULL,
    status      TINYINT     NOT NULL DEFAULT 1,
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    permission_name VARCHAR(80)  NOT NULL COMMENT '权限名称',
    permission_code VARCHAR(100) NOT NULL COMMENT '权限编码，如 user:create',
    permission_type VARCHAR(20)  NOT NULL COMMENT 'MENU / OPERATION / RESOURCE',
    parent_id       BIGINT       NOT NULL DEFAULT 0 COMMENT '父权限id（权限树）',
    resource_id     BIGINT                DEFAULT NULL COMMENT '关联资源id',
    path            VARCHAR(200)          DEFAULT NULL COMMENT '菜单路径/标识',
    sort_order      INT          NOT NULL DEFAULT 0 COMMENT '排序',
    status          TINYINT      NOT NULL DEFAULT 1,
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_permission_code (permission_code),
    KEY idx_parent (parent_id),
    KEY idx_type (permission_type)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='权限表';

-- 资源表
CREATE TABLE IF NOT EXISTS sys_resource (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    resource_name VARCHAR(80)  NOT NULL COMMENT '资源名称',
    resource_code VARCHAR(100) NOT NULL COMMENT '资源编码',
    resource_type VARCHAR(20)  NOT NULL COMMENT 'MENU / FILE / API / DATA',
    resource_path VARCHAR(255)          DEFAULT NULL COMMENT '资源路径/URI',
    description   VARCHAR(200)          DEFAULT NULL,
    status        TINYINT      NOT NULL DEFAULT 1,
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_resource_code (resource_code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='资源表';

-- 用户-角色 关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id      BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_role (role_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='用户角色关联';

-- 角色-权限 关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id            BIGINT NOT NULL AUTO_INCREMENT,
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_perm (role_id, permission_id),
    KEY idx_perm (permission_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='角色权限关联';

-- 审计日志表
CREATE TABLE IF NOT EXISTS sys_audit_log (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    user_id       BIGINT                DEFAULT NULL,
    username      VARCHAR(50)           DEFAULT NULL COMMENT '操作人用户名',
    module        VARCHAR(50)           DEFAULT NULL COMMENT '模块：用户/角色/权限/资源/认证',
    operation     VARCHAR(50)           DEFAULT NULL COMMENT '操作：LOGIN/CREATE/UPDATE/DELETE/ASSIGN...',
    target_object VARCHAR(200)          DEFAULT NULL COMMENT '操作对象',
    detail        VARCHAR(500)          DEFAULT NULL COMMENT '操作详情（脱敏）',
    result        VARCHAR(20)  NOT NULL DEFAULT 'SUCCESS' COMMENT 'SUCCESS / FAILURE',
    duration_ms   BIGINT       NOT NULL DEFAULT 0 COMMENT '耗时(毫秒)',
    error_message VARCHAR(500)          DEFAULT NULL COMMENT '失败原因',
    client        VARCHAR(50)           DEFAULT 'CLI' COMMENT '客户端标识',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user (username),
    KEY idx_module (module),
    KEY idx_time (create_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='审计日志表';

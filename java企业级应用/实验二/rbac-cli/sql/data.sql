-- ==========================================================
-- RBAC 权限管理系统 - 手工初始化参考脚本（可选）
-- 说明：
--   1. 程序首次启动时 DataInitializer 会自动完成同样的初始化（推荐方式），
--      本文档仅作为手工初始化的参考 / 数据字典；
--   2. 仅在"库为空且自动初始化被关闭（db.autoInit=false）"时手工执行本脚本；
--   3. 重复执行前请先清空相关表，或改用程序自动初始化。
-- 默认账号：admin / admin123
-- ==========================================================

USE rbac_db;

-- ---------- 资源 ----------
INSERT INTO sys_resource (id, resource_name, resource_code, resource_type, resource_path, description, status) VALUES
(1, '财务报表演示资源', 'report:finance', 'FILE', '/reports/finance-2025.xlsx', '演示用文件资源：财务部 2025 年度报表', 1),
(2, '用户接口演示资源', 'api:user', 'API', '/api/v1/user', '演示用接口资源：用户服务 REST API', 1);

-- ---------- 权限（33 条）----------
-- 菜单权限（MENU）
INSERT INTO sys_permission (id, permission_name, permission_code, permission_type, parent_id, resource_id, path, sort_order, status) VALUES
(1,  '系统管理',     'menu:system',     'MENU', 0, NULL, 'system',     1, 1),
(2,  '用户管理',     'menu:user',       'MENU', 1, NULL, 'user',       2, 1),
(3,  '角色管理',     'menu:role',       'MENU', 1, NULL, 'role',       3, 1),
(4,  '权限管理',     'menu:permission', 'MENU', 1, NULL, 'permission', 4, 1),
(5,  '资源管理',     'menu:resource',   'MENU', 1, NULL, 'resource',   5, 1),
(6,  '审计日志',     'menu:audit',      'MENU', 1, NULL, 'audit',      6, 1);
-- 用户模块操作权限（OPERATION）
INSERT INTO sys_permission (id, permission_name, permission_code, permission_type, parent_id, resource_id, path, sort_order, status) VALUES
(7,  '查询用户',   'user:list',           'OPERATION', 2, NULL, NULL, 10, 1),
(8,  '新增用户',   'user:create',         'OPERATION', 2, NULL, NULL, 11, 1),
(9,  '修改用户',   'user:update',         'OPERATION', 2, NULL, NULL, 12, 1),
(10, '删除用户',   'user:delete',         'OPERATION', 2, NULL, NULL, 13, 1),
(11, '分配角色',   'user:assign-role',    'OPERATION', 2, NULL, NULL, 14, 1),
(12, '重置密码',   'user:reset-password', 'OPERATION', 2, NULL, NULL, 15, 1),
(13, '启用禁用',   'user:toggle',         'OPERATION', 2, NULL, NULL, 16, 1);
-- 角色模块
INSERT INTO sys_permission (id, permission_name, permission_code, permission_type, parent_id, resource_id, path, sort_order, status) VALUES
(14, '查询角色', 'role:list',              'OPERATION', 3, NULL, NULL, 20, 1),
(15, '新增角色', 'role:create',            'OPERATION', 3, NULL, NULL, 21, 1),
(16, '修改角色', 'role:update',            'OPERATION', 3, NULL, NULL, 22, 1),
(17, '删除角色', 'role:delete',            'OPERATION', 3, NULL, NULL, 23, 1),
(18, '分配权限', 'role:assign-permission', 'OPERATION', 3, NULL, NULL, 24, 1);
-- 权限模块
INSERT INTO sys_permission (id, permission_name, permission_code, permission_type, parent_id, resource_id, path, sort_order, status) VALUES
(19, '查询权限', 'permission:list',   'OPERATION', 4, NULL, NULL, 30, 1),
(20, '新增权限', 'permission:create', 'OPERATION', 4, NULL, NULL, 31, 1),
(21, '修改权限', 'permission:update', 'OPERATION', 4, NULL, NULL, 32, 1),
(22, '删除权限', 'permission:delete', 'OPERATION', 4, NULL, NULL, 33, 1);
-- 资源模块
INSERT INTO sys_permission (id, permission_name, permission_code, permission_type, parent_id, resource_id, path, sort_order, status) VALUES
(23, '查询资源',     'resource:list',   'OPERATION', 5, NULL, NULL, 40, 1),
(24, '新增资源',     'resource:create', 'OPERATION', 5, NULL, NULL, 41, 1),
(25, '修改资源',     'resource:update', 'OPERATION', 5, NULL, NULL, 42, 1),
(26, '删除资源',     'resource:delete', 'OPERATION', 5, NULL, NULL, 43, 1),
(27, '资源访问校验', 'resource:check',  'OPERATION', 5, NULL, NULL, 44, 1);
-- 审计模块
INSERT INTO sys_permission (id, permission_name, permission_code, permission_type, parent_id, resource_id, path, sort_order, status) VALUES
(28, '查询审计日志', 'audit:list',      'OPERATION', 6, NULL, NULL, 50, 1),
(29, '查询登录日志', 'audit:login',     'OPERATION', 6, NULL, NULL, 51, 1),
(30, '查询操作日志', 'audit:operation', 'OPERATION', 6, NULL, NULL, 52, 1);
-- 个人与资源权限
INSERT INTO sys_permission (id, permission_name, permission_code, permission_type, parent_id, resource_id, path, sort_order, status) VALUES
(31, '修改本人密码',     'profile:password', 'OPERATION', 0, NULL, 'profile', 90, 1),
(32, '财务报表资源权限', 'finance:report',   'RESOURCE',  0, 1,    NULL,      60, 1),
(33, '用户接口资源权限', 'api:user',         'RESOURCE',  0, 2,    NULL,      61, 1);

-- ---------- 角色（含继承关系）----------
INSERT INTO sys_role (id, role_name, role_code, parent_id, description, status) VALUES
(1, '超级管理员', 'ADMIN',        0, '系统内置超级管理员，拥有全部权限', 1),
(2, '用户管理员', 'USER_MANAGER', 0, '负责用户的日常管理（增删改查、分配角色、重置密码）', 1),
(3, '审计员',     'AUDITOR',      0, '只读查看各模块并拥有审计日志权限', 1),
(4, '普通用户',   'USER',         0, '仅可登录与修改本人密码', 1),
(5, '审计主管',   'AUDITOR_LEAD', 3, '审计员的上级角色（演示角色继承：自身不配权限，自动继承父角色权限）', 1);

-- ---------- 角色-权限 授权 ----------
-- ADMIN：全部权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission;
-- USER_MANAGER
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(2, 1), (2, 2), (2, 7), (2, 8), (2, 9), (2, 10), (2, 11), (2, 12), (2, 13), (2, 14), (2, 27), (2, 33);
-- AUDITOR
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(3, 1), (3, 6), (3, 7), (3, 14), (3, 19), (3, 23), (3, 28), (3, 29), (3, 30), (3, 32);
-- USER
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(4, 31);
-- AUDITOR_LEAD：不直接授权（权限从父角色 AUDITOR 继承）

-- ---------- 用户 ----------
-- 说明：密码为 PBKDF2 加盐哈希（格式 saltHex:hashHex），salt 列存 saltHex。
-- 下面的 admin 账号（密码 admin123）哈希由系统生成，可直接使用。
INSERT INTO sys_user (id, username, password, salt, real_name, email, phone, status, login_fail_count) VALUES
(1, 'admin', '55aabe485f8af9e080786bf04097bd2d:44661bae5417381c473f055fc455834dfab4ad7c5a4234a4dcf38ab065c5afe2',
 '55aabe485f8af9e080786bf04097bd2d', '超级管理员', NULL, NULL, 1, 0);

-- 演示账号（manager1 / auditor1 / auditlead1 / user1，密码均为 123456）
-- 因哈希随机加盐，请让程序自动创建，或使用系统"新增用户"功能创建后再绑定角色，例如：
--   INSERT INTO sys_user_role (user_id, role_id) SELECT u.id, 4 FROM sys_user u WHERE u.username = 'manager1';

INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1); -- admin -> ADMIN

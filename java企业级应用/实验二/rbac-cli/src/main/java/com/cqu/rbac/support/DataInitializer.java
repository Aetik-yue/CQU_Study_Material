package com.cqu.rbac.support;

import com.cqu.rbac.common.constants.Constants;
import com.cqu.rbac.common.enums.PermissionType;
import com.cqu.rbac.common.enums.ResourceType;
import com.cqu.rbac.common.exception.DaoException;
import com.cqu.rbac.config.ConfigManager;
import com.cqu.rbac.dao.PermissionDao;
import com.cqu.rbac.dao.ResourceDao;
import com.cqu.rbac.dao.RoleDao;
import com.cqu.rbac.dao.UserDao;
import com.cqu.rbac.domain.entity.Permission;
import com.cqu.rbac.domain.entity.Resource;
import com.cqu.rbac.domain.entity.Role;
import com.cqu.rbac.domain.entity.User;
import com.cqu.rbac.jdbc.JdbcTemplate;
import com.cqu.rbac.security.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据初始化器（特色功能：首次启动自动建表 + 初始化数据，开箱即用）。
 * <p>执行顺序：建表（schema.sql）→ 资源 → 权限 → 角色与授权 → 用户（admin + 可选演示账号）。</p>
 */
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final JdbcTemplate jdbcTemplate;
    private final PermissionDao permissionDao;
    private final RoleDao roleDao;
    private final UserDao userDao;
    private final ResourceDao resourceDao;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(JdbcTemplate jdbcTemplate, PermissionDao permissionDao, RoleDao roleDao,
                           UserDao userDao, ResourceDao resourceDao, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.permissionDao = permissionDao;
        this.roleDao = roleDao;
        this.userDao = userDao;
        this.resourceDao = resourceDao;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 初始化入口：各步骤均幂等（有数据则跳过）。
     */
    public void initialize() {
        ConfigManager config = ConfigManager.getInstance();
        if (config.getBoolean("db.autoInit", true)) {
            executeSchema();
        }
        if (resourceDao.count() == 0) {
            initResources();
        }
        if (permissionDao.count() == 0) {
            initPermissions();
        }
        if (roleDao.count() == 0) {
            initRolesAndGrants();
        }
        if (userDao.count() == 0) {
            initUsers(config.getBoolean("db.initDemoData", true));
        }
    }

    // ---------------- 建表 ----------------

    /** 读取 classpath:schema.sql 并逐条执行（幂等 CREATE TABLE IF NOT EXISTS） */
    private void executeSchema() {
        String script = readResource("schema.sql");
        List<String> statements = splitStatements(script);
        for (String sql : statements) {
            jdbcTemplate.update(sql);
        }
        log.info("数据库表结构检查完成（{} 条建表语句）", statements.size());
    }

    /** 读取 classpath 资源为字符串 */
    private String readResource(String name) {
        try (InputStream in = DataInitializer.class.getClassLoader().getResourceAsStream(name)) {
            if (in == null) {
                throw new DaoException("初始化脚本未找到：" + name);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            throw new DaoException("读取初始化脚本失败：" + name, e);
        }
    }

    /** 按 ';' 切分 SQL 语句（过滤空段与 '--' 注释行） */
    private List<String> splitStatements(String script) {
        StringBuilder cleaned = new StringBuilder();
        for (String line : script.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                continue;
            }
            cleaned.append(line).append('\n');
        }
        List<String> statements = new ArrayList<>();
        for (String part : cleaned.toString().split(";")) {
            String sql = part.trim();
            if (!sql.isEmpty()) {
                statements.add(sql);
            }
        }
        return statements;
    }

    // ---------------- 资源 ----------------

    private void initResources() {
        Resource report = new Resource("财务报表演示资源", "report:finance", ResourceType.FILE.name());
        report.setResourcePath("/reports/finance-2025.xlsx");
        report.setDescription("演示用文件资源：财务部 2025 年度报表");
        resourceDao.insert(report);

        Resource api = new Resource("用户接口演示资源", "api:user", ResourceType.API.name());
        api.setResourcePath("/api/v1/user");
        api.setDescription("演示用接口资源：用户服务 REST API");
        resourceDao.insert(api);

        log.info("初始化资源 {} 条", 2);
    }

    // ---------------- 权限 ----------------

    /** 权限种子（父权限用编码表达，便于阅读；落库时转换为 parent_id） */
    private static class PermSeed {
        final String code;
        final String name;
        final String type;
        final String parentCode;
        final String path;
        final int sort;
        final String resourceCode;

        PermSeed(String code, String name, String type, String parentCode, String path, int sort, String resourceCode) {
            this.code = code;
            this.name = name;
            this.type = type;
            this.parentCode = parentCode;
            this.path = path;
            this.sort = sort;
            this.resourceCode = resourceCode;
        }
    }

    private void initPermissions() {
        List<PermSeed> seeds = Arrays.asList(
                // ---- 菜单（MENU） ----
                new PermSeed("menu:system", "系统管理", PermissionType.MENU.name(), null, "system", 1, null),
                new PermSeed("menu:user", "用户管理", PermissionType.MENU.name(), "menu:system", "user", 2, null),
                new PermSeed("menu:role", "角色管理", PermissionType.MENU.name(), "menu:system", "role", 3, null),
                new PermSeed("menu:permission", "权限管理", PermissionType.MENU.name(), "menu:system", "permission", 4, null),
                new PermSeed("menu:resource", "资源管理", PermissionType.MENU.name(), "menu:system", "resource", 5, null),
                new PermSeed("menu:audit", "审计日志", PermissionType.MENU.name(), "menu:system", "audit", 6, null),

                // ---- 用户模块（OPERATION） ----
                new PermSeed("user:list", "查询用户", PermissionType.OPERATION.name(), "menu:user", null, 10, null),
                new PermSeed("user:create", "新增用户", PermissionType.OPERATION.name(), "menu:user", null, 11, null),
                new PermSeed("user:update", "修改用户", PermissionType.OPERATION.name(), "menu:user", null, 12, null),
                new PermSeed("user:delete", "删除用户", PermissionType.OPERATION.name(), "menu:user", null, 13, null),
                new PermSeed("user:assign-role", "分配角色", PermissionType.OPERATION.name(), "menu:user", null, 14, null),
                new PermSeed("user:reset-password", "重置密码", PermissionType.OPERATION.name(), "menu:user", null, 15, null),
                new PermSeed("user:toggle", "启用禁用", PermissionType.OPERATION.name(), "menu:user", null, 16, null),

                // ---- 角色模块 ----
                new PermSeed("role:list", "查询角色", PermissionType.OPERATION.name(), "menu:role", null, 20, null),
                new PermSeed("role:create", "新增角色", PermissionType.OPERATION.name(), "menu:role", null, 21, null),
                new PermSeed("role:update", "修改角色", PermissionType.OPERATION.name(), "menu:role", null, 22, null),
                new PermSeed("role:delete", "删除角色", PermissionType.OPERATION.name(), "menu:role", null, 23, null),
                new PermSeed("role:assign-permission", "分配权限", PermissionType.OPERATION.name(), "menu:role", null, 24, null),

                // ---- 权限模块 ----
                new PermSeed("permission:list", "查询权限", PermissionType.OPERATION.name(), "menu:permission", null, 30, null),
                new PermSeed("permission:create", "新增权限", PermissionType.OPERATION.name(), "menu:permission", null, 31, null),
                new PermSeed("permission:update", "修改权限", PermissionType.OPERATION.name(), "menu:permission", null, 32, null),
                new PermSeed("permission:delete", "删除权限", PermissionType.OPERATION.name(), "menu:permission", null, 33, null),

                // ---- 资源模块 ----
                new PermSeed("resource:list", "查询资源", PermissionType.OPERATION.name(), "menu:resource", null, 40, null),
                new PermSeed("resource:create", "新增资源", PermissionType.OPERATION.name(), "menu:resource", null, 41, null),
                new PermSeed("resource:update", "修改资源", PermissionType.OPERATION.name(), "menu:resource", null, 42, null),
                new PermSeed("resource:delete", "删除资源", PermissionType.OPERATION.name(), "menu:resource", null, 43, null),
                new PermSeed("resource:check", "资源访问校验", PermissionType.OPERATION.name(), "menu:resource", null, 44, null),

                // ---- 审计模块 ----
                new PermSeed("audit:list", "查询审计日志", PermissionType.OPERATION.name(), "menu:audit", null, 50, null),
                new PermSeed("audit:login", "查询登录日志", PermissionType.OPERATION.name(), "menu:audit", null, 51, null),
                new PermSeed("audit:operation", "查询操作日志", PermissionType.OPERATION.name(), "menu:audit", null, 52, null),

                // ---- 个人 ----
                new PermSeed("profile:password", "修改本人密码", PermissionType.OPERATION.name(), null, "profile", 90, null),

                // ---- 资源权限（RESOURCE，关联具体资源） ----
                new PermSeed("finance:report", "财务报表资源权限", PermissionType.RESOURCE.name(), null, null, 60, "report:finance"),
                new PermSeed("api:user", "用户接口资源权限", PermissionType.RESOURCE.name(), null, null, 61, "api:user")
        );

        // 先建父权限，再建子权限（seeds 已按父先序排列）
        Map<String, Long> codeToId = new LinkedHashMap<>();
        Map<String, Long> resourceCodeToId = new HashMap<>();
        for (Resource r : resourceDao.findAll()) {
            resourceCodeToId.put(r.getResourceCode(), r.getId());
        }

        for (PermSeed seed : seeds) {
            Permission permission = new Permission(seed.name, seed.code, seed.type);
            permission.setParentId(seed.parentCode == null ? Constants.ROOT_PARENT_ID : codeToId.get(seed.parentCode));
            permission.setPath(seed.path);
            permission.setSortOrder(seed.sort);
            if (seed.resourceCode != null) {
                permission.setResourceId(resourceCodeToId.get(seed.resourceCode));
            }
            permissionDao.insert(permission);
            codeToId.put(seed.code, permission.getId());
        }
        log.info("初始化权限 {} 条", seeds.size());
    }

    // ---------------- 角色与授权 ----------------

    private void initRolesAndGrants() {
        Map<String, Long> codeToRoleId = new LinkedHashMap<>();

        // 1) 创建角色
        Role admin = createRole(Constants.ROLE_CODE_ADMIN, "超级管理员", Constants.ROOT_PARENT_ID,
                "系统内置超级管理员，拥有全部权限");
        Role userManager = createRole(Constants.ROLE_CODE_USER_MANAGER, "用户管理员", Constants.ROOT_PARENT_ID,
                "负责用户的日常管理（增删改查、分配角色、重置密码）");
        Role auditor = createRole(Constants.ROLE_CODE_AUDITOR, "审计员", Constants.ROOT_PARENT_ID,
                "只读查看各模块并拥有审计日志权限");
        Role user = createRole(Constants.ROLE_CODE_USER, "普通用户", Constants.ROOT_PARENT_ID,
                "仅可登录与修改本人密码");
        Role auditorLead = createRole(Constants.ROLE_CODE_AUDITOR_LEAD, "审计主管", auditor.getId(),
                "审计员的上级角色（演示角色继承：自身不配权限，自动继承父角色权限）");
        codeToRoleId.put(admin.getRoleCode(), admin.getId());
        codeToRoleId.put(userManager.getRoleCode(), userManager.getId());
        codeToRoleId.put(auditor.getRoleCode(), auditor.getId());
        codeToRoleId.put(user.getRoleCode(), user.getId());
        codeToRoleId.put(auditorLead.getRoleCode(), auditorLead.getId());

        // 2) 权限编码 → id 映射
        Map<String, Long> codeToPermId = new HashMap<>();
        for (Permission p : permissionDao.findAll()) {
            codeToPermId.put(p.getPermissionCode(), p.getId());
        }

        // 3) 授权规则
        // ADMIN：全部权限
        grant(codeToRoleId.get(Constants.ROLE_CODE_ADMIN), codeToPermId, codeToPermId.keySet());

        // USER_MANAGER：系统管理/用户管理菜单 + 用户全部操作 + 角色只读 + 资源校验 + 用户接口资源权限
        grant(codeToRoleId.get(Constants.ROLE_CODE_USER_MANAGER), codeToPermId, Arrays.asList(
                "menu:system", "menu:user",
                "user:list", "user:create", "user:update", "user:delete",
                "user:assign-role", "user:reset-password", "user:toggle",
                "role:list", "resource:check", "api:user"));

        // AUDITOR：系统管理/审计菜单 + 审计全部 + 各模块只读 + 财务报表资源权限
        grant(codeToRoleId.get(Constants.ROLE_CODE_AUDITOR), codeToPermId, Arrays.asList(
                "menu:system", "menu:audit",
                "audit:list", "audit:login", "audit:operation",
                "user:list", "role:list", "permission:list", "resource:list",
                "finance:report"));

        // USER：仅修改本人密码
        grant(codeToRoleId.get(Constants.ROLE_CODE_USER), codeToPermId,
                java.util.Collections.singletonList("profile:password"));

        // AUDITOR_LEAD：不直接授权（权限从父角色 AUDITOR 继承而来）
        log.info("初始化角色 5 个并完成授权");
    }

    /** 创建角色 */
    private Role createRole(String code, String name, Long parentId, String description) {
        Role role = new Role(name, code);
        role.setParentId(parentId == null ? Constants.ROOT_PARENT_ID : parentId);
        role.setDescription(description);
        roleDao.insert(role);
        return role;
    }

    /** 按权限编码批量授权（编码不存在则忽略并告警） */
    private void grant(Long roleId, Map<String, Long> codeToPermId, java.util.Collection<String> codes) {
        List<Long> permissionIds = new ArrayList<>();
        for (String code : codes) {
            Long id = codeToPermId.get(code);
            if (id == null) {
                log.warn("授权时权限编码不存在，已跳过：{}", code);
                continue;
            }
            permissionIds.add(id);
        }
        roleDao.insertRolePermissions(roleId, permissionIds);
    }

    // ---------------- 用户 ----------------

    private void initUsers(boolean demoData) {
        // 默认管理员
        createUser(Constants.DEFAULT_ADMIN_USERNAME, Constants.DEFAULT_ADMIN_PASSWORD, "超级管理员",
                "ADMIN");
        log.info("初始化默认管理员账号：{} / {}", Constants.DEFAULT_ADMIN_USERNAME, Constants.DEFAULT_ADMIN_PASSWORD);

        if (demoData) {
            createUser("manager1", Constants.DEFAULT_DEMO_PASSWORD, "王经理", Constants.ROLE_CODE_USER_MANAGER);
            createUser("auditor1", Constants.DEFAULT_DEMO_PASSWORD, "李审计", Constants.ROLE_CODE_AUDITOR);
            createUser("auditlead1", Constants.DEFAULT_DEMO_PASSWORD, "赵主管", Constants.ROLE_CODE_AUDITOR_LEAD);
            createUser("user1", Constants.DEFAULT_DEMO_PASSWORD, "张三", Constants.ROLE_CODE_USER);
            log.info("初始化演示账号 4 个（manager1 / auditor1 / auditlead1 / user1），密码均为 {}",
                    Constants.DEFAULT_DEMO_PASSWORD);
        }
    }

    /** 创建用户并绑定角色 */
    private void createUser(String username, String rawPassword, String realName, String roleCode) {
        String encoded = passwordEncoder.encode(rawPassword);
        User user = User.builder()
                .username(username)
                .password(encoded)
                .salt(encoded.substring(0, encoded.indexOf(':')))
                .realName(realName)
                .status(Constants.STATUS_ENABLED)
                .build();
        userDao.insert(user);

        Role role = roleDao.findByCode(roleCode);
        if (role != null) {
            userDao.insertUserRoles(user.getId(), java.util.Collections.singletonList(role.getId()));
        } else {
            log.warn("用户 [{}] 绑定角色失败：角色编码 {} 不存在", username, roleCode);
        }
    }
}

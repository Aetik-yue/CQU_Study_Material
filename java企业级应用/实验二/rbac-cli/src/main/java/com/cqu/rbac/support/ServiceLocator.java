package com.cqu.rbac.support;

import com.cqu.rbac.cli.session.SessionManager;
import com.cqu.rbac.config.ConfigManager;
import com.cqu.rbac.config.DataSourceManager;
import com.cqu.rbac.dao.AuditLogDao;
import com.cqu.rbac.dao.PermissionDao;
import com.cqu.rbac.dao.ResourceDao;
import com.cqu.rbac.dao.RoleDao;
import com.cqu.rbac.dao.UserDao;
import com.cqu.rbac.dao.impl.AuditLogDaoImpl;
import com.cqu.rbac.dao.impl.PermissionDaoImpl;
import com.cqu.rbac.dao.impl.ResourceDaoImpl;
import com.cqu.rbac.dao.impl.RoleDaoImpl;
import com.cqu.rbac.dao.impl.UserDaoImpl;
import com.cqu.rbac.event.AuditEventListener;
import com.cqu.rbac.event.EventBus;
import com.cqu.rbac.jdbc.JdbcTemplate;
import com.cqu.rbac.jdbc.TransactionTemplate;
import com.cqu.rbac.proxy.AuditAndAuthInvocationHandler;
import com.cqu.rbac.proxy.ServiceProxyFactory;
import com.cqu.rbac.security.PasswordEncoder;
import com.cqu.rbac.security.Pbkdf2PasswordEncoder;
import com.cqu.rbac.security.PermissionCache;
import com.cqu.rbac.security.PermissionChecker;
import com.cqu.rbac.service.AuditService;
import com.cqu.rbac.service.AuthService;
import com.cqu.rbac.service.PermissionService;
import com.cqu.rbac.service.ResourceService;
import com.cqu.rbac.service.RoleService;
import com.cqu.rbac.service.UserService;
import com.cqu.rbac.service.impl.AuditServiceImpl;
import com.cqu.rbac.service.impl.AuthServiceImpl;
import com.cqu.rbac.service.impl.PermissionServiceImpl;
import com.cqu.rbac.service.impl.ResourceServiceImpl;
import com.cqu.rbac.service.impl.RoleServiceImpl;
import com.cqu.rbac.service.impl.UserServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

/**
 * 服务工厂（组合根 / Service Locator）。
 * <p>设计模式：单例 Singleton + 工厂 Factory——集中组装基础设施、DAO、安全组件、事件总线与
 * "经代理增强的"服务对象；CLI 层只依赖本工厂获取接口。</p>
 */
public final class ServiceLocator {

    private static final Logger log = LoggerFactory.getLogger(ServiceLocator.class);

    private static volatile ServiceLocator instance;

    private volatile boolean initialized = false;

    // ---- 基础设施 ----
    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private TransactionTemplate transactionTemplate;

    // ---- DAO ----
    private UserDao userDao;
    private RoleDao roleDao;
    private PermissionDao permissionDao;
    private ResourceDao resourceDao;
    private AuditLogDao auditLogDao;

    // ---- 安全 / 事件 / 会话 ----
    private PasswordEncoder passwordEncoder;
    private PermissionCache permissionCache;
    private AuthorizationFacade authorizationFacade;
    private PermissionChecker permissionChecker;
    private SessionManager sessionManager;
    private EventBus eventBus;

    // ---- 服务（代理对象） ----
    private UserService userService;
    private RoleService roleService;
    private PermissionService permissionService;
    private ResourceService resourceService;
    private AuditService auditService;
    private AuthService authService;

    // ---- 数据初始化 ----
    private DataInitializer dataInitializer;

    private ServiceLocator() {
    }

    /** 获取单例（双重校验锁） */
    public static ServiceLocator getInstance() {
        if (instance == null) {
            synchronized (ServiceLocator.class) {
                if (instance == null) {
                    instance = new ServiceLocator();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化：组装对象图并注册审计监听器（幂等）。
     */
    public synchronized void init() {
        if (initialized) {
            return;
        }

        // 1) 基础设施
        dataSource = DataSourceManager.getInstance().getDataSource();
        jdbcTemplate = new JdbcTemplate(dataSource);
        transactionTemplate = new TransactionTemplate(dataSource);

        // 2) DAO
        userDao = new UserDaoImpl(jdbcTemplate);
        roleDao = new RoleDaoImpl(jdbcTemplate);
        permissionDao = new PermissionDaoImpl(jdbcTemplate);
        resourceDao = new ResourceDaoImpl(jdbcTemplate);
        auditLogDao = new AuditLogDaoImpl(jdbcTemplate);

        // 3) 安全组件 / 会话 / 事件总线
        passwordEncoder = Pbkdf2PasswordEncoder.getInstance();
        permissionCache = new PermissionCache();
        authorizationFacade = new AuthorizationFacade(userDao, roleDao, permissionDao, resourceDao, permissionCache);
        sessionManager = new SessionManager();
        eventBus = EventBus.getInstance();
        permissionChecker = new PermissionChecker(authorizationFacade, sessionManager);

        // 4) 服务实现 + 动态代理增强（鉴权 + 审计）
        ServiceProxyFactory proxyFactory = new ServiceProxyFactory(permissionChecker, sessionManager, eventBus);
        userService = proxyFactory.createProxy(
                new UserServiceImpl(userDao, roleDao, transactionTemplate, sessionManager,
                        passwordEncoder, authorizationFacade), UserService.class);
        roleService = proxyFactory.createProxy(
                new RoleServiceImpl(roleDao, permissionDao, transactionTemplate, authorizationFacade), RoleService.class);
        permissionService = proxyFactory.createProxy(
                new PermissionServiceImpl(permissionDao, resourceDao, authorizationFacade), PermissionService.class);
        resourceService = proxyFactory.createProxy(
                new ResourceServiceImpl(resourceDao, permissionDao, authorizationFacade), ResourceService.class);
        auditService = proxyFactory.createProxy(
                new AuditServiceImpl(auditLogDao), AuditService.class);
        authService = proxyFactory.createProxy(
                new AuthServiceImpl(userDao, passwordEncoder, authorizationFacade, sessionManager), AuthService.class);

        // 5) 注册审计监听器（观察者：审计事件 → 落库）
        eventBus.register(new AuditEventListener(auditService));

        // 6) 数据初始化器
        dataInitializer = new DataInitializer(jdbcTemplate, permissionDao, roleDao, userDao,
                resourceDao, passwordEncoder);

        initialized = true;
        log.info("服务工厂初始化完成（服务已启用动态代理：鉴权 + 审计）");
    }

    /** 关闭资源（程序退出前调用） */
    public void shutdown() {
        DataSourceManager.getInstance().close();
    }

    // ---------------- getter ----------------

    public boolean isInitialized() {
        return initialized;
    }

    public UserService userService() {
        return userService;
    }

    public RoleService roleService() {
        return roleService;
    }

    public PermissionService permissionService() {
        return permissionService;
    }

    public ResourceService resourceService() {
        return resourceService;
    }

    public AuditService auditService() {
        return auditService;
    }

    public AuthService authService() {
        return authService;
    }

    public AuthorizationFacade authorizationFacade() {
        return authorizationFacade;
    }

    public SessionManager sessionManager() {
        return sessionManager;
    }

    public EventBus eventBus() {
        return eventBus;
    }

    public JdbcTemplate jdbcTemplate() {
        return jdbcTemplate;
    }

    public UserDao userDao() {
        return userDao;
    }

    public RoleDao roleDao() {
        return roleDao;
    }

    public PermissionDao permissionDao() {
        return permissionDao;
    }

    public ResourceDao resourceDao() {
        return resourceDao;
    }

    public PasswordEncoder passwordEncoder() {
        return passwordEncoder;
    }

    public PermissionCache permissionCache() {
        return permissionCache;
    }

    public ConfigManager config() {
        return ConfigManager.getInstance();
    }

    public DataInitializer dataInitializer() {
        return dataInitializer;
    }
}

package com.cqu.rbac.proxy;

import com.cqu.rbac.cli.session.SessionManager;
import com.cqu.rbac.event.EventBus;
import com.cqu.rbac.security.PermissionChecker;

import java.lang.reflect.Proxy;

/**
 * 服务代理工厂：为 Service 实现生成 JDK 动态代理对象。
 * <p>设计模式：代理 Proxy（JDK 动态代理）+ 简单工厂——集中创建"被切面增强"的服务对象，
 * 对调用方屏蔽 Proxy.newProxyInstance 的细节。</p>
 */
public class ServiceProxyFactory {

    private final PermissionChecker permissionChecker;
    private final SessionManager sessionManager;
    private final EventBus eventBus;

    public ServiceProxyFactory(PermissionChecker permissionChecker, SessionManager sessionManager, EventBus eventBus) {
        this.permissionChecker = permissionChecker;
        this.sessionManager = sessionManager;
        this.eventBus = eventBus;
    }

    /**
     * 创建服务代理：每个目标对象对应一个独立的拦截器实例。
     *
     * @param target           目标实现对象
     * @param serviceInterface 服务接口（必须是接口）
     * @return 代理对象（调用方通过接口使用）
     */
    @SuppressWarnings("unchecked")
    public <T> T createProxy(T target, Class<T> serviceInterface) {
        if (!serviceInterface.isInterface()) {
            throw new IllegalArgumentException("serviceInterface 必须是接口：" + serviceInterface.getName());
        }
        return (T) Proxy.newProxyInstance(
                serviceInterface.getClassLoader(),
                new Class<?>[]{serviceInterface},
                new AuditAndAuthInvocationHandler(target, permissionChecker, sessionManager, eventBus));
    }
}

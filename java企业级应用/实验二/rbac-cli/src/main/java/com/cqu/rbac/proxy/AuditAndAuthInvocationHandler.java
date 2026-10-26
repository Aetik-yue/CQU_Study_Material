package com.cqu.rbac.proxy;

import com.cqu.rbac.cli.session.SessionManager;
import com.cqu.rbac.common.annotation.AuditLog;
import com.cqu.rbac.common.annotation.RequiresPermission;
import com.cqu.rbac.common.constants.Constants;
import com.cqu.rbac.common.enums.OperationResult;
import com.cqu.rbac.common.exception.AuthException;
import com.cqu.rbac.domain.dto.ChangePasswordDTO;
import com.cqu.rbac.domain.dto.LoginDTO;
import com.cqu.rbac.domain.dto.RoleDTO;
import com.cqu.rbac.domain.dto.UserDTO;
import com.cqu.rbac.domain.entity.Permission;
import com.cqu.rbac.domain.entity.Resource;
import com.cqu.rbac.event.AuditEvent;
import com.cqu.rbac.event.EventBus;
import com.cqu.rbac.security.PermissionChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 鉴权 + 审计统一拦截器（InvocationHandler）。
 * <p>设计模式：代理 Proxy + 观察者 Observer 的桥梁——方法执行前做权限校验，
 * 执行后（成功或异常）构造审计事件投递到 {@link EventBus}。</p>
 *
 * <p>拦截逻辑：</p>
 * <ol>
 *   <li>读取目标实现方法上的 {@code @RequiresPermission}，有则强制校验，无权限抛 AuthException（并记"鉴权失败"审计）</li>
 *   <li>执行目标方法，统计耗时</li>
 *   <li>方法上存在 {@code @AuditLog} 时，构造审计日志（含操作人、模块、操作、对象、结果、耗时、失败原因）并发布事件</li>
 *   <li>审计参数自动脱敏：参数名含 password/pwd/salt 的值一律记为 ***</li>
 * </ol>
 */
public class AuditAndAuthInvocationHandler implements InvocationHandler {

    private static final Logger log = LoggerFactory.getLogger(AuditAndAuthInvocationHandler.class);

    /** 敏感参数名关键字（脱敏用） */
    private static final Set<String> SENSITIVE_KEYWORDS = new HashSet<>(Arrays.asList(
            "password", "oldpassword", "newpassword", "pwd", "salt"));

    /** 可作为"操作对象"的参数名（不区分大小写） */
    private static final Set<String> TARGET_PARAM_NAMES = new HashSet<>(Arrays.asList(
            "id", "userid", "roleid", "permissionid", "resourceid", "targetid",
            "username", "rolecode", "permissioncode", "resourcecode", "code"));

    /** 目标实现对象（每次 createProxy 由工厂为其创建新的处理器实例） */
    private final Object target;
    private final PermissionChecker permissionChecker;
    private final SessionManager sessionManager;
    private final EventBus eventBus;

    public AuditAndAuthInvocationHandler(Object target, PermissionChecker permissionChecker,
                                         SessionManager sessionManager, EventBus eventBus) {
        this.target = target;
        this.permissionChecker = permissionChecker;
        this.sessionManager = sessionManager;
        this.eventBus = eventBus;
    }

    /** 核心拦截：鉴权 → 执行 → 审计 */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Object 基础方法（toString/hashCode/equals）直接放行
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(target, args);
        }

        // 定位实现类方法（注解标注在实现类上）
        Method implMethod = resolveImplMethod(method);

        // 操作人快照：必须在方法执行前采集（否则 logout 等清会话的方法会丢失操作人）
        Long actorId = sessionManager.currentUserId();
        String actorName = sessionManager.currentUsername();

        // 1) 权限校验
        RequiresPermission requires = implMethod.getAnnotation(RequiresPermission.class);
        if (requires == null) {
            requires = method.getAnnotation(RequiresPermission.class); // 接口注解兜底
        }
        if (requires != null) {
            try {
                permissionChecker.check(requires.logical(), requires.value());
            } catch (AuthException e) {
                // 越权调用：记录"鉴权失败"审计后原样抛出
                publishAudit(implMethod, method, args, actorId, actorName, e, 0L, true);
                throw e;
            }
        }

        // 2) 执行目标方法 + 计时 + 审计
        long start = System.currentTimeMillis();
        try {
            Object result = method.invoke(target, args);
            publishAudit(implMethod, method, args, actorId, actorName, null,
                    System.currentTimeMillis() - start, false);
            return result;
        } catch (InvocationTargetException ite) {
            Throwable cause = ite.getCause() == null ? ite : ite.getCause();
            publishAudit(implMethod, method, args, actorId, actorName, cause,
                    System.currentTimeMillis() - start, false);
            throw cause;
        }
    }

    /** 解析实现类上的同名方法（含注解），失败时退回接口方法 */
    private Method resolveImplMethod(Method method) {
        try {
            return target.getClass().getMethod(method.getName(), method.getParameterTypes());
        } catch (NoSuchMethodException e) {
            return method;
        }
    }

    /**
     * 构造并发布审计事件。
     *
     * @param denied 是否因鉴权失败而记录（未标注 @AuditLog 的方法在鉴权失败时也要记录）
     */
    private void publishAudit(Method implMethod, Method ifaceMethod, Object[] args,
                              Long actorId, String actorName, Throwable error, long cost, boolean denied) {
        AuditLog annotation = implMethod.getAnnotation(AuditLog.class);
        if (annotation == null) {
            annotation = ifaceMethod.getAnnotation(AuditLog.class);
        }
        if (annotation == null && !denied) {
            return; // 未声明审计且非鉴权失败 → 不记录
        }

        String module = annotation != null ? annotation.module() : Constants.MODULE_AUTHZ;
        String operation = annotation != null ? annotation.operation() : "DENIED";

        // 提取操作对象与详情（自动脱敏）
        String target = describeTarget(implMethod, args);
        String detail = describeDetail(implMethod, args);

        com.cqu.rbac.domain.entity.AuditLog auditLog = com.cqu.rbac.domain.entity.AuditLog.builder()
                .userId(actorId != null ? actorId : extractUserId(args))
                .username(actorName != null ? actorName : extractUsername(args))
                .module(module)
                .operation(operation)
                .targetObject(truncate(target, Constants.AUDIT_TARGET_MAX_LEN))
                .detail(truncate(detail, 500))
                .result(error == null ? OperationResult.SUCCESS.name() : OperationResult.FAILURE.name())
                .durationMs(cost)
                .errorMessage(error == null ? null : truncate(error.getMessage(), Constants.AUDIT_ERROR_MAX_LEN))
                .client(Constants.CLIENT_CLI)
                .createTime(LocalDateTime.now())
                .build();

        eventBus.post(new AuditEvent(auditLog));
        if (error != null) {
            log.debug("审计事件已发布（失败）：{} {}.{} - {}", actorName, module, operation, error.getMessage());
        } else {
            log.debug("审计事件已发布：{} {}.{} 耗时 {}ms", actorName, module, operation, cost);
        }
    }

    // ---------------- 参数描述（含脱敏） ----------------

    /** 提取"操作对象"：优先取名为 id/userId/username/code 等的参数 */
    private String describeTarget(Method method, Object[] args) {
        if (args == null || args.length == 0) {
            return method.getName() + "()";
        }
        Parameter[] params = method.getParameters();
        for (int i = 0; i < args.length; i++) {
            String name = paramName(params, i);
            if (args[i] != null && TARGET_PARAM_NAMES.contains(name.toLowerCase())
                    && !isSensitive(name)) {
                return describeValue(name, args[i]);
            }
        }
        // 兜底：第一个非空且非敏感参数的简要描述
        for (int i = 0; i < args.length; i++) {
            if (args[i] != null && !isSensitive(paramName(params, i))) {
                return describeValue(paramName(params, i), args[i]);
            }
        }
        return method.getName() + "()";
    }

    /** 提取"操作详情"：全部参数（敏感参数脱敏） */
    private String describeDetail(Method method, Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        Parameter[] params = method.getParameters();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            String name = paramName(params, i);
            if (isSensitive(name)) {
                sb.append(name).append("=***"); // 密码等敏感信息绝不记录
            } else {
                sb.append(describeValue(name, args[i]));
            }
        }
        return sb.toString();
    }

    /** 取参数名（编译需开启 -parameters；否则退化为 argN） */
    private String paramName(Parameter[] params, int index) {
        return index < params.length && params[index].isNamePresent()
                ? params[index].getName() : "arg" + index;
    }

    /** 是否为敏感参数名 */
    private boolean isSensitive(String name) {
        String lower = name.toLowerCase();
        return SENSITIVE_KEYWORDS.stream().anyMatch(lower::contains);
    }

    /**
     * 值描述：复杂对象（DTO/实体/集合）直接给出语义键（如 username=admin、roleCode=ADMIN），
     * 简单值输出 参数名=值。用于审计的对象/详情字段。
     */
    private String describeValue(String paramName, Object value) {
        if (value == null) {
            return paramName + "=null";
        }
        if (value instanceof LoginDTO) {
            return "username=" + ((LoginDTO) value).getUsername();
        }
        if (value instanceof UserDTO) {
            UserDTO dto = (UserDTO) value;
            return dto.getId() != null ? ("id=" + dto.getId()) : ("username=" + dto.getUsername());
        }
        if (value instanceof RoleDTO) {
            RoleDTO dto = (RoleDTO) value;
            return dto.getId() != null ? ("id=" + dto.getId()) : ("roleCode=" + dto.getRoleCode());
        }
        if (value instanceof ChangePasswordDTO) {
            return "userId=" + ((ChangePasswordDTO) value).getUserId(); // 密码字段已省略
        }
        if (value instanceof Permission) {
            return "permissionCode=" + ((Permission) value).getPermissionCode();
        }
        if (value instanceof Resource) {
            return "resourceCode=" + ((Resource) value).getResourceCode();
        }
        if (value instanceof Collection<?>) {
            return paramName + "=[共 " + ((Collection<?>) value).size() + " 项]";
        }
        return paramName + "=" + truncate(String.valueOf(value), 60);
    }

    /** 审计失败：从参数中提取用户 id（login 等未登录场景兜底） */
    private Long extractUserId(Object[] args) {
        if (args == null) {
            return null;
        }
        for (Object arg : args) {
            if (arg instanceof UserDTO && ((UserDTO) arg).getId() != null) {
                return ((UserDTO) arg).getId();
            }
            if (arg instanceof ChangePasswordDTO) {
                return ((ChangePasswordDTO) arg).getUserId();
            }
        }
        return null;
    }

    /** 审计操作人：从参数中提取用户名（登录审计兜底） */
    private String extractUsername(Object[] args) {
        if (args == null) {
            return null;
        }
        for (Object arg : args) {
            if (arg instanceof LoginDTO) {
                return ((LoginDTO) arg).getUsername();
            }
            if (arg instanceof UserDTO) {
                return ((UserDTO) arg).getUsername();
            }
        }
        return null;
    }

    private static String truncate(String text, int max) {
        if (text == null) {
            return null;
        }
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }
}

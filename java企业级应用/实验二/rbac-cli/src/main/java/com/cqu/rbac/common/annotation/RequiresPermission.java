package com.cqu.rbac.common.annotation;

import com.cqu.rbac.common.enums.Logical;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 方法级权限注解（自定义注解 + 运行时反射读取）。
 * <p>标注在 Service 实现类的方法上，由动态代理（AuditAndAuthInvocationHandler）
 * 在方法执行前读取并强制校验当前登录用户是否拥有所需权限，无权限抛出 {@code AuthException}。</p>
 *
 * <p>示例：</p>
 * <pre>
 *   &#64;RequiresPermission("user:create")                       // 需要 user:create
 *   &#64;RequiresPermission(value = {"a", "b"}, logical = OR)     // a 或 b 任一即可
 * </pre>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequiresPermission {

    /** 所需权限编码（一个或多个） */
    String[] value();

    /** 多权限组合逻辑，默认 AND（全部满足） */
    Logical logical() default Logical.AND;
}

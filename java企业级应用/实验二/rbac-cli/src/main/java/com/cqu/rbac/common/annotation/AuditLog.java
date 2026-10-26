package com.cqu.rbac.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 方法级审计注解（自定义注解 + 运行时反射读取）。
 * <p>标注在 Service 实现类的方法上，动态代理会在方法执行前后采集
 * 操作人、模块、操作、对象、结果、耗时等信息并发布审计事件（AOP 式审计，业务零侵入）。</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AuditLog {

    /** 业务模块名，如"用户管理" */
    String module();

    /** 操作类型，如 LOGIN / CREATE / UPDATE / DELETE / ASSIGN_ROLE / TOGGLE_STATUS */
    String operation();
}

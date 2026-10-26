package com.cqu.rbac.event;

/**
 * 事件监听器接口（观察者模式 + 函数式接口）。
 * <p>Lambda 落地点：{@code bus.register(event -> {...})} 可直接以 Lambda 注册监听。</p>
 */
@FunctionalInterface
public interface EventListener {

    /** 接收事件（实现方需自行判断事件类型） */
    void onEvent(Object event);
}

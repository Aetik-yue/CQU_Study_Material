package com.cqu.rbac.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 事件总线（单例）。
 * <p>设计模式：单例 Singleton + 观察者 Observer——发布者 {@code post} 事件，
 * 全部注册的监听器依次收到通知；监听器异常被隔离，不影响发布方主流程。</p>
 */
public final class EventBus {

    private static final Logger log = LoggerFactory.getLogger(EventBus.class);

    /** 单例实例（饿汉式） */
    private static final EventBus INSTANCE = new EventBus();

    /** 监听器列表：CopyOnWriteArrayList 保证并发注册/遍历安全 */
    private final List<EventListener> listeners = new CopyOnWriteArrayList<>();

    private EventBus() {
    }

    public static EventBus getInstance() {
        return INSTANCE;
    }

    /** 注册监听器 */
    public void register(EventListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    /**
     * 发布事件：依次通知所有监听器。
     * <p>单个监听器抛异常仅记录日志，不中断其他监听器，也不影响调用方。</p>
     */
    public void post(Object event) {
        if (event == null) {
            return;
        }
        for (EventListener listener : listeners) {
            try {
                listener.onEvent(event);
            } catch (Exception e) {
                log.error("事件监听器处理失败（event={}）: {}", event.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
    }

    /** 当前监听器数量（诊断用） */
    public int listenerCount() {
        return listeners.size();
    }
}

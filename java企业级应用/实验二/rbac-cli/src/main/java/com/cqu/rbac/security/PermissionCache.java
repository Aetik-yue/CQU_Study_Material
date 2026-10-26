package com.cqu.rbac.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 权限缓存（特色功能）：以 userId 为 key 缓存"有效权限码集合"，
 * 角色/权限关系变更后主动失效，避免每次鉴权都走数据库，也避免越权。
 * <p>集合类型落地点：{@link ConcurrentHashMap} 线程安全缓存。</p>
 */
public class PermissionCache {

    private static final Logger log = LoggerFactory.getLogger(PermissionCache.class);

    /** userId → 权限码集合 */
    private final Map<Long, Set<String>> cache = new ConcurrentHashMap<>();

    /** 读取缓存（未命中返回 null） */
    public Set<String> get(Long userId) {
        return userId == null ? null : cache.get(userId);
    }

    /** 写入缓存 */
    public void put(Long userId, Set<String> codes) {
        if (userId != null && codes != null) {
            cache.put(userId, codes);
        }
    }

    /** 失效单个用户缓存 */
    public void evict(Long userId) {
        if (userId != null && cache.remove(userId) != null) {
            log.debug("已失效用户 {} 的权限缓存", userId);
        }
    }

    /** 批量失效多个用户缓存 */
    public void evict(Collection<Long> userIds) {
        if (userIds == null) {
            return;
        }
        for (Long userId : userIds) {
            cache.remove(userId);
        }
        log.debug("已批量失效 {} 个用户的权限缓存", userIds.size());
    }

    /** 清空全部缓存（角色-权限关系变更等全局性变更时使用） */
    public void evictAll() {
        int size = cache.size();
        cache.clear();
        if (size > 0) {
            log.debug("已清空全部权限缓存（{} 条）", size);
        }
    }

    /** 当前缓存条目数（诊断用） */
    public int size() {
        return cache.size();
    }
}

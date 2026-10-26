package com.cqu.rbac.support;

import com.cqu.rbac.common.enums.PermissionType;
import com.cqu.rbac.dao.PermissionDao;
import com.cqu.rbac.dao.ResourceDao;
import com.cqu.rbac.dao.RoleDao;
import com.cqu.rbac.dao.UserDao;
import com.cqu.rbac.domain.entity.Permission;
import com.cqu.rbac.domain.entity.Resource;
import com.cqu.rbac.domain.vo.MenuNode;
import com.cqu.rbac.security.PermissionCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * 鉴权外观 Facade：统一封装"权限聚合、权限判定、菜单树构建、资源访问校验"，
 * 对上层（代理层/CLI）提供简单的调用入口。
 * <p>设计模式：外观 Facade——屏蔽 RBAC 聚合逻辑的复杂性（角色继承链、缓存、树构建）。</p>
 *
 * <p>核心算法（权限聚合）：用户直接角色 → 沿 parent_id 递归继承祖先角色 →
 * 汇总各角色启用权限的编码集合 → 写入缓存。</p>
 */
public class AuthorizationFacade {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationFacade.class);

    private final UserDao userDao;
    private final RoleDao roleDao;
    private final PermissionDao permissionDao;
    private final ResourceDao resourceDao;
    private final PermissionCache permissionCache;

    public AuthorizationFacade(UserDao userDao, RoleDao roleDao, PermissionDao permissionDao,
                               ResourceDao resourceDao, PermissionCache permissionCache) {
        this.userDao = userDao;
        this.roleDao = roleDao;
        this.permissionDao = permissionDao;
        this.resourceDao = resourceDao;
        this.permissionCache = permissionCache;
    }

    // ---------------- 权限聚合 ----------------

    /**
     * 加载用户的有效权限码集合（含角色继承），带缓存。
     */
    public Set<String> loadPermissions(Long userId) {
        Set<String> cached = permissionCache.get(userId);
        if (cached != null) {
            return cached;
        }

        // 1) 用户直接绑定的角色
        List<Long> directRoleIds = userDao.findRoleIds(userId);

        Set<String> codes = new TreeSet<>();
        if (!directRoleIds.isEmpty()) {
            // 2) 角色继承链：自身 + 全部祖先（去重）
            Set<Long> chainRoleIds = new LinkedHashSet<>(directRoleIds);
            for (Long roleId : directRoleIds) {
                chainRoleIds.addAll(roleDao.findRoleChainIds(roleId));
            }
            // 3) 汇总启用权限的编码（TreeSet 去重且有序）
            List<Permission> permissions = permissionDao.findByRoleIds(chainRoleIds);
            codes = permissions.stream()
                    .map(Permission::getPermissionCode)
                    .collect(Collectors.toCollection(TreeSet::new));
        }

        // 4) 写入缓存
        permissionCache.put(userId, codes);
        log.debug("权限聚合完成：userId={}, 角色链={} 条, 权限={} 个", userId, directRoleIds.size(), codes.size());
        return codes;
    }

    // ---------------- 权限判定（策略：单个 / 任意 / 全部 / 资源） ----------------

    /** 是否拥有指定权限码 */
    public boolean hasPermission(Long userId, String code) {
        return code != null && loadPermissions(userId).contains(code);
    }

    /** 是否拥有任意一个权限码 */
    public boolean hasAny(Long userId, Collection<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return true;
        }
        Set<String> mine = loadPermissions(userId);
        return codes.stream().anyMatch(mine::contains);
    }

    /** 是否拥有全部权限码 */
    public boolean hasAll(Long userId, Collection<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return true;
        }
        Set<String> mine = loadPermissions(userId);
        return mine.containsAll(codes);
    }

    /**
     * 能否访问某资源：本质检查用户是否拥有"关联该资源的 RESOURCE 权限"。
     */
    public boolean canAccessResource(Long userId, String resourceCode) {
        if (resourceCode == null || resourceCode.trim().isEmpty()) {
            return false;
        }
        Resource resource = resourceDao.findByCode(resourceCode.trim());
        if (resource == null || !resource.isEnabled()) {
            return false;
        }
        List<Permission> linked = permissionDao.findByResourceId(resource.getId());
        if (linked.isEmpty()) {
            return false; // 没有任何权限指向该资源 → 无人可访问
        }
        Set<String> mine = loadPermissions(userId);
        return linked.stream().anyMatch(p -> mine.contains(p.getPermissionCode()));
    }

    // ---------------- 动态菜单构建 ----------------

    /**
     * 构建用户可访问的菜单树：只保留用户拥有的 MENU 权限，并自动补全其祖先链。
     */
    public List<MenuNode> buildMenus(Long userId) {
        List<Permission> allMenus = permissionDao.findByType(PermissionType.MENU.name());
        if (allMenus.isEmpty()) {
            return new ArrayList<>();
        }
        Set<String> mine = loadPermissions(userId);

        Map<Long, Permission> byId = new HashMap<>();
        for (Permission p : allMenus) {
            byId.put(p.getId(), p);
        }

        // 1) 收集：用户拥有的菜单 + 其全部祖先（保证父容器存在）
        Set<Long> included = new LinkedHashSet<>();
        for (Permission p : allMenus) {
            if (mine.contains(p.getPermissionCode())) {
                collectWithAncestors(p, byId, included);
            }
        }

        // 2) 先创建全部节点（保持 sort_order 顺序）
        Map<Long, MenuNode> nodeById = new LinkedHashMap<>();
        for (Long id : included) {
            Permission p = byId.get(id);
            nodeById.put(id, new MenuNode(p.getPermissionCode(), p.getPermissionName(), p.getPath()));
        }

        // 3) 再挂接父子关系
        List<MenuNode> roots = new ArrayList<>();
        for (Long id : included) {
            Permission p = byId.get(id);
            MenuNode node = nodeById.get(id);
            Long parentId = p.getParentId();
            MenuNode parent = parentId == null ? null : nodeById.get(parentId);
            if (parent == null) {
                roots.add(node);
            } else {
                parent.addChild(node);
            }
        }
        return roots;
    }

    /** 向上追溯，把自己与全部存在的祖先加入集合 */
    private void collectWithAncestors(Permission p, Map<Long, Permission> byId, Set<Long> included) {
        Permission current = p;
        int guard = 0;
        while (current != null && guard++ < 32) {
            included.add(current.getId());
            Long parentId = current.getParentId();
            current = (parentId == null || parentId == 0) ? null : byId.get(parentId);
        }
    }

    // ---------------- 缓存失效 ----------------

    /** 失效指定用户的权限缓存 */
    public void evict(Long userId) {
        permissionCache.evict(userId);
    }

    /** 批量失效用户权限缓存 */
    public void evict(Collection<Long> userIds) {
        permissionCache.evict(userIds);
    }

    /** 清空全部权限缓存（角色-权限关系等全局变更时使用） */
    public void evictAll() {
        permissionCache.evictAll();
    }
}

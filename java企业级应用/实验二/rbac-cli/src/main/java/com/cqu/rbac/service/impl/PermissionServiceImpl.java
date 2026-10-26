package com.cqu.rbac.service.impl;

import com.cqu.rbac.common.annotation.AuditLog;
import com.cqu.rbac.common.annotation.RequiresPermission;
import com.cqu.rbac.common.constants.Constants;
import com.cqu.rbac.common.enums.PermissionType;
import com.cqu.rbac.common.exception.BusinessException;
import com.cqu.rbac.common.util.ValidateUtil;
import com.cqu.rbac.dao.PermissionDao;
import com.cqu.rbac.dao.ResourceDao;
import com.cqu.rbac.domain.entity.Permission;
import com.cqu.rbac.domain.vo.PermissionNode;
import com.cqu.rbac.service.PermissionService;
import com.cqu.rbac.support.AuthorizationFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 权限管理服务实现。
 * <p>含权限树构建、删除保护（子权限/被角色引用）、父级存在性与防环校验。</p>
 */
public class PermissionServiceImpl implements PermissionService {

    private static final Logger log = LoggerFactory.getLogger(PermissionServiceImpl.class);

    /** 权限编码：小写字母/数字/冒号/短横线/下划线，如 user:create */
    private static final Pattern CODE_PATTERN = Pattern.compile("^[a-z][a-z0-9:_-]{1,99}$");

    private final PermissionDao permissionDao;
    private final ResourceDao resourceDao;
    private final AuthorizationFacade authorizationFacade;

    public PermissionServiceImpl(PermissionDao permissionDao, ResourceDao resourceDao,
                                 AuthorizationFacade authorizationFacade) {
        this.permissionDao = permissionDao;
        this.resourceDao = resourceDao;
        this.authorizationFacade = authorizationFacade;
    }

    // ---------------- 查询 ----------------

    @Override
    @RequiresPermission("permission:list")
    public List<Permission> listAll() {
        return permissionDao.findAll();
    }

    @Override
    @RequiresPermission("permission:list")
    public List<Permission> listByType(String type) {
        if (ValidateUtil.isBlank(type)) {
            return permissionDao.findAll();
        }
        return permissionDao.findByType(PermissionType.of(type).name());
    }

    @Override
    @RequiresPermission("permission:list")
    public Permission get(Long id) {
        return requirePermission(id);
    }

    @Override
    @RequiresPermission("permission:list")
    public List<PermissionNode> tree(String type) {
        List<Permission> all = ValidateUtil.isBlank(type) ? permissionDao.findAll() : listByType(type);
        if (all.isEmpty()) {
            return new ArrayList<>();
        }
        // 排序：先按 sortOrder 再按 id，保证父节点先于子节点处理
        all.sort((a, b) -> {
            int c = Integer.compare(nullSafe(a.getSortOrder()), nullSafe(b.getSortOrder()));
            return c != 0 ? c : Long.compare(nullSafe(a.getId()), nullSafe(b.getId()));
        });

        Map<Long, PermissionNode> nodeById = new HashMap<>();
        for (Permission p : all) {
            nodeById.put(p.getId(), new PermissionNode(p));
        }

        List<PermissionNode> roots = new ArrayList<>();
        for (Permission p : all) {
            PermissionNode node = nodeById.get(p.getId());
            Long parentId = p.getParentId();
            PermissionNode parent = (parentId == null || parentId == Constants.ROOT_PARENT_ID)
                    ? null : nodeById.get(parentId);
            if (parent == null) {
                node.setLevel(0);
                roots.add(node);
            } else {
                node.setLevel(parent.getLevel() + 1);
                parent.addChild(node);
            }
        }
        return roots;
    }

    // ---------------- 新增 / 修改 / 删除 ----------------

    @Override
    @RequiresPermission("permission:create")
    @AuditLog(module = Constants.MODULE_PERMISSION, operation = "CREATE")
    public void create(Permission permission) {
        validateFields(permission);
        if (permissionDao.findByCode(permission.getPermissionCode().trim()) != null) {
            throw new BusinessException("E5001", "权限编码 [" + permission.getPermissionCode() + "] 已存在");
        }
        validateParent(permission.getParentId(), null);
        validateResource(permission.getResourceId());
        normalize(permission);
        permissionDao.insert(permission);
        authorizationFacade.evictAll();
        log.info("创建权限 [{}]（{}）成功", permission.getPermissionName(), permission.getPermissionCode());
    }

    @Override
    @RequiresPermission("permission:update")
    @AuditLog(module = Constants.MODULE_PERMISSION, operation = "UPDATE")
    public void update(Permission permission) {
        if (permission.getId() == null) {
            throw new BusinessException("E5002", "权限 id 不能为空");
        }
        Permission existing = requirePermission(permission.getId());
        ValidateUtil.requireNonBlank(permission.getPermissionName(), "权限名称", 2, 80);
        // 权限编码创建后不可修改（代码中以编码做鉴权引用）
        permission.setPermissionCode(existing.getPermissionCode());
        validateParent(permission.getParentId(), existing.getId());
        validateResource(permission.getResourceId());
        normalize(permission);
        permission.setUpdateTime(null);
        permissionDao.update(permission);
        authorizationFacade.evictAll();
        log.info("修改权限 [{}]（id={}）成功", existing.getPermissionCode(), existing.getId());
    }

    @Override
    @RequiresPermission("permission:delete")
    @AuditLog(module = Constants.MODULE_PERMISSION, operation = "DELETE")
    public void delete(Long id) {
        Permission permission = requirePermission(id);
        long childCount = permissionDao.countChildren(id);
        if (childCount > 0) {
            throw new BusinessException("E5003", "权限 [" + permission.getPermissionCode() + "] 存在 "
                    + childCount + " 个子权限，禁止删除");
        }
        long roleRefs = permissionDao.countRoleReferences(id);
        if (roleRefs > 0) {
            throw new BusinessException("E5004", "权限 [" + permission.getPermissionCode() + "] 仍被 "
                    + roleRefs + " 个角色引用，禁止删除（请先解除引用）");
        }
        permissionDao.deleteById(id);
        authorizationFacade.evictAll();
        log.info("删除权限 [{}]（id={}）成功", permission.getPermissionCode(), id);
    }

    // ---------------- 私有辅助 ----------------

    private Permission requirePermission(Long id) {
        if (id == null) {
            throw new BusinessException("E5005", "权限 id 不能为空");
        }
        Permission permission = permissionDao.findById(id);
        if (permission == null) {
            throw new BusinessException("E5006", "权限不存在：id=" + id);
        }
        return permission;
    }

    /** 字段校验：名称/编码/类型 */
    private void validateFields(Permission permission) {
        ValidateUtil.requireNonBlank(permission.getPermissionName(), "权限名称", 2, 80);
        ValidateUtil.requireNonBlank(permission.getPermissionCode(), "权限编码", 2, 100);
        ValidateUtil.requireMatch(permission.getPermissionCode(), "权限编码", CODE_PATTERN,
                "小写字母开头，可含小写字母/数字/冒号/短横线/下划线，如 user:create");
        PermissionType.of(permission.getPermissionType()); // 非法类型抛业务异常
        ValidateUtil.optionalMaxLength(permission.getPath(), "路径", 200);
    }

    /** 归一化字段默认值 */
    private void normalize(Permission permission) {
        permission.setPermissionName(permission.getPermissionName().trim());
        permission.setPermissionCode(permission.getPermissionCode().trim());
        permission.setPermissionType(PermissionType.of(permission.getPermissionType()).name());
        permission.setParentId(permission.getParentId() == null ? Constants.ROOT_PARENT_ID : permission.getParentId());
        permission.setPath(ValidateUtil.trimToNull(permission.getPath()));
        permission.setSortOrder(permission.getSortOrder() == null ? 0 : permission.getSortOrder());
        permission.setStatus(permission.getStatus() == null ? Constants.STATUS_ENABLED : permission.getStatus());
    }

    /** 父权限校验：存在 + 防自环 */
    private void validateParent(Long parentId, Long selfId) {
        if (parentId == null || parentId == Constants.ROOT_PARENT_ID) {
            return;
        }
        if (selfId != null && parentId.equals(selfId)) {
            throw new BusinessException("E5007", "父权限不能是自己");
        }
        Permission parent = permissionDao.findById(parentId);
        if (parent == null) {
            throw new BusinessException("E5008", "父权限不存在：id=" + parentId);
        }
        if (selfId != null) {
            // 沿父链上溯，防止把权限挂到自己的子孙下形成环
            Permission current = parent;
            int guard = 0;
            while (current != null && guard++ < 32) {
                if (selfId.equals(current.getId())) {
                    throw new BusinessException("E5009", "不能把权限挂到自己的子权限下（会形成层级环）");
                }
                Long pid = current.getParentId();
                current = (pid == null || pid == Constants.ROOT_PARENT_ID) ? null : permissionDao.findById(pid);
            }
        }
    }

    /** 关联资源校验 */
    private void validateResource(Long resourceId) {
        if (resourceId != null && resourceDao.findById(resourceId) == null) {
            throw new BusinessException("E5010", "关联的资源不存在：id=" + resourceId);
        }
    }

    private static int nullSafe(Integer v) {
        return v == null ? 0 : v;
    }

    private static long nullSafe(Long v) {
        return v == null ? 0L : v;
    }
}

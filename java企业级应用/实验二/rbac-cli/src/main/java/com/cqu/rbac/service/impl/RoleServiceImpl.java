package com.cqu.rbac.service.impl;

import com.cqu.rbac.common.annotation.AuditLog;
import com.cqu.rbac.common.annotation.RequiresPermission;
import com.cqu.rbac.common.constants.Constants;
import com.cqu.rbac.common.enums.PermissionType;
import com.cqu.rbac.common.exception.BusinessException;
import com.cqu.rbac.common.result.PageQuery;
import com.cqu.rbac.common.result.PageResult;
import com.cqu.rbac.common.util.ValidateUtil;
import com.cqu.rbac.dao.PermissionDao;
import com.cqu.rbac.dao.RoleDao;
import com.cqu.rbac.domain.dto.RoleDTO;
import com.cqu.rbac.domain.entity.Permission;
import com.cqu.rbac.domain.entity.Role;
import com.cqu.rbac.jdbc.TransactionTemplate;
import com.cqu.rbac.service.RoleService;
import com.cqu.rbac.support.AuthorizationFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 角色管理服务实现。
 * <p>含角色继承校验（防自环/防环）、删除保护（内置角色/被用户绑定/有子角色）。</p>
 */
public class RoleServiceImpl implements RoleService {

    private static final Logger log = LoggerFactory.getLogger(RoleServiceImpl.class);

    /** 角色编码：大写字母开头，大写字母/数字/下划线，2-50 位 */
    private static final Pattern ROLE_CODE_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]{1,49}$");

    /** 删除保护的内置角色编码 */
    private static final List<String> BUILTIN_ROLE_CODES = Arrays.asList(
            Constants.ROLE_CODE_ADMIN, Constants.ROLE_CODE_USER_MANAGER,
            Constants.ROLE_CODE_AUDITOR, Constants.ROLE_CODE_USER);

    private final RoleDao roleDao;
    private final PermissionDao permissionDao;
    private final TransactionTemplate transactionTemplate;
    private final AuthorizationFacade authorizationFacade;

    public RoleServiceImpl(RoleDao roleDao, PermissionDao permissionDao,
                           TransactionTemplate transactionTemplate, AuthorizationFacade authorizationFacade) {
        this.roleDao = roleDao;
        this.permissionDao = permissionDao;
        this.transactionTemplate = transactionTemplate;
        this.authorizationFacade = authorizationFacade;
    }

    // ---------------- 查询 ----------------

    @Override
    @RequiresPermission("role:list")
    public List<Role> list() {
        return roleDao.findAll();
    }

    @Override
    @RequiresPermission("role:list")
    public PageResult<Role> page(int page, int size) {
        PageQuery query = new PageQuery(page, size);
        long total = roleDao.count();
        if (total == 0) {
            return PageResult.empty(query.getPage(), query.getSize());
        }
        // 数据量小，直接内存分页（演示分页机制的一致性）
        List<Role> all = roleDao.findAll();
        int from = Math.min(query.getOffset(), all.size());
        int to = Math.min(from + query.getSize(), all.size());
        return PageResult.of(all.subList(from, to), total, query.getPage(), query.getSize());
    }

    @Override
    @RequiresPermission("role:list")
    public Role get(Long id) {
        return requireRole(id);
    }

    // ---------------- 新增 / 修改 / 删除 ----------------

    @Override
    @RequiresPermission("role:create")
    @AuditLog(module = Constants.MODULE_ROLE, operation = "CREATE")
    public void create(RoleDTO dto) {
        ValidateUtil.requireNonBlank(dto.getRoleName(), "roleName", "角色名称", 2, 50);
        ValidateUtil.requireMatch(dto.getRoleCode(), "roleCode", "角色编码", ROLE_CODE_PATTERN,
                "大写字母开头，可含大写字母/数字/下划线，2-50 位");
        ValidateUtil.optionalMaxLength(dto.getDescription(), "description", "描述", 200);

        String code = dto.getRoleCode().trim();
        if (roleDao.findByCode(code) != null) {
            throw new BusinessException("E4001", "角色编码 [" + code + "] 已存在", "roleCode");
        }
        validateParent(dto.getParentId(), null);

        Role role = new Role(dto.getRoleName().trim(), code);
        role.setParentId(dto.getParentId() == null ? Constants.ROOT_PARENT_ID : dto.getParentId());
        role.setDescription(ValidateUtil.trimToNull(dto.getDescription()));
        role.setStatus(dto.getStatus() == null ? Constants.STATUS_ENABLED : dto.getStatus());
        roleDao.insert(role);
        log.info("创建角色 [{}]（{}）成功，父角色={}", role.getRoleName(), code, role.getParentId());
    }

    @Override
    @RequiresPermission("role:update")
    @AuditLog(module = Constants.MODULE_ROLE, operation = "UPDATE")
    public void update(RoleDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("E4002", "角色 id 不能为空");
        }
        Role role = requireRole(dto.getId());
        ValidateUtil.requireNonBlank(dto.getRoleName(), "roleName", "角色名称", 2, 50);
        ValidateUtil.optionalMaxLength(dto.getDescription(), "description", "描述", 200);
        validateParent(dto.getParentId(), role.getId());

        role.setRoleName(dto.getRoleName().trim());
        if (dto.getParentId() != null) {
            role.setParentId(dto.getParentId());
        }
        role.setDescription(ValidateUtil.trimToNull(dto.getDescription()));
        if (dto.getStatus() != null) {
            role.setStatus(dto.getStatus());
        }
        roleDao.update(role);
        // 继承关系可能变化 → 清空权限缓存，保证下次访问重新聚合
        authorizationFacade.evictAll();
        log.info("修改角色 [{}]（id={}）成功", role.getRoleCode(), role.getId());
    }

    @Override
    @RequiresPermission("role:delete")
    @AuditLog(module = Constants.MODULE_ROLE, operation = "DELETE")
    public void delete(Long id) {
        Role role = requireRole(id);

        // 删除保护：内置角色
        if (BUILTIN_ROLE_CODES.contains(role.getRoleCode())) {
            throw new BusinessException("E4003", "内置角色 [" + role.getRoleCode() + "] 禁止删除");
        }
        // 删除保护：仍有用户绑定
        long userCount = roleDao.countUsersByRole(id);
        if (userCount > 0) {
            throw new BusinessException("E4004", "角色 [" + role.getRoleName() + "] 仍被 " + userCount
                    + " 个用户绑定，禁止删除（请先解除绑定）");
        }
        // 删除保护：存在子角色（继承关系）
        long childCount = roleDao.countChildren(id);
        if (childCount > 0) {
            throw new BusinessException("E4005", "角色 [" + role.getRoleName() + "] 存在 " + childCount
                    + " 个子角色，禁止删除（请先调整继承关系）");
        }

        transactionTemplate.execute(() -> {
            roleDao.deleteRolePermissions(id);
            roleDao.deleteById(id);
            return null;
        });
        authorizationFacade.evictAll();
        log.info("删除角色 [{}]（id={}）成功", role.getRoleCode(), id);
    }

    // ---------------- 权限分配与查看 ----------------

    @Override
    @RequiresPermission("role:assign-permission")
    @AuditLog(module = Constants.MODULE_ROLE, operation = "ASSIGN_PERMISSION")
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        Role role = requireRole(roleId);
        if (permissionIds != null) {
            for (Long permissionId : permissionIds) {
                if (permissionDao.findById(permissionId) == null) {
                    throw new BusinessException("E4006", "权限不存在：id=" + permissionId);
                }
            }
        }
        transactionTemplate.execute(() -> {
            roleDao.deleteRolePermissions(roleId);
            roleDao.insertRolePermissions(roleId, permissionIds);
            return null;
        });
        // 角色-权限关系变更会影响该角色及其全部子角色的用户 → 保守起见清空缓存
        authorizationFacade.evictAll();
        log.info("给角色 [{}] 分配权限 {} 项成功", role.getRoleCode(),
                permissionIds == null ? 0 : permissionIds.size());
    }

    @Override
    @RequiresPermission("role:list")
    public List<Permission> listPermissions(Long roleId) {
        requireRole(roleId);
        List<Long> ids = roleDao.findPermissionIds(roleId);
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        return permissionDao.findByIds(ids);
    }

    // ---------------- 私有辅助 ----------------

    private Role requireRole(Long id) {
        if (id == null) {
            throw new BusinessException("E4007", "角色 id 不能为空");
        }
        Role role = roleDao.findById(id);
        if (role == null) {
            throw new BusinessException("E4008", "角色不存在：id=" + id);
        }
        return role;
    }

    /**
     * 校验父角色：必须存在；不能以自己为父；不能以子孙为父（防环）。
     */
    private void validateParent(Long parentId, Long selfId) {
        if (parentId == null || parentId == Constants.ROOT_PARENT_ID) {
            return;
        }
        if (selfId != null && parentId.equals(selfId)) {
            throw new BusinessException("E4009", "父角色不能是自己", "parentId");
        }
        if (roleDao.findById(parentId) == null) {
            throw new BusinessException("E4010", "父角色不存在：id=" + parentId, "parentId");
        }
        if (selfId != null) {
            // 沿待设父角色的继承链上溯，若遇到自己 → 形成环
            List<Long> chain = roleDao.findRoleChainIds(parentId);
            if (chain.contains(selfId)) {
                throw new BusinessException("E4011", "不能把角色挂到自己的子角色下（会形成继承环）", "parentId");
            }
        }
    }
}

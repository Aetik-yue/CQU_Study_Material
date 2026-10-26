package com.cqu.rbac.service.impl;

import com.cqu.rbac.common.annotation.AuditLog;
import com.cqu.rbac.common.annotation.RequiresPermission;
import com.cqu.rbac.common.constants.Constants;
import com.cqu.rbac.common.enums.ResourceType;
import com.cqu.rbac.common.exception.BusinessException;
import com.cqu.rbac.common.util.ValidateUtil;
import com.cqu.rbac.dao.PermissionDao;
import com.cqu.rbac.dao.ResourceDao;
import com.cqu.rbac.domain.entity.Resource;
import com.cqu.rbac.service.ResourceService;
import com.cqu.rbac.support.AuthorizationFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 资源管理服务实现。
 * <p>含资源访问校验（本质：用户是否拥有"关联该资源的 RESOURCE 权限"）。</p>
 */
public class ResourceServiceImpl implements ResourceService {

    private static final Logger log = LoggerFactory.getLogger(ResourceServiceImpl.class);

    /** 资源编码：字母数字/冒号/短横线/下划线/点，如 report:finance */
    private static final Pattern CODE_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9:_\\-.]{1,99}$");

    private final ResourceDao resourceDao;
    private final PermissionDao permissionDao;
    private final AuthorizationFacade authorizationFacade;

    public ResourceServiceImpl(ResourceDao resourceDao, PermissionDao permissionDao,
                               AuthorizationFacade authorizationFacade) {
        this.resourceDao = resourceDao;
        this.permissionDao = permissionDao;
        this.authorizationFacade = authorizationFacade;
    }

    // ---------------- 查询 ----------------

    @Override
    @RequiresPermission("resource:list")
    public List<Resource> listAll() {
        return resourceDao.findAll();
    }

    @Override
    @RequiresPermission("resource:list")
    public Resource get(Long id) {
        return requireResource(id);
    }

    // ---------------- 新增 / 修改 / 删除 ----------------

    @Override
    @RequiresPermission("resource:create")
    @AuditLog(module = Constants.MODULE_RESOURCE, operation = "CREATE")
    public void create(Resource resource) {
        validateFields(resource);
        String code = resource.getResourceCode().trim();
        if (resourceDao.findByCode(code) != null) {
            throw new BusinessException("E6001", "资源编码 [" + code + "] 已存在");
        }
        normalize(resource);
        resourceDao.insert(resource);
        authorizationFacade.evictAll();
        log.info("创建资源 [{}]（{}）成功", resource.getResourceName(), code);
    }

    @Override
    @RequiresPermission("resource:update")
    @AuditLog(module = Constants.MODULE_RESOURCE, operation = "UPDATE")
    public void update(Resource resource) {
        if (resource.getId() == null) {
            throw new BusinessException("E6002", "资源 id 不能为空");
        }
        Resource existing = requireResource(resource.getId());
        ValidateUtil.requireNonBlank(resource.getResourceName(), "资源名称", 2, 80);
        // 资源编码创建后不可修改（权限通过 resource_id 关联）
        resource.setResourceCode(existing.getResourceCode());
        normalize(resource);
        resourceDao.update(resource);
        log.info("修改资源 [{}]（id={}）成功", existing.getResourceCode(), existing.getId());
    }

    @Override
    @RequiresPermission("resource:delete")
    @AuditLog(module = Constants.MODULE_RESOURCE, operation = "DELETE")
    public void delete(Long id) {
        Resource resource = requireResource(id);
        long refs = permissionDao.countByResourceId(id);
        if (refs > 0) {
            throw new BusinessException("E6003", "资源 [" + resource.getResourceCode() + "] 被 " + refs
                    + " 个权限关联，禁止删除（请先解除关联）");
        }
        resourceDao.deleteById(id);
        log.info("删除资源 [{}]（id={}）成功", resource.getResourceCode(), id);
    }

    // ---------------- 资源访问校验 ----------------

    @Override
    @RequiresPermission("resource:check")
    @AuditLog(module = Constants.MODULE_RESOURCE, operation = "CHECK_ACCESS")
    public boolean checkAccess(Long userId, String resourceCode) {
        if (userId == null) {
            throw new BusinessException("E6004", "用户 id 不能为空");
        }
        boolean accessible = authorizationFacade.canAccessResource(userId, resourceCode);
        log.info("资源访问校验：userId={}, resource={}, 结果={}", userId, resourceCode, accessible ? "允许" : "拒绝");
        return accessible;
    }

    // ---------------- 私有辅助 ----------------

    private Resource requireResource(Long id) {
        if (id == null) {
            throw new BusinessException("E6005", "资源 id 不能为空");
        }
        Resource resource = resourceDao.findById(id);
        if (resource == null) {
            throw new BusinessException("E6006", "资源不存在：id=" + id);
        }
        return resource;
    }

    private void validateFields(Resource resource) {
        ValidateUtil.requireNonBlank(resource.getResourceName(), "资源名称", 2, 80);
        ValidateUtil.requireNonBlank(resource.getResourceCode(), "资源编码", 2, 100);
        ValidateUtil.requireMatch(resource.getResourceCode(), "资源编码", CODE_PATTERN,
                "字母开头，可含字母/数字/冒号/短横线/下划线/点，如 report:finance");
        ResourceType.of(resource.getResourceType()); // 非法类型抛业务异常
        ValidateUtil.optionalMaxLength(resource.getResourcePath(), "资源路径", 255);
        ValidateUtil.optionalMaxLength(resource.getDescription(), "描述", 200);
    }

    private void normalize(Resource resource) {
        resource.setResourceName(resource.getResourceName().trim());
        resource.setResourceCode(resource.getResourceCode().trim());
        resource.setResourceType(ResourceType.of(resource.getResourceType()).name());
        resource.setResourcePath(ValidateUtil.trimToNull(resource.getResourcePath()));
        resource.setDescription(ValidateUtil.trimToNull(resource.getDescription()));
        resource.setStatus(resource.getStatus() == null ? Constants.STATUS_ENABLED : resource.getStatus());
    }
}

package com.cqu.rbac.domain.entity;

import com.cqu.rbac.common.constants.Constants;

/**
 * 权限实体（对应表 sys_permission）。
 * <p>parentId 构成权限/菜单树；resourceId 关联具体资源（可空，用于 RESOURCE 类型权限）。</p>
 */
public class Permission extends BaseEntity {

    /** 权限名称 */
    private String permissionName;
    /** 权限编码（唯一，如 user:create） */
    private String permissionCode;
    /** 权限类型：MENU / OPERATION / RESOURCE */
    private String permissionType;
    /** 父权限 id（0 表示顶级，用于构建权限树/菜单树） */
    private Long parentId = Constants.ROOT_PARENT_ID;
    /** 关联资源 id（可空） */
    private Long resourceId;
    /** 菜单路径/标识 */
    private String path;
    /** 排序号 */
    private Integer sortOrder = 0;
    /** 状态：1 启用 / 0 禁用 */
    private Integer status = Constants.STATUS_ENABLED;

    public Permission() {
    }

    public Permission(String permissionName, String permissionCode, String permissionType) {
        this.permissionName = permissionName;
        this.permissionCode = permissionCode;
        this.permissionType = permissionType;
    }

    /** 是否启用 */
    public boolean isEnabled() {
        return status != null && status == Constants.STATUS_ENABLED;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    public String getPermissionCode() {
        return permissionCode;
    }

    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }

    public String getPermissionType() {
        return permissionType;
    }

    public void setPermissionType(String permissionType) {
        this.permissionType = permissionType;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Permission{id=" + id + ", code='" + permissionCode + "', type='" + permissionType + "'}";
    }
}

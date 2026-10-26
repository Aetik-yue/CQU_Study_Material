package com.cqu.rbac.domain.entity;

import com.cqu.rbac.common.constants.Constants;

/**
 * 角色实体（对应表 sys_role）。
 * <p>parentId 支持角色继承：用户拥有某角色时，可递归继承其父角色的权限。</p>
 */
public class Role extends BaseEntity {

    /** 角色名称 */
    private String roleName;
    /** 角色编码（唯一，如 ADMIN） */
    private String roleCode;
    /** 父角色 id（0 表示顶级角色） */
    private Long parentId = Constants.ROOT_PARENT_ID;
    /** 描述 */
    private String description;
    /** 状态：1 启用 / 0 禁用 */
    private Integer status = Constants.STATUS_ENABLED;

    public Role() {
    }

    public Role(String roleName, String roleCode) {
        this.roleName = roleName;
        this.roleCode = roleCode;
    }

    /** 是否启用 */
    public boolean isEnabled() {
        return status != null && status == Constants.STATUS_ENABLED;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Role{id=" + id + ", roleName='" + roleName + "', roleCode='" + roleCode + "'}";
    }
}

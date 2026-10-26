package com.cqu.rbac.domain.entity;

import com.cqu.rbac.common.constants.Constants;

/**
 * 资源实体（对应表 sys_resource）。
 * <p>资源是被权限保护的对象：菜单、文件、接口、数据等。</p>
 */
public class Resource extends BaseEntity {

    /** 资源名称 */
    private String resourceName;
    /** 资源编码（唯一） */
    private String resourceCode;
    /** 资源类型：MENU / FILE / API / DATA */
    private String resourceType;
    /** 资源路径/URI */
    private String resourcePath;
    /** 描述 */
    private String description;
    /** 状态：1 启用 / 0 禁用 */
    private Integer status = Constants.STATUS_ENABLED;

    public Resource() {
    }

    public Resource(String resourceName, String resourceCode, String resourceType) {
        this.resourceName = resourceName;
        this.resourceCode = resourceCode;
        this.resourceType = resourceType;
    }

    /** 是否启用 */
    public boolean isEnabled() {
        return status != null && status == Constants.STATUS_ENABLED;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getResourceCode() {
        return resourceCode;
    }

    public void setResourceCode(String resourceCode) {
        this.resourceCode = resourceCode;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
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
        return "Resource{id=" + id + ", code='" + resourceCode + "', type='" + resourceType + "'}";
    }
}

package com.cqu.rbac.domain.vo;

import com.cqu.rbac.domain.entity.Permission;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限树节点 VO：包装权限实体 + 子节点，用于多级权限/菜单树形展示。
 */
public class PermissionNode {

    /** 节点对应的权限 */
    private Permission permission;
    /** 子节点 */
    private List<PermissionNode> children = new ArrayList<>();
    /** 层级（根为 0，便于缩进展示） */
    private int level;

    public PermissionNode() {
    }

    public PermissionNode(Permission permission) {
        this.permission = permission;
    }

    /** 添加子节点 */
    public void addChild(PermissionNode child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
    }

    /** 是否存在子节点 */
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    public List<PermissionNode> getChildren() {
        return children;
    }

    public void setChildren(List<PermissionNode> children) {
        this.children = children;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return "PermissionNode{" + permission + ", children=" + (children == null ? 0 : children.size()) + "}";
    }
}

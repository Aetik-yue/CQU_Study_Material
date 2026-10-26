package com.cqu.rbac.domain.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单树节点 VO：由用户的 MENU 类型权限构建，供动态菜单展示。
 */
public class MenuNode {

    /** 菜单编码（对应权限编码） */
    private String code;
    /** 菜单名称 */
    private String name;
    /** 菜单路径/标识 */
    private String path;
    /** 子菜单 */
    private List<MenuNode> children = new ArrayList<>();

    public MenuNode() {
    }

    public MenuNode(String code, String name, String path) {
        this.code = code;
        this.name = name;
        this.path = path;
    }

    /** 添加子节点 */
    public void addChild(MenuNode child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<MenuNode> getChildren() {
        return children;
    }

    public void setChildren(List<MenuNode> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return "MenuNode{code='" + code + "', name='" + name + "'}";
    }
}

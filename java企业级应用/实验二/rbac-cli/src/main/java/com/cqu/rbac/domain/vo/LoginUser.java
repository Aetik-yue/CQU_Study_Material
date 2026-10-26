package com.cqu.rbac.domain.vo;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * 登录会话用户 VO：登录成功后放入会话，承载"身份 + 角色 + 有效权限 + 可访问菜单"。
 */
public class LoginUser {

    /** 用户 id */
    private Long userId;
    /** 用户名 */
    private String username;
    /** 姓名 */
    private String realName;
    /** 角色编码列表（如 ADMIN） */
    private List<String> roleCodes = new ArrayList<>();
    /** 角色名称列表（展示用，如"超级管理员"） */
    private List<String> roleNames = new ArrayList<>();
    /** 有效权限码集合（含角色继承；TreeSet 保证有序便于展示） */
    private Set<String> permissions = new TreeSet<>();
    /** 可访问菜单树 */
    private List<MenuNode> menus = new ArrayList<>();

    public LoginUser() {
    }

    /** 是否拥有指定权限码 */
    public boolean hasPermission(String code) {
        return code != null && permissions != null && permissions.contains(code);
    }

    /** 展示名：优先姓名，其次用户名 */
    public String realNameOrUsername() {
        return realName == null || realName.isEmpty() ? username : realName;
    }

    /** 是否拥有任意一个权限码 */
    public boolean hasAnyPermission(Set<String> codes) {
        if (codes == null || codes.isEmpty() || permissions == null) {
            return false;
        }
        Set<String> copy = new LinkedHashSet<>(codes);
        copy.retainAll(permissions);
        return !copy.isEmpty();
    }

    /** 角色编码列表的展示串 */
    public String roleCodesText() {
        return roleCodes == null || roleCodes.isEmpty() ? "（无角色）" : String.join("、", roleCodes);
    }

    /** 角色名称列表的展示串 */
    public String roleNamesText() {
        return roleNames == null || roleNames.isEmpty() ? "（无角色）" : String.join("、", roleNames);
    }

    // ---------------- getter / setter ----------------

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public List<String> getRoleCodes() {
        return roleCodes;
    }

    public void setRoleCodes(List<String> roleCodes) {
        this.roleCodes = roleCodes;
    }

    public List<String> getRoleNames() {
        return roleNames;
    }

    public void setRoleNames(List<String> roleNames) {
        this.roleNames = roleNames;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    public List<MenuNode> getMenus() {
        return menus;
    }

    public void setMenus(List<MenuNode> menus) {
        this.menus = menus;
    }

    @Override
    public String toString() {
        return "LoginUser{username='" + username + "', roles=" + roleCodes + ", permissions=" + permissions.size() + "}";
    }
}

package com.cqu.rbac.domain.vo;

import com.cqu.rbac.domain.entity.User;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户列表展示 VO：用户实体 + 角色名称聚合（避免列表页 N+1 查询）。
 */
public class UserVO {

    /** 用户实体 */
    private User user;
    /** 角色名称聚合串（如"超级管理员"或"用户管理员,审计员"） */
    private String roleNames;
    /** 角色 ID 列表（供"分配角色"表达式计算增删） */
    private List<Long> roleIds = new ArrayList<>();

    public UserVO() {
    }

    public UserVO(User user, String roleNames) {
        this.user = user;
        this.roleNames = roleNames;
    }

    /** 展示用角色名（空时显示占位符） */
    public String roleNamesText() {
        return roleNames == null || roleNames.isEmpty() ? "（未分配）" : roleNames.replace(",", "、");
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getRoleNames() {
        return roleNames;
    }

    public void setRoleNames(String roleNames) {
        this.roleNames = roleNames;
    }

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds == null ? new ArrayList<>() : roleIds;
    }

    @Override
    public String toString() {
        return "UserVO{" + user + ", roleNames='" + roleNames + "'}";
    }
}

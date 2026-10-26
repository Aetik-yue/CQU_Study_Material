package com.cqu.rbac.service;

import com.cqu.rbac.domain.entity.Permission;
import com.cqu.rbac.domain.vo.PermissionNode;

import java.util.List;

/**
 * 权限管理服务。
 * <p>权限注解 / 审计注解标注在实现类方法上。</p>
 */
public interface PermissionService {

    /** 查询全部权限 */
    List<Permission> listAll();

    /** 按类型查询权限（可为 null 表示全部） */
    List<Permission> listByType(String type);

    /** 查询权限详情 */
    Permission get(Long id);

    /** 构建权限树（type 可为 null 表示全部） */
    List<PermissionNode> tree(String type);

    /** 新增权限 */
    void create(Permission permission);

    /** 修改权限（编码不可修改） */
    void update(Permission permission);

    /** 删除权限（有子权限或被角色引用时禁止） */
    void delete(Long id);
}

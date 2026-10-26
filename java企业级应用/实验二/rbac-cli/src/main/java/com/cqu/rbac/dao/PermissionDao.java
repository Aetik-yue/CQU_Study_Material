package com.cqu.rbac.dao;

import com.cqu.rbac.domain.entity.Permission;

import java.util.Collection;
import java.util.List;

/**
 * 权限 DAO：单表 CRUD + 按角色/类型/资源查询 + 引用计数（删除保护用）。
 */
public interface PermissionDao extends BaseDao<Permission, Long> {

    /** 按权限编码查询（唯一键） */
    Permission findByCode(String code);

    /** 按类型查询（仅启用，按 sort_order 排序） */
    List<Permission> findByType(String type);

    /** 查询一批角色拥有的全部启用权限（去重由 SQL DISTINCT 保证） */
    List<Permission> findByRoleIds(Collection<Long> roleIds);

    /** 统计子权限数量（删除保护用） */
    long countChildren(Long parentId);

    /** 统计被角色引用的次数（删除保护用） */
    long countRoleReferences(Long permissionId);

    /** 按 id 集合批量查询（含禁用，供角色权限明细展示） */
    List<Permission> findByIds(Collection<Long> ids);

    /** 查询关联某资源的权限列表（资源访问校验用） */
    List<Permission> findByResourceId(Long resourceId);

    /** 统计关联某资源的权限数量（资源删除保护用） */
    long countByResourceId(Long resourceId);
}

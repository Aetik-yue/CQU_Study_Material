package com.cqu.rbac.dao;

import com.cqu.rbac.domain.entity.Role;

import java.util.List;

/**
 * 角色 DAO：单表 CRUD + 角色权限关系维护 + 角色继承链查询。
 */
public interface RoleDao extends BaseDao<Role, Long> {

    /** 按角色编码查询（唯一键） */
    Role findByCode(String code);

    /** 查询角色拥有的权限 id 列表 */
    List<Long> findPermissionIds(Long roleId);

    /** 批量插入 角色-权限 关系 */
    void insertRolePermissions(Long roleId, List<Long> permissionIds);

    /** 删除角色全部 角色-权限 关系 */
    void deleteRolePermissions(Long roleId);

    /** 统计绑定该角色的用户数量（删除保护用） */
    long countUsersByRole(Long roleId);

    /** 统计以该角色为父角色的子角色数量（删除保护用） */
    long countChildren(Long roleId);

    /**
     * 查询角色继承链 id 列表：包含自身与全部祖先（沿 parent_id 向上递归）。
     * <p>实现为循环 + 防环（visited 集合），避免递归 CTE 的兼容性问题。</p>
     */
    List<Long> findRoleChainIds(Long roleId);
}

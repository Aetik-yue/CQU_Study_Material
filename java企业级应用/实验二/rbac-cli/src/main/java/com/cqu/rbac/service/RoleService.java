package com.cqu.rbac.service;

import com.cqu.rbac.common.result.PageResult;
import com.cqu.rbac.domain.dto.RoleDTO;
import com.cqu.rbac.domain.entity.Permission;
import com.cqu.rbac.domain.entity.Role;

import java.util.List;

/**
 * 角色管理服务。
 * <p>权限注解 / 审计注解标注在实现类方法上。</p>
 */
public interface RoleService {

    /** 查询全部角色 */
    List<Role> list();

    /** 分页查询角色 */
    PageResult<Role> page(int page, int size);

    /** 查询角色详情 */
    Role get(Long id);

    /** 新增角色（支持指定父角色，构成继承关系） */
    void create(RoleDTO dto);

    /** 修改角色（编码不可修改） */
    void update(RoleDTO dto);

    /** 删除角色（禁止删除内置角色/被用户绑定/有子角色） */
    void delete(Long id);

    /** 给角色分配权限（事务内先删后增） */
    void assignPermissions(Long roleId, List<Long> permissionIds);

    /** 查看角色直接拥有的权限 */
    List<Permission> listPermissions(Long roleId);
}

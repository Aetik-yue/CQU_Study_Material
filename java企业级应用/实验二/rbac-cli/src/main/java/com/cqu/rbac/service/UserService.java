package com.cqu.rbac.service;

import com.cqu.rbac.common.result.PageResult;
import com.cqu.rbac.domain.dto.ChangePasswordDTO;
import com.cqu.rbac.domain.dto.UserDTO;
import com.cqu.rbac.domain.entity.Role;
import com.cqu.rbac.domain.vo.UserVO;

import java.util.List;

/**
 * 用户管理服务。
 * <p>注意：权限注解 {@code @RequiresPermission} / 审计注解 {@code @AuditLog}
 * 标注在实现类（UserServiceImpl）的方法上，由动态代理读取。</p>
 */
public interface UserService {

    /** 条件分页查询（关键字匹配用户名/姓名） */
    PageResult<UserVO> page(String keyword, Integer status, int page, int size);

    /** 查询用户详情（含角色名） */
    UserVO get(Long id);

    /** 新增用户 */
    void create(UserDTO dto);

    /** 修改用户资料（用户名/编码不可修改） */
    void update(UserDTO dto);

    /** 删除用户（禁止删除自己与超级管理员） */
    void delete(Long id);

    /** 给用户分配角色（事务内先删后增） */
    void assignRoles(Long userId, List<Long> roleIds);

    /** 启用/禁用用户 */
    void toggleStatus(Long userId);

    /** 重置用户密码（管理员操作） */
    void resetPassword(Long userId, String newPassword);

    /** 修改本人密码（校验旧密码） */
    void changeOwnPassword(ChangePasswordDTO dto);

    /** 查询全部角色（供"分配角色"选择） */
    List<Role> listRoles();
}

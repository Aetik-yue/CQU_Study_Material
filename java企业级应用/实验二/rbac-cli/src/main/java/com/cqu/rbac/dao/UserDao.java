package com.cqu.rbac.dao;

import com.cqu.rbac.domain.entity.User;
import com.cqu.rbac.domain.vo.UserVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户 DAO：单表 CRUD + 用户角色关系维护 + 登录安全字段维护。
 */
public interface UserDao extends BaseDao<User, Long> {

    /** 按用户名查询（唯一键） */
    User findByUsername(String username);

    /** 条件分页查询（关键字匹配用户名/姓名；status 可为 null 表示不限），返回带角色名的 VO */
    List<UserVO> findPage(String keyword, Integer status, int offset, int size);

    /** 条件计数（与 findPage 条件一致） */
    long countByCondition(String keyword, Integer status);

    /** 查询用户的角色 id 列表 */
    List<Long> findRoleIds(Long userId);

    /** 查询用户的角色编码列表 */
    List<String> findRoleCodes(Long userId);

    /** 查询用户的角色名称列表 */
    List<String> findRoleNames(Long userId);

    /** 批量插入 用户-角色 关系 */
    void insertUserRoles(Long userId, List<Long> roleIds);

    /** 删除用户全部 用户-角色 关系 */
    void deleteUserRoles(Long userId);

    /** 查询拥有指定角色的用户 id 列表（权限缓存失效定位用） */
    List<Long> findUserIdsByRole(Long roleId);

    /** 更新密码（含盐值） */
    int updatePassword(Long userId, String password, String salt);

    /** 更新登录失败计数与锁定时间 */
    int updateLockInfo(Long userId, int failCount, LocalDateTime lockUntil);

    /** 登录成功：清零失败计数并解除锁定 */
    int clearLoginFail(Long userId);
}

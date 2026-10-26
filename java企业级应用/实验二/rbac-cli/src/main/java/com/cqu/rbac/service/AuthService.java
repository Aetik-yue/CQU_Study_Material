package com.cqu.rbac.service;

import com.cqu.rbac.domain.dto.LoginDTO;
import com.cqu.rbac.domain.vo.LoginUser;

/**
 * 认证服务：登录 / 注销。
 * <p>登录不做前置权限校验（login 无 @RequiresPermission），但必须记录审计；
 * 登录失败计数与锁定逻辑在本服务实现。</p>
 */
public interface AuthService {

    /**
     * 登录：校验账号状态、锁定、密码；成功后加载角色/权限/菜单并写入会话。
     *
     * @return 登录会话用户
     */
    LoginUser login(LoginDTO dto);

    /** 注销：清空会话 */
    void logout();
}

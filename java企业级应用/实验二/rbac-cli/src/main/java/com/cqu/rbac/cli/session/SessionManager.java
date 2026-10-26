package com.cqu.rbac.cli.session;

import com.cqu.rbac.common.exception.AuthException;
import com.cqu.rbac.domain.vo.LoginUser;

/**
 * 会话管理器：保存"当前登录用户"。
 * <p>基于 ThreadLocal 实现（命令行应用为单线程交互，天然适配）；
 * 未登录时访问抛出 {@link AuthException}。</p>
 */
public class SessionManager {

    /** 当前登录用户（ThreadLocal 持有） */
    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    /** 写入登录用户 */
    public void setLoginUser(LoginUser user) {
        HOLDER.set(user);
    }

    /**
     * 获取当前登录用户。
     *
     * @throws AuthException 尚未登录时抛出
     */
    public LoginUser getLoginUser() {
        LoginUser user = HOLDER.get();
        if (user == null) {
            throw new AuthException(AuthException.CODE_NOT_LOGIN, "尚未登录，请先登录");
        }
        return user;
    }

    /** 获取当前登录用户，未登录返回 null（审计等场景使用） */
    public LoginUser getLoginUserOrNull() {
        return HOLDER.get();
    }

    /** 是否已登录 */
    public boolean isLoggedIn() {
        return HOLDER.get() != null;
    }

    /** 当前登录用户 id（未登录返回 null） */
    public Long currentUserId() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getUserId();
    }

    /** 当前登录用户名（未登录返回 null） */
    public String currentUsername() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getUsername();
    }

    /** 清空会话（注销） */
    public void clear() {
        HOLDER.remove();
    }
}

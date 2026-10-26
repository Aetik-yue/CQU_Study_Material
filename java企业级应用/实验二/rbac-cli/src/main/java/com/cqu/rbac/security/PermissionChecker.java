package com.cqu.rbac.security;

import com.cqu.rbac.cli.session.SessionManager;
import com.cqu.rbac.common.enums.Logical;
import com.cqu.rbac.common.exception.AuthException;
import com.cqu.rbac.domain.vo.LoginUser;
import com.cqu.rbac.support.AuthorizationFacade;

import java.util.Arrays;

/**
 * 权限判定器：供动态代理在方法执行前调用。
 * <p>设计模式：策略 Strategy——AND（全部满足）/ OR（任意满足）两种判定策略；
 * 判定数据来自 {@link AuthorizationFacade}（角色继承 + 缓存）。</p>
 */
public class PermissionChecker {

    private final AuthorizationFacade facade;
    private final SessionManager session;

    public PermissionChecker(AuthorizationFacade facade, SessionManager session) {
        this.facade = facade;
        this.session = session;
    }

    /** 校验权限（默认 AND：全部满足） */
    public void check(String... codes) {
        check(Logical.AND, codes);
    }

    /**
     * 按指定逻辑校验权限；不满足时抛出 {@link AuthException}。
     */
    public void check(Logical logical, String... codes) {
        if (codes == null || codes.length == 0) {
            return; // 未声明所需权限 → 放行
        }
        LoginUser user = currentUser();
        Long userId = user.getUserId();

        boolean granted = logical == Logical.OR
                ? facade.hasAny(userId, Arrays.asList(codes))
                : facade.hasAll(userId, Arrays.asList(codes));

        if (!granted) {
            String need = (logical == Logical.OR ? "任意一个" : "全部") + "权限 " + Arrays.toString(codes);
            throw new AuthException(AuthException.CODE_NO_PERMISSION,
                    "权限不足：需要" + need);
        }
    }

    /**
     * 获取当前登录用户；未登录时抛 AuthException（由代理转为"鉴权失败"审计）。
     */
    private LoginUser currentUser() {
        return session.getLoginUser();
    }
}

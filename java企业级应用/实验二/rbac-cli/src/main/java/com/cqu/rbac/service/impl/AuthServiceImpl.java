package com.cqu.rbac.service.impl;

import com.cqu.rbac.cli.session.SessionManager;
import com.cqu.rbac.common.annotation.AuditLog;
import com.cqu.rbac.common.constants.Constants;
import com.cqu.rbac.common.exception.AuthException;
import com.cqu.rbac.common.util.ValidateUtil;
import com.cqu.rbac.dao.UserDao;
import com.cqu.rbac.domain.dto.LoginDTO;
import com.cqu.rbac.domain.entity.User;
import com.cqu.rbac.domain.vo.LoginUser;
import com.cqu.rbac.security.PasswordEncoder;
import com.cqu.rbac.service.AuthService;
import com.cqu.rbac.support.AuthorizationFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 认证服务实现：登录（含失败计数与锁定）、注销。
 * <p>登录/登出不设置 @RequiresPermission（前置校验），但通过 @AuditLog 保证审计记录。</p>
 */
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final AuthorizationFacade authorizationFacade;
    private final SessionManager sessionManager;

    public AuthServiceImpl(UserDao userDao, PasswordEncoder passwordEncoder,
                           AuthorizationFacade authorizationFacade, SessionManager sessionManager) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
        this.authorizationFacade = authorizationFacade;
        this.sessionManager = sessionManager;
    }

    @Override
    @AuditLog(module = Constants.MODULE_AUTH, operation = "LOGIN")
    public LoginUser login(LoginDTO dto) {
        // 1) 入参校验
        if (dto == null || ValidateUtil.isBlank(dto.getUsername()) || ValidateUtil.isBlank(dto.getPassword())) {
            throw new AuthException(AuthException.CODE_LOGIN_FAILED, "用户名和密码不能为空");
        }
        String username = dto.getUsername().trim();

        // 2) 用户存在性（不暴露"用户不存在"与"密码错误"的差异，防账号枚举）
        User user = userDao.findByUsername(username);
        if (user == null) {
            log.warn("登录失败：用户名 [{}] 不存在", username);
            throw new AuthException(AuthException.CODE_LOGIN_FAILED, "用户名或密码错误");
        }

        // 3) 账号状态
        if (!user.isEnabled()) {
            log.warn("登录失败：账号 [{}] 已被禁用", username);
            throw new AuthException(AuthException.CODE_ACCOUNT_DISABLED,
                    "账号 [" + username + "] 已被禁用，请联系管理员");
        }

        // 4) 锁定检查
        if (user.isLocked()) {
            log.warn("登录失败：账号 [{}] 处于锁定期，截止 {}", username, user.getLockUntil());
            throw new AuthException(AuthException.CODE_ACCOUNT_LOCKED,
                    "账号已锁定，请于 " + user.getLockUntil().format(TIME_FORMATTER) + " 后重试");
        }
        // 锁定期已过：重置历史失败计数，给予新一轮尝试机会
        if (user.getLockUntil() != null) {
            userDao.clearLoginFail(user.getId());
            user.setLoginFailCount(0);
            user.setLockUntil(null);
        }

        // 5) 密码校验（恒定时间比较）
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            int failCount = (user.getLoginFailCount() == null ? 0 : user.getLoginFailCount()) + 1;
            if (failCount >= Constants.MAX_LOGIN_FAIL) {
                LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(Constants.LOCK_MINUTES);
                userDao.updateLockInfo(user.getId(), failCount, lockUntil);
                log.warn("登录失败：账号 [{}] 连续失败 {} 次，已锁定至 {}", username, failCount, lockUntil);
                throw new AuthException(AuthException.CODE_ACCOUNT_LOCKED,
                        "密码错误次数过多，账号已锁定 " + Constants.LOCK_MINUTES + " 分钟");
            }
            userDao.updateLockInfo(user.getId(), failCount, null);
            log.warn("登录失败：账号 [{}] 密码错误（第 {} 次）", username, failCount);
            throw new AuthException(AuthException.CODE_LOGIN_FAILED,
                    "密码错误（剩余尝试次数 " + (Constants.MAX_LOGIN_FAIL - failCount) + " 次）");
        }

        // 6) 登录成功：清失败计数 → 装载角色/权限/菜单 → 写入会话
        userDao.clearLoginFail(user.getId());
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setRealName(user.getRealName());
        loginUser.setRoleCodes(userDao.findRoleCodes(user.getId()));
        loginUser.setRoleNames(userDao.findRoleNames(user.getId()));
        loginUser.setPermissions(authorizationFacade.loadPermissions(user.getId()));
        loginUser.setMenus(authorizationFacade.buildMenus(user.getId()));
        sessionManager.setLoginUser(loginUser);
        log.info("登录成功：用户 [{}]，角色 {}，有效权限 {} 项",
                username, loginUser.roleCodesText(), loginUser.getPermissions().size());
        return loginUser;
    }

    @Override
    @AuditLog(module = Constants.MODULE_AUTH, operation = "LOGOUT")
    public void logout() {
        LoginUser user = sessionManager.getLoginUserOrNull();
        if (user != null) {
            log.info("用户 [{}] 已注销", user.getUsername());
        }
        sessionManager.clear();
    }
}

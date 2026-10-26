package com.cqu.rbac.service.impl;

import com.cqu.rbac.cli.session.SessionManager;
import com.cqu.rbac.common.annotation.AuditLog;
import com.cqu.rbac.common.annotation.RequiresPermission;
import com.cqu.rbac.common.constants.Constants;
import com.cqu.rbac.common.enums.Logical;
import com.cqu.rbac.common.exception.BusinessException;
import com.cqu.rbac.common.result.PageQuery;
import com.cqu.rbac.common.result.PageResult;
import com.cqu.rbac.common.util.ValidateUtil;
import com.cqu.rbac.dao.RoleDao;
import com.cqu.rbac.dao.UserDao;
import com.cqu.rbac.domain.dto.ChangePasswordDTO;
import com.cqu.rbac.domain.dto.UserDTO;
import com.cqu.rbac.domain.entity.Role;
import com.cqu.rbac.domain.entity.User;
import com.cqu.rbac.domain.vo.UserVO;
import com.cqu.rbac.jdbc.TransactionTemplate;
import com.cqu.rbac.security.PasswordEncoder;
import com.cqu.rbac.service.UserService;
import com.cqu.rbac.support.AuthorizationFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 用户管理服务实现。
 * <p>书写要点：输入校验 → 业务规则 → 事务 → 缓存失效 → 审计（由代理/注解完成）。</p>
 * <p>权限注解与审计注解标注在本实现类的方法上（由动态代理读取）。</p>
 */
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    /** 用户名：字母开头，字母/数字/下划线，3-50 位 */
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]{2,49}$");
    /** 邮箱 */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    /** 手机号（中国大陆 11 位） */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    /** 密码长度限制 */
    private static final int PASSWORD_MIN = 6;
    private static final int PASSWORD_MAX = 64;

    private final UserDao userDao;
    private final RoleDao roleDao;
    private final TransactionTemplate transactionTemplate;
    private final SessionManager sessionManager;
    private final PasswordEncoder passwordEncoder;
    private final AuthorizationFacade authorizationFacade;

    public UserServiceImpl(UserDao userDao, RoleDao roleDao, TransactionTemplate transactionTemplate,
                           SessionManager sessionManager, PasswordEncoder passwordEncoder,
                           AuthorizationFacade authorizationFacade) {
        this.userDao = userDao;
        this.roleDao = roleDao;
        this.transactionTemplate = transactionTemplate;
        this.sessionManager = sessionManager;
        this.passwordEncoder = passwordEncoder;
        this.authorizationFacade = authorizationFacade;
    }

    // ---------------- 查询 ----------------

    @Override
    public PageResult<UserVO> page(String keyword, Integer status, int page, int size) {
        PageQuery query = new PageQuery(page, size);
        long total = userDao.countByCondition(keyword, status);
        if (total == 0) {
            return PageResult.empty(query.getPage(), query.getSize());
        }
        List<UserVO> records = userDao.findPage(keyword, status, query.getOffset(), query.getSize());
        return PageResult.of(records, total, query.getPage(), query.getSize());
    }

    @Override
    public UserVO get(Long id) {
        User user = requireUser(id);
        List<String> roleNames = userDao.findRoleNames(id);
        UserVO vo = new UserVO(user, String.join(",", roleNames));
        vo.setRoleIds(userDao.findRoleIds(id));
        return vo;
    }

    // ---------------- 新增 / 修改 / 删除 ----------------

    @Override
    @RequiresPermission("user:create")
    @AuditLog(module = Constants.MODULE_USER, operation = "CREATE")
    public void create(UserDTO dto) {
        // 1) 输入校验（携带字段键，CLI 表单可只重填出错项）
        ValidateUtil.requireNonBlank(dto.getUsername(), "username", "用户名", 3, 50);
        ValidateUtil.requireMatch(dto.getUsername(), "username", "用户名", USERNAME_PATTERN,
                "字母开头，可含字母/数字/下划线，3-50 位");
        ValidateUtil.requireNonBlank(dto.getPassword(), "password", "密码", PASSWORD_MIN, PASSWORD_MAX);
        ValidateUtil.optionalMaxLength(dto.getRealName(), "realName", "姓名", 50);
        ValidateUtil.optionalMatch(dto.getEmail(), "email", "邮箱", EMAIL_PATTERN, "如 user@example.com");
        ValidateUtil.optionalMatch(dto.getPhone(), "phone", "电话", PHONE_PATTERN, "11 位手机号");
        validateStatus(dto.getStatus());

        // 2) 业务规则：用户名唯一
        String username = dto.getUsername().trim();
        if (userDao.findByUsername(username) != null) {
            throw new BusinessException("E3001", "用户名 [" + username + "] 已存在", "username");
        }

        // 3) 组装并落库（密码 PBKDF2 加盐哈希，绝不存明文）
        String encoded = passwordEncoder.encode(dto.getPassword());
        User user = User.builder()
                .username(username)
                .password(encoded)
                .salt(extractSalt(encoded))
                .realName(ValidateUtil.trimToNull(dto.getRealName()))
                .email(ValidateUtil.trimToNull(dto.getEmail()))
                .phone(ValidateUtil.trimToNull(dto.getPhone()))
                .status(dto.getStatus() == null ? Constants.STATUS_ENABLED : dto.getStatus())
                .build();
        userDao.insert(user);
        log.info("创建用户 [{}] 成功（id={}）", username, user.getId());
    }

    @Override
    @RequiresPermission("user:update")
    @AuditLog(module = Constants.MODULE_USER, operation = "UPDATE")
    public void update(UserDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("E3002", "用户 id 不能为空");
        }
        User user = requireUser(dto.getId());

        ValidateUtil.optionalMaxLength(dto.getRealName(), "realName", "姓名", 50);
        ValidateUtil.optionalMatch(dto.getEmail(), "email", "邮箱", EMAIL_PATTERN, "如 user@example.com");
        ValidateUtil.optionalMatch(dto.getPhone(), "phone", "电话", PHONE_PATTERN, "11 位手机号");
        validateStatus(dto.getStatus());

        // 禁用超级管理员需保护
        if (dto.getStatus() != null && dto.getStatus() == Constants.STATUS_DISABLED) {
            ensureNotBuiltinAdmin(user.getId(), "禁用");
        }

        user.setRealName(ValidateUtil.trimToNull(dto.getRealName()));
        user.setEmail(ValidateUtil.trimToNull(dto.getEmail()));
        user.setPhone(ValidateUtil.trimToNull(dto.getPhone()));
        if (dto.getStatus() != null) {
            user.setStatus(dto.getStatus());
        }
        userDao.update(user);
        log.info("修改用户 [{}] 成功（id={}）", user.getUsername(), user.getId());
    }

    @Override
    @RequiresPermission("user:delete")
    @AuditLog(module = Constants.MODULE_USER, operation = "DELETE")
    public void delete(Long id) {
        User user = requireUser(id);

        // 业务规则：禁止删除自己 / 禁止删除超级管理员
        Long currentUserId = sessionManager.currentUserId();
        if (id.equals(currentUserId)) {
            throw new BusinessException("E3003", "不能删除当前登录用户");
        }
        ensureNotBuiltinAdmin(id, "删除");

        // 事务：先删关系表再删主表
        transactionTemplate.execute(() -> {
            userDao.deleteUserRoles(id);
            userDao.deleteById(id);
            return null;
        });
        authorizationFacade.evict(id);
        log.info("删除用户 [{}] 成功（id={}）", user.getUsername(), id);
    }

    @Override
    @RequiresPermission("user:assign-role")
    @AuditLog(module = Constants.MODULE_USER, operation = "ASSIGN_ROLE")
    public void assignRoles(Long userId, List<Long> roleIds) {
        User user = requireUser(userId);
        // 校验角色存在性
        if (roleIds != null) {
            for (Long roleId : roleIds) {
                if (roleDao.findById(roleId) == null) {
                    throw new BusinessException("E3004", "角色不存在：id=" + roleId);
                }
            }
        }
        // 事务：先删后增，保证原子性
        transactionTemplate.execute(() -> {
            userDao.deleteUserRoles(userId);
            userDao.insertUserRoles(userId, roleIds);
            return null;
        });
        // 角色变化 → 失效该用户权限缓存
        authorizationFacade.evict(userId);
        log.info("给用户 [{}] 分配角色 {} 成功", user.getUsername(), roleIds);
    }

    @Override
    @RequiresPermission("user:toggle")
    @AuditLog(module = Constants.MODULE_USER, operation = "TOGGLE_STATUS")
    public void toggleStatus(Long userId) {
        User user = requireUser(userId);
        Long currentUserId = sessionManager.currentUserId();
        if (userId.equals(currentUserId)) {
            throw new BusinessException("E3005", "不能禁用当前登录用户");
        }
        if (user.isEnabled()) {
            ensureNotBuiltinAdmin(userId, "禁用");
        }
        user.setStatus(user.isEnabled() ? Constants.STATUS_DISABLED : Constants.STATUS_ENABLED);
        userDao.update(user);
        log.info("用户 [{}] 状态切换为 {}（id={}）", user.getUsername(),
                user.isEnabled() ? "启用" : "禁用", userId);
    }

    @Override
    @RequiresPermission("user:reset-password")
    @AuditLog(module = Constants.MODULE_USER, operation = "RESET_PASSWORD")
    public void resetPassword(Long userId, String newPassword) {
        User user = requireUser(userId);
        ValidateUtil.requireNonBlank(newPassword, "newPassword", "新密码", PASSWORD_MIN, PASSWORD_MAX);

        String encoded = passwordEncoder.encode(newPassword);
        userDao.updatePassword(userId, encoded, extractSalt(encoded));
        userDao.clearLoginFail(userId); // 重置密码同时解除锁定
        log.info("重置用户 [{}] 密码成功（id={}）", user.getUsername(), userId);
    }

    // ---------------- 本人改密 ----------------

    @Override
    @RequiresPermission("profile:password")
    @AuditLog(module = Constants.MODULE_PROFILE, operation = "CHANGE_PASSWORD")
    public void changeOwnPassword(ChangePasswordDTO dto) {
        // 以会话中的当前用户为准，防止越权修改他人密码
        Long currentUserId = sessionManager.currentUserId();
        User user = requireUser(currentUserId);

        if (ValidateUtil.isBlank(dto.getOldPassword())) {
            throw new BusinessException("E3006", "旧密码不能为空", "oldPassword");
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("E3007", "旧密码不正确", "oldPassword");
        }
        ValidateUtil.requireNonBlank(dto.getNewPassword(), "newPassword", "新密码", PASSWORD_MIN, PASSWORD_MAX);
        if (dto.getNewPassword().equals(dto.getOldPassword())) {
            throw new BusinessException("E3008", "新密码不能与旧密码相同", "newPassword");
        }

        String encoded = passwordEncoder.encode(dto.getNewPassword());
        userDao.updatePassword(currentUserId, encoded, extractSalt(encoded));
        log.info("用户 [{}] 修改本人密码成功", user.getUsername());
    }

    // ---------------- 角色选择 ----------------

    @Override
    @RequiresPermission(value = {"user:assign-role", "role:list"}, logical = Logical.OR)
    public List<Role> listRoles() {
        return roleDao.findAll();
    }

    // ---------------- 私有辅助 ----------------

    /** 查询用户，不存在抛出业务异常 */
    private User requireUser(Long id) {
        if (id == null) {
            throw new BusinessException("E3009", "用户 id 不能为空");
        }
        User user = userDao.findById(id);
        if (user == null) {
            throw new BusinessException("E3010", "用户不存在：id=" + id);
        }
        return user;
    }

    /** 内置超级管理员保护 */
    private void ensureNotBuiltinAdmin(Long userId, String action) {
        if (userDao.findRoleCodes(userId).contains(Constants.ROLE_CODE_ADMIN)) {
            throw new BusinessException("E3011", "内置超级管理员账号禁止" + action);
        }
    }

    /** 状态值校验：仅允许 0/1 或 null */
    private void validateStatus(Integer status) {
        if (status != null && status != Constants.STATUS_ENABLED && status != Constants.STATUS_DISABLED) {
            throw new BusinessException("E3012", "状态值非法：仅支持 1（启用）/ 0（禁用）", "status");
        }
    }

    /** 从编码串中提取盐值（盐值单独存储到 salt 列） */
    private String extractSalt(String encoded) {
        int idx = encoded.indexOf(':');
        return idx > 0 ? encoded.substring(0, idx) : null;
    }
}

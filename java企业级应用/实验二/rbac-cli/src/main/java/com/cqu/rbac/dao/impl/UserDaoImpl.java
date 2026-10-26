package com.cqu.rbac.dao.impl;

import com.cqu.rbac.dao.UserDao;
import com.cqu.rbac.domain.entity.User;
import com.cqu.rbac.domain.vo.UserVO;
import com.cqu.rbac.jdbc.JdbcTemplate;
import com.cqu.rbac.jdbc.RowMapper;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户 DAO 实现：继承 AbstractJdbcDao 复用模板方法，补充用户专属查询与关系维护。
 */
public class UserDaoImpl extends AbstractJdbcDao<User, Long> implements UserDao {

    public UserDaoImpl(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    // ---------------- 模板方法原语 ----------------

    @Override
    protected String getTableName() {
        return "sys_user";
    }

    @Override
    protected RowMapper<User> getRowMapper() {
        return UserDaoImpl::mapUser;
    }

    @Override
    protected String getInsertSql() {
        return "INSERT INTO sys_user (username, password, salt, real_name, email, phone, status, login_fail_count) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSql() {
        // 通用更新仅覆盖资料字段；密码走 updatePassword，登录安全字段走 updateLockInfo/clearLoginFail
        return "UPDATE sys_user SET real_name = ?, email = ?, phone = ?, status = ? WHERE id = ?";
    }

    @Override
    protected Object[] getInsertParams(User u) {
        return new Object[]{
                u.getUsername(), u.getPassword(), u.getSalt(),
                u.getRealName(), u.getEmail(), u.getPhone(),
                u.getStatus() == null ? 1 : u.getStatus(),
                u.getLoginFailCount() == null ? 0 : u.getLoginFailCount()
        };
    }

    @Override
    protected Object[] getUpdateParams(User u) {
        return new Object[]{u.getRealName(), u.getEmail(), u.getPhone(), u.getStatus(), u.getId()};
    }

    @Override
    protected void assignGeneratedId(User entity, Number id) {
        entity.setId(id == null ? null : id.longValue());
    }

    // ---------------- 行映射 ----------------

    /** 结果集行 → User 实体（下划线列名显式映射到驼峰字段） */
    static User mapUser(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return User.builder()
                .id(rs.getLong("id"))
                .username(rs.getString("username"))
                .password(rs.getString("password"))
                .salt(rs.getString("salt"))
                .realName(rs.getString("real_name"))
                .email(rs.getString("email"))
                .phone(rs.getString("phone"))
                .status(rs.getInt("status"))
                .loginFailCount(rs.getInt("login_fail_count"))
                .lockUntil(toLocalDateTime(rs.getTimestamp("lock_until")))
                .createTime(toLocalDateTime(rs.getTimestamp("create_time")))
                .updateTime(toLocalDateTime(rs.getTimestamp("update_time")))
                .build();
    }

    /** Timestamp → LocalDateTime（可空安全） */
    static LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }

    // ---------------- 用户专属查询 ----------------

    @Override
    public User findByUsername(String username) {
        return jdbcTemplate.queryForObjectOrNull(
                "SELECT * FROM sys_user WHERE username = ?", getRowMapper(), username);
    }

    @Override
    public List<UserVO> findPage(String keyword, Integer status, int offset, int size) {
        StringBuilder sql = new StringBuilder(
                "SELECT u.*, (SELECT GROUP_CONCAT(r.role_name ORDER BY r.id SEPARATOR ',') "
                        + "        FROM sys_user_role ur JOIN sys_role r ON r.id = ur.role_id "
                        + "        WHERE ur.user_id = u.id) AS role_names "
                        + "FROM sys_user u WHERE 1 = 1");
        List<Object> args = new ArrayList<>();
        appendCondition(sql, args, keyword, status);
        sql.append(" ORDER BY u.id LIMIT ?, ?");
        args.add(offset);
        args.add(size);
        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
            User user = mapUser(rs, rowNum);
            return new UserVO(user, rs.getString("role_names"));
        }, args.toArray());
    }

    @Override
    public long countByCondition(String keyword, Integer status) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM sys_user u WHERE 1 = 1");
        List<Object> args = new ArrayList<>();
        appendCondition(sql, args, keyword, status);
        return jdbcTemplate.count(sql.toString(), args.toArray());
    }

    /** 拼接条件：关键字模糊匹配用户名/姓名；状态精确匹配 */
    private void appendCondition(StringBuilder sql, List<Object> args, String keyword, Integer status) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (u.username LIKE ? OR u.real_name LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
        }
        if (status != null) {
            sql.append(" AND u.status = ?");
            args.add(status);
        }
    }

    // ---------------- 关系表维护 ----------------

    @Override
    public List<Long> findRoleIds(Long userId) {
        return jdbcTemplate.query("SELECT role_id FROM sys_user_role WHERE user_id = ? ORDER BY role_id",
                (rs, rowNum) -> rs.getLong(1), userId);
    }

    @Override
    public List<String> findRoleCodes(Long userId) {
        return jdbcTemplate.query(
                "SELECT r.role_code FROM sys_user_role ur JOIN sys_role r ON r.id = ur.role_id "
                        + "WHERE ur.user_id = ? AND r.status = 1 ORDER BY r.id",
                (rs, rowNum) -> rs.getString(1), userId);
    }

    @Override
    public List<String> findRoleNames(Long userId) {
        return jdbcTemplate.query(
                "SELECT r.role_name FROM sys_user_role ur JOIN sys_role r ON r.id = ur.role_id "
                        + "WHERE ur.user_id = ? AND r.status = 1 ORDER BY r.id",
                (rs, rowNum) -> rs.getString(1), userId);
    }

    @Override
    public void insertUserRoles(Long userId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        StringBuilder sql = new StringBuilder("INSERT INTO sys_user_role (user_id, role_id) VALUES ");
        List<Object> args = new ArrayList<>();
        for (int i = 0; i < roleIds.size(); i++) {
            sql.append(i == 0 ? "(?, ?)" : ", (?, ?)");
            args.add(userId);
            args.add(roleIds.get(i));
        }
        jdbcTemplate.update(sql.toString(), args.toArray());
    }

    @Override
    public void deleteUserRoles(Long userId) {
        jdbcTemplate.update("DELETE FROM sys_user_role WHERE user_id = ?", userId);
    }

    @Override
    public List<Long> findUserIdsByRole(Long roleId) {
        return jdbcTemplate.query("SELECT user_id FROM sys_user_role WHERE role_id = ?",
                (rs, rowNum) -> rs.getLong(1), roleId);
    }

    // ---------------- 密码与登录安全字段 ----------------

    @Override
    public int updatePassword(Long userId, String password, String salt) {
        return jdbcTemplate.update("UPDATE sys_user SET password = ?, salt = ? WHERE id = ?",
                password, salt, userId);
    }

    @Override
    public int updateLockInfo(Long userId, int failCount, LocalDateTime lockUntil) {
        return jdbcTemplate.update("UPDATE sys_user SET login_fail_count = ?, lock_until = ? WHERE id = ?",
                failCount, lockUntil, userId);
    }

    @Override
    public int clearLoginFail(Long userId) {
        return jdbcTemplate.update("UPDATE sys_user SET login_fail_count = 0, lock_until = NULL WHERE id = ?",
                userId);
    }
}

package com.cqu.rbac.domain.entity;

import com.cqu.rbac.common.constants.Constants;

import java.time.LocalDateTime;

/**
 * 用户实体（对应表 sys_user）。
 * <p>设计模式：建造者 Builder——通过静态内部类 {@link Builder} 支持链式构建复杂对象（体现内部类）。</p>
 */
public class User extends BaseEntity {

    /** 登录名（唯一） */
    private String username;
    /** 密码（PBKDF2 加盐哈希，格式 saltHex:hashHex） */
    private String password;
    /** 盐值（hex） */
    private String salt;
    /** 姓名 */
    private String realName;
    /** 邮箱 */
    private String email;
    /** 电话 */
    private String phone;
    /** 状态：1 启用 / 0 禁用 */
    private Integer status = Constants.STATUS_ENABLED;
    /** 连续登录失败次数 */
    private Integer loginFailCount = 0;
    /** 锁定截止时间 */
    private LocalDateTime lockUntil;

    public User() {
    }

    private User(Builder builder) {
        this.id = builder.id;
        this.username = builder.username;
        this.password = builder.password;
        this.salt = builder.salt;
        this.realName = builder.realName;
        this.email = builder.email;
        this.phone = builder.phone;
        this.status = builder.status;
        this.loginFailCount = builder.loginFailCount;
        this.lockUntil = builder.lockUntil;
        this.createTime = builder.createTime;
        this.updateTime = builder.updateTime;
    }

    /** 是否启用 */
    public boolean isEnabled() {
        return status != null && status == Constants.STATUS_ENABLED;
    }

    /** 当前是否处于锁定状态 */
    public boolean isLocked() {
        return lockUntil != null && lockUntil.isAfter(LocalDateTime.now());
    }

    /** 展示名：优先姓名，其次用户名 */
    public String displayName() {
        return realName == null || realName.isEmpty() ? username : realName;
    }

    // ---------------- 建造者 ----------------

    public static Builder builder() {
        return new Builder();
    }

    /** 建造者静态内部类（设计模式：Builder + 内部类） */
    public static class Builder {
        private Long id;
        private String username;
        private String password;
        private String salt;
        private String realName;
        private String email;
        private String phone;
        private Integer status = Constants.STATUS_ENABLED;
        private Integer loginFailCount = 0;
        private LocalDateTime lockUntil;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder salt(String salt) {
            this.salt = salt;
            return this;
        }

        public Builder realName(String realName) {
            this.realName = realName;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder status(Integer status) {
            this.status = status;
            return this;
        }

        public Builder loginFailCount(Integer loginFailCount) {
            this.loginFailCount = loginFailCount;
            return this;
        }

        public Builder lockUntil(LocalDateTime lockUntil) {
            this.lockUntil = lockUntil;
            return this;
        }

        public Builder createTime(LocalDateTime createTime) {
            this.createTime = createTime;
            return this;
        }

        public Builder updateTime(LocalDateTime updateTime) {
            this.updateTime = updateTime;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }

    // ---------------- getter / setter ----------------

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getLoginFailCount() {
        return loginFailCount;
    }

    public void setLoginFailCount(Integer loginFailCount) {
        this.loginFailCount = loginFailCount;
    }

    public LocalDateTime getLockUntil() {
        return lockUntil;
    }

    public void setLockUntil(LocalDateTime lockUntil) {
        this.lockUntil = lockUntil;
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', realName='" + realName + "'}";
    }
}

package com.cqu.rbac.domain.dto;

/**
 * 用户新增/修改 DTO。
 * <p>新增时 username/password 必填；修改时 id 必填，password 不通过本 DTO 修改（使用重置/改密接口）。</p>
 */
public class UserDTO {

    /** 用户 id（修改时必填） */
    private Long id;
    /** 用户名（新增必填） */
    private String username;
    /** 密码（新增必填；修改时忽略） */
    private String password;
    /** 姓名 */
    private String realName;
    /** 邮箱 */
    private String email;
    /** 电话 */
    private String phone;
    /** 状态：1 启用 / 0 禁用（修改时可调整） */
    private Integer status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    /** 注意：不输出密码，防止日志泄露 */
    @Override
    public String toString() {
        return "UserDTO{id=" + id + ", username='" + username + "'}";
    }
}

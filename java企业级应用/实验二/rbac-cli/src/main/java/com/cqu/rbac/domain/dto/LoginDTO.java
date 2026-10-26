package com.cqu.rbac.domain.dto;

/**
 * 登录请求 DTO。
 */
public class LoginDTO {

    /** 用户名 */
    private String username;
    /** 密码（明文，仅内存中传递，绝不落库/落日志） */
    private String password;

    public LoginDTO() {
    }

    public LoginDTO(String username, String password) {
        this.username = username;
        this.password = password;
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

    /** 注意：不输出密码，防止日志泄露 */
    @Override
    public String toString() {
        return "LoginDTO{username='" + username + "'}";
    }
}

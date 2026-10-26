package com.cqu.rbac.cli.screens;

import com.cqu.rbac.cli.console.ConsolePrinter;
import com.cqu.rbac.cli.console.ConsoleReader;
import com.cqu.rbac.cli.console.InputClosedException;
import com.cqu.rbac.cli.console.Theme;
import com.cqu.rbac.common.constants.Constants;
import com.cqu.rbac.common.exception.BusinessException;
import com.cqu.rbac.domain.dto.LoginDTO;
import com.cqu.rbac.domain.vo.LoginUser;
import com.cqu.rbac.support.ServiceLocator;

/**
 * 登录页（品牌首页）：字符画 LOGO + 系统信息 + 登录表单。
 * <p>登录成功/失败、账号锁定等均会由认证服务的审计切面自动记录。</p>
 */
public class LoginScreen extends AbstractScreen {

    public LoginScreen(ConsolePrinter printer, ConsoleReader reader, ServiceLocator locator) {
        super(printer, reader, locator);
    }

    /**
     * 展示登录页。
     *
     * @return true 表示登录成功
     */
    public boolean show() {
        for (int attempt = 1; attempt <= Constants.MAX_LOGIN_ATTEMPTS; attempt++) {
            render(attempt);

            String username;
            try {
                username = reader.readString(printer.prompt("用户名"));
            } catch (InputClosedException e) {
                return false; // 输入流结束（脚本模式）：安静退出
            }
            if ("q".equalsIgnoreCase(username)) {
                return false;
            }
            if (username.isEmpty()) {
                printer.warn("用户名不能为空");
                reader.pause();
                continue;
            }

            String password = reader.readPassword(printer.prompt("密　码"));
            if (password.isEmpty()) {
                printer.warn("密码不能为空");
                reader.pause();
                continue;
            }

            try {
                LoginUser user = locator.authService().login(new LoginDTO(username, password));
                printer.blankLine();
                printer.success("登录成功，欢迎 " + user.realNameOrUsername()
                        + "（" + user.roleNamesText() + "）");
                reader.pause();
                return true;
            } catch (BusinessException e) {
                printer.blankLine();
                printer.error(e.getMessage());
                printer.info("本会话已尝试 " + attempt + "/" + Constants.MAX_LOGIN_ATTEMPTS + " 次");
                reader.pause();
            }
        }
        printer.blankLine();
        printer.error("连续失败次数过多，程序退出");
        return false;
    }

    /** 渲染登录界面：品牌面板 + 系统信息 + 提示 */
    private void render(int attempt) {
        printer.clearScreen();
        printer.logoPanel();
        printer.blankLine();

        Theme theme = printer.theme();
        String leftPlain = "默认账号 admin / admin123";
        String rightPlain = "MySQL 8 · HikariCP · PBKDF2";
        int pad = printer.contentWidth() + 4 - Theme.displayWidth(leftPlain) - Theme.displayWidth(rightPlain);
        printer.line("  " + theme.fg(Theme.MUTED, leftPlain) + Theme.repeat(' ', Math.max(1, pad))
                + theme.fg(Theme.MUTED, rightPlain));

        if (attempt > 1) {
            printer.warn("登录失败，请重试（第 " + attempt + "/" + Constants.MAX_LOGIN_ATTEMPTS + " 次）");
        } else {
            printer.info("输入 q 可退出程序");
        }
        printer.blankLine();
    }
}

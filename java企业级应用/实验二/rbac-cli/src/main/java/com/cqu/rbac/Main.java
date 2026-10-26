package com.cqu.rbac;

import com.cqu.rbac.cli.console.ConsolePrinter;
import com.cqu.rbac.cli.console.ConsoleReader;
import com.cqu.rbac.cli.menu.MenuEngine;
import com.cqu.rbac.cli.screens.LoginScreen;
import com.cqu.rbac.cli.screens.MainScreen;
import com.cqu.rbac.common.exception.GlobalExceptionHandler;
import com.cqu.rbac.support.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 程序入口。
 * <p>启动流程：初始化服务工厂 → 自动建表与数据初始化 → 登录页 → 主菜单（按权限动态渲染）→ 退出。</p>
 */
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        ServiceLocator locator = ServiceLocator.getInstance();
        try {
            // 1) 组装对象图（数据源 / DAO / 服务 / 代理 / 事件总线）
            locator.init();

            // 2) 首次启动自动建表 + 初始化数据（幂等）
            locator.dataInitializer().initialize();

            // 3) 命令行界面：登录 → 主菜单
            ConsolePrinter printer = new ConsolePrinter(
                    locator.config().getBoolean("ui.color", true),
                    locator.config().getBoolean("ui.clearScreen", true),
                    locator.config().getBoolean("ui.trueColor", true));
            ConsoleReader reader = new ConsoleReader(printer.theme());
            MenuEngine engine = new MenuEngine(printer, reader, locator.sessionManager());

            LoginScreen loginScreen = new LoginScreen(printer, reader, locator);
            if (loginScreen.show()) {
                engine.run(new MainScreen(printer, reader, locator, engine).build());
            }

            // 4) 收尾：若仍处于登录态则注销（记录登出审计）
            if (locator.sessionManager().isLoggedIn()) {
                try {
                    locator.authService().logout();
                } catch (Exception e) {
                    log.warn("注销失败：{}", e.getMessage());
                }
            }
            reader.pause();
            printer.blankLine();
            printer.info("程序已退出，感谢使用 " + com.cqu.rbac.common.constants.Constants.APP_NAME);
            log.info("程序正常退出");
        } catch (Throwable e) {
            GlobalExceptionHandler.handleFatal(e);
        } finally {
            locator.shutdown();
        }
    }
}

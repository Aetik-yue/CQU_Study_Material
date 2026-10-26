package com.cqu.rbac.cli.screens;

import com.cqu.rbac.cli.console.ConsolePrinter;
import com.cqu.rbac.cli.console.ConsoleReader;
import com.cqu.rbac.cli.menu.Menu;
import com.cqu.rbac.cli.menu.MenuEngine;
import com.cqu.rbac.domain.vo.LoginUser;
import com.cqu.rbac.support.ServiceLocator;

/**
 * 主菜单（按权限动态构建）：菜单入口根据当前用户的权限动态显示，无权限的入口不可见。
 * <p>视觉：业务功能 / 账户 两个分组，双列排布。</p>
 */
public class MainScreen extends AbstractScreen {

    private final MenuEngine engine;

    public MainScreen(ConsolePrinter printer, ConsoleReader reader, ServiceLocator locator, MenuEngine engine) {
        super(printer, reader, locator);
        this.engine = engine;
    }

    /** 构建主菜单（动态鉴权 + 分组） */
    public Menu build() {
        LoginUser me = locator.sessionManager().getLoginUser();
        Menu menu = new Menu("主菜单");

        int index = 1;
        boolean businessSection = false;

        if (me.hasPermission("menu:user")) {
            businessSection = ensureSection(menu, businessSection, "业 务 功 能");
            menu.add(String.valueOf(index++), "用户管理",
                    () -> engine.run(new UserScreen(printer, reader, locator, engine).build()));
        }
        if (me.hasPermission("menu:role")) {
            businessSection = ensureSection(menu, businessSection, "业 务 功 能");
            menu.add(String.valueOf(index++), "角色管理",
                    () -> engine.run(new RoleScreen(printer, reader, locator, engine).build()));
        }
        if (me.hasPermission("menu:permission")) {
            businessSection = ensureSection(menu, businessSection, "业 务 功 能");
            menu.add(String.valueOf(index++), "权限管理",
                    () -> engine.run(new PermissionScreen(printer, reader, locator).build()));
        }
        if (me.hasPermission("menu:resource")) {
            businessSection = ensureSection(menu, businessSection, "业 务 功 能");
            menu.add(String.valueOf(index++), "资源管理",
                    () -> engine.run(new ResourceScreen(printer, reader, locator).build()));
        }
        if (me.hasPermission("resource:check")) {
            businessSection = ensureSection(menu, businessSection, "业 务 功 能");
            menu.add(String.valueOf(index++), "权限校验演示",
                    () -> engine.run(new AuthzTestScreen(printer, reader, locator).build()));
        }
        if (me.hasPermission("menu:audit")) {
            businessSection = ensureSection(menu, businessSection, "业 务 功 能");
            menu.add(String.valueOf(index++), "审计日志",
                    () -> engine.run(new AuditScreen(printer, reader, locator).build()));
        }

        menu.addSection("账 户");
        menu.add(String.valueOf(index++), "修改本人密码",
                () -> engine.run(new ProfileScreen(printer, reader, locator).build()));
        menu.add(String.valueOf(index++), "我的身份与权限", this::showMyIdentity);
        menu.addExit("0", "退出登录并关闭程序", () -> locator.authService().logout());
        return menu;
    }

    /** 首次遇到业务菜单时插入分组标题 */
    private boolean ensureSection(Menu menu, boolean added, String title) {
        if (!added) {
            menu.addSection(title);
        }
        return true;
    }

    // ---------------- 我的身份与权限 ----------------

    private void showMyIdentity() {
        LoginUser me = locator.sessionManager().getLoginUser();
        printIdentity(me);
        printMenuTree(me.getMenus(), "当前账号没有任何菜单权限");
    }
}

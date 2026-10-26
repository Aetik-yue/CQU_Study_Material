package com.cqu.rbac.cli.screens;

import com.cqu.rbac.cli.console.ConsolePrinter;
import com.cqu.rbac.cli.console.ConsoleReader;
import com.cqu.rbac.cli.menu.Menu;
import com.cqu.rbac.domain.dto.ChangePasswordDTO;
import com.cqu.rbac.domain.vo.LoginUser;
import com.cqu.rbac.support.ServiceLocator;

/**
 * 个人中心屏：修改本人密码（校验旧密码）、查看账号信息。
 * <p>密码输入支持 b 取消 / q 退出；两次输入不一致时只重来一次输入；旧密码错误可只重填旧密码。</p>
 */
public class ProfileScreen extends AbstractScreen {

    public ProfileScreen(ConsolePrinter printer, ConsoleReader reader, ServiceLocator locator) {
        super(printer, reader, locator);
    }

    public Menu build() {
        Menu menu = new Menu("个人中心");
        menu.addSection("账 户");
        menu.add("1", "修改本人密码", this::changePassword);
        menu.add("2", "查看我的账号信息", this::showAccount);
        menu.addBack("0", "返回上级");
        return menu;
    }

    /** 修改本人密码：校验旧密码 → 校验强度 → 更新（权限 profile:password，审计自动记录） */
    private void changePassword() {
        printer.title("修改本人密码");
        printOperationLegend();
        LoginUser me = locator.sessionManager().getLoginUser();
        printer.keyValue("账号", me.getUsername() + "（" + me.realNameOrUsername() + "）");

        String[] oldPassword = {reader.readPasswordOperation("旧密码（b 取消）：")};
        if (oldPassword[0].isEmpty()) {
            printer.warn("旧密码不能为空，操作已取消");
            return;
        }
        String[] newPassword = {askNewPasswordTwice()};

        submitWithRefill(
                () -> locator.userService().changeOwnPassword(
                        new ChangePasswordDTO(me.getUserId(), oldPassword[0], newPassword[0])),
                field -> {
                    if ("oldPassword".equals(field)) {
                        oldPassword[0] = reader.readPasswordOperation("旧密码（b 取消）：");
                        return true;
                    }
                    if ("newPassword".equals(field)) {
                        newPassword[0] = askNewPasswordTwice();
                        return true;
                    }
                    return false;
                });
        printer.success("密码修改成功，下次登录请使用新密码");
    }

    /** 两次输入新密码；不一致时提示并重新输入 */
    private String askNewPasswordTwice() {
        while (true) {
            String first = reader.readPasswordOperation("新密码（6-64 位，b 取消）：");
            if (first.isEmpty()) {
                printer.warn("新密码不能为空");
                continue;
            }
            String second = reader.readPasswordOperation("确认新密码（b 取消）：");
            if (first.equals(second)) {
                return first;
            }
            printer.warn("两次输入的新密码不一致，请重新输入");
        }
    }

    /** 查看账号信息（来自会话，不查库） */
    private void showAccount() {
        LoginUser me = locator.sessionManager().getLoginUser();
        printer.title("我的账号信息");
        printer.keyValue("用户 ID", String.valueOf(me.getUserId()));
        printer.keyValue("用户名", me.getUsername());
        printer.keyValue("姓名", me.getRealName() == null ? "-" : me.getRealName());
        printer.keyValue("角色", me.roleNamesText());
        printer.keyValue("角色编码", me.roleCodesText());
        printer.keyValue("有效权限数", String.valueOf(me.getPermissions().size()));
    }
}

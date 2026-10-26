package com.cqu.rbac.cli.console;

/**
 * 退出信号：用户在任意输入提示处输入 q（退出程序）时抛出，
 * 由菜单引擎捕获后统一走"注销 → 退出"流程。
 */
public class QuitSignal extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public QuitSignal() {
        super("用户请求退出程序");
    }
}

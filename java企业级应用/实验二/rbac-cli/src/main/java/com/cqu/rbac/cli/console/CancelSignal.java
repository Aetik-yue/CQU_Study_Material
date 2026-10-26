package com.cqu.rbac.cli.console;

/**
 * 操作取消信号：用户在多步输入（如编辑表单）中输入 b 时抛出，
 * 由菜单引擎捕获后仅提示"操作已取消"并返回当前菜单，不退出程序。
 */
public class CancelSignal extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CancelSignal() {
        super("操作已取消");
    }
}

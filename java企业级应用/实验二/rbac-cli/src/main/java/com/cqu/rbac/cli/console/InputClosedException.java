package com.cqu.rbac.cli.console;

/**
 * 输入流结束信号：当标准输入被关闭（如脚本驱动的 stdin 结束）时抛出，
 * 由菜单引擎捕获并安静退出，避免异常堆栈污染输出。
 */
public class InputClosedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InputClosedException() {
        super("输入流已结束");
    }
}

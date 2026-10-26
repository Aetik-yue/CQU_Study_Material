package com.cqu.rbac.cli.console;

/**
 * 返回信号：由动作主动结束当前菜单层级（例如"删除对象后关闭对象工作台"）。
 * <p>与 {@link QuitSignal}（退出程序）和 {@link CancelSignal}（取消当前操作）区分：
 * BackSignal 仅让当前 {@code MenuEngine.run} 返回上一级，不退出程序。</p>
 */
public class BackSignal extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BackSignal() {
        super("返回上级");
    }
}

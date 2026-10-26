package com.cqu.rbac.cli.console;

/**
 * ANSI 颜色基础常量（16 色与样式控制码）。
 * <p>更高层的配色（24 位真彩调色板、渐变、256 色降级）由 {@link Theme} 在此之上实现；
 * 本类只保留最基础的转义序列常量，供 {@code Theme} 与旧代码复用。</p>
 */
public final class AnsiColors {

    private AnsiColors() {
    }

    /** 重置样式 */
    public static final String RESET = "\u001B[0m";
    /** 加粗 */
    public static final String BOLD = "\u001B[1m";
    /** 红色（错误） */
    public static final String RED = "\u001B[31m";
    /** 绿色（成功） */
    public static final String GREEN = "\u001B[32m";
    /** 黄色（警告/提示） */
    public static final String YELLOW = "\u001B[33m";
    /** 青色（标题） */
    public static final String CYAN = "\u001B[36m";
    /** 灰色（次要信息） */
    public static final String GRAY = "\u001B[90m";

    /** 包裹颜色（color 为空则原样返回） */
    public static String wrap(String color, String text) {
        if (color == null || color.isEmpty()) {
            return text;
        }
        return color + text + RESET;
    }
}

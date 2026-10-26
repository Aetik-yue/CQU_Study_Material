package com.cqu.rbac.cli.console;

import com.cqu.rbac.common.result.Result;

import java.util.List;

/**
 * 控制台输出器：统一的"圆角面板 + 状态栏 + 带框表格 + 语义色徽标"渲染。
 * <p>设计参考开源 TUI 项目的通用语言（lipgloss 的圆角边框/内边距/对齐、git/gh 的状态色），
 * 使用 ANSI 自行绘制，不依赖第三方库。</p>
 * <ul>
 *   <li>面板内容宽度固定 72 列（外框 78 列，兼容 80 列终端）</li>
 *   <li>表格按显示宽度对齐（中文按 2 列），超宽时自动压缩列宽</li>
 *   <li>非交互式（脚本/重定向）下不输出清屏控制符，便于自动化测试</li>
 * </ul>
 */
public class ConsolePrinter {

    /** 面板内容宽度 */
    private static final int CONTENT_WIDTH = 72;
    /** 面板边框内宽（不含两角） */
    private static final int INNER_WIDTH = 74;
    /** 整行宽度（含左侧 2 列缩进） */
    private static final int OUTER_WIDTH = 78;
    /** 表格最大总宽（超过则自动压缩列宽） */
    private static final int TABLE_MAX_WIDTH = 112;
    /** 单元格最大显示宽度 */
    private static final int MAX_CELL_WIDTH = 30;

    private final Theme theme;
    private final boolean clearScreenEnabled;
    /** 是否交互式终端（清屏仅在交互式下生效） */
    private final boolean interactive;

    public ConsolePrinter(boolean colorEnabled, boolean clearScreenEnabled) {
        this(colorEnabled, clearScreenEnabled, true);
    }

    public ConsolePrinter(boolean colorEnabled, boolean clearScreenEnabled, boolean trueColor) {
        this.theme = new Theme(colorEnabled, trueColor);
        this.clearScreenEnabled = clearScreenEnabled;
        this.interactive = System.console() != null;
    }

    /** 主题（供 ConsoleReader 等组件复用配色） */
    public Theme theme() {
        return theme;
    }

    /** 面板内容区宽度 */
    public int contentWidth() {
        return CONTENT_WIDTH;
    }

    /** 是否交互式终端 */
    public boolean isInteractive() {
        return interactive;
    }

    // ---------------- 基础输出 ----------------

    /** 清屏（仅交互式 + 开启时生效；脚本环境不输出控制符） */
    public void clearScreen() {
        if (interactive && clearScreenEnabled) {
            System.out.print("\u001B[2J\u001B[H");
            System.out.flush();
        }
    }

    /** 空行 */
    public void blankLine() {
        System.out.println();
    }

    /** 原始行输出 */
    public void line(String text) {
        System.out.println(text == null ? "" : text);
    }

    /** 分区横线 */
    public void divider() {
        System.out.println("  " + theme.fg(Theme.BORDER, Theme.repeat('─', INNER_WIDTH)));
    }

    // ---------------- 面板（圆角边框） ----------------

    /**
     * 面板顶边：{@code ╭─ 标题 ───────────── 右侧信息 ─╮}
     *
     * @param leftTitle  左侧标题（可为空）
     * @param rightText  右侧信息（可为空）
     */
    public void panelTop(String leftTitle, String rightText) {
        String title = leftTitle == null ? "" : leftTitle;
        String right = rightText == null ? "" : rightText;

        if (title.isEmpty()) {
            System.out.println("  " + theme.fg(Theme.BORDER, "╭" + Theme.repeat('─', INNER_WIDTH) + "╮"));
            return;
        }

        int titleWidth = Theme.displayWidth(title);
        int rightWidth = Theme.displayWidth(right);
        // 内宽 = "─ " + 标题 + " " + 填充 + [右侧 " " + right + " "] + "─"
        int fill = rightWidth == 0
                ? INNER_WIDTH - (2 + titleWidth + 1) - 1
                : INNER_WIDTH - (2 + titleWidth + 1) - (1 + rightWidth + 1) - 1;
        if (fill < 1) {
            // 标题过长：截断标题，优先保证边框闭合
            title = Theme.stripAnsi(title);
            int maxTitle = Math.max(4, INNER_WIDTH - 8 - rightWidth);
            title = Theme.truncate(title, maxTitle);
            titleWidth = Theme.displayWidth(title);
            fill = rightWidth == 0
                    ? INNER_WIDTH - (2 + titleWidth + 1) - 1
                    : INNER_WIDTH - (2 + titleWidth + 1) - (1 + rightWidth + 1) - 1;
            fill = Math.max(1, fill);
        }

        StringBuilder sb = new StringBuilder("  ");
        sb.append(theme.fg(Theme.BORDER, "╭─ "));
        sb.append(theme.fgBold(Theme.TEXT, title));
        sb.append(theme.fg(Theme.BORDER, " "));
        sb.append(theme.fg(Theme.BORDER, Theme.repeat('─', fill)));
        if (rightWidth > 0) {
            sb.append(theme.fg(Theme.BORDER, " "));
            sb.append(theme.fg(Theme.MUTED, right));
            sb.append(theme.fg(Theme.BORDER, " "));
        }
        sb.append(theme.fg(Theme.BORDER, "─╮"));
        System.out.println(sb);
    }

    /** 面板分隔线：{@code ├─ 分组名 ──────────┤} */
    public void panelDivider(String label) {
        if (label == null || label.trim().isEmpty()) {
            System.out.println("  " + theme.fg(Theme.BORDER, "├" + Theme.repeat('─', INNER_WIDTH) + "┤"));
            return;
        }
        String text = label.trim();
        int fill = Math.max(1, INNER_WIDTH - (2 + Theme.displayWidth(text) + 1));
        StringBuilder sb = new StringBuilder("  ");
        sb.append(theme.fg(Theme.BORDER, "├─ "));
        sb.append(theme.fg(Theme.MUTED, text));
        sb.append(theme.fg(Theme.BORDER, " " + Theme.repeat('─', fill) + "┤"));
        System.out.println(sb);
    }

    /**
     * 面板内容行：{@code │ 内容 ... │}（内容可含 ANSI，按显示宽度补齐）。
     */
    public void panelLine(String content) {
        String text = content == null ? "" : content;
        int pad = Math.max(0, CONTENT_WIDTH - Theme.displayWidth(text));
        StringBuilder sb = new StringBuilder("  ");
        sb.append(theme.fg(Theme.BORDER, "│ "));
        sb.append(text);
        sb.append(Theme.repeat(' ', pad));
        sb.append(theme.fg(Theme.BORDER, " │"));
        System.out.println(sb);
    }

    /** 面板空行 */
    public void panelBlank() {
        panelLine("");
    }

    /** 面板底边：{@code ╰────────╯} */
    public void panelBottom() {
        System.out.println("  " + theme.fg(Theme.BORDER, "╰" + Theme.repeat('─', INNER_WIDTH) + "╯"));
    }

    /**
     * 登录页品牌面板：RBAC 字符画 LOGO（真彩渐变）+ 中英文副标题，整体居中。
     */
    public void logoPanel() {
        panelTop(null, null);
        panelBlank();
        for (String logoLine : Banner.logoLines()) {
            String colored = theme.gradient(logoLine, Theme.PRIMARY, Theme.ACCENT);
            panelLine(Theme.center(colored, CONTENT_WIDTH));
        }
        panelBlank();
        panelLine(Theme.center(theme.fgBold(Theme.TEXT, Banner.subtitleCn()), CONTENT_WIDTH));
        panelLine(Theme.center(theme.fg(Theme.MUTED, Banner.subtitleEn()), CONTENT_WIDTH));
        panelBlank();
        panelBottom();
    }

    // ---------------- 语义化输出（徽标） ----------------

    /** 横幅（品牌 + 单行面板，兼容旧调用） */
    public void banner(String text) {
        panelTop("◆ " + text, null);
        panelBottom();
    }

    /** 分区标题：{@code ▌ 标题} */
    public void title(String text) {
        System.out.println();
        System.out.println("  " + theme.fg(Theme.ACCENT, "▌") + " " + theme.fgBold(Theme.TEXT, text));
    }

    /** 成功（绿色 √） */
    public void success(String text) {
        System.out.println("  " + theme.fgBold(Theme.SUCCESS, "√ ") + theme.fg(Theme.SUCCESS, text));
    }

    /** 失败/错误（红色 ×） */
    public void error(String text) {
        System.out.println("  " + theme.fgBold(Theme.DANGER, "× ") + theme.fg(Theme.DANGER, text));
    }

    /** 警告/提示（黄色 !） */
    public void warn(String text) {
        System.out.println("  " + theme.fgBold(Theme.WARN, "! ") + theme.fg(Theme.WARN, text));
    }

    /** 普通信息（灰色 ·） */
    public void info(String text) {
        System.out.println("  " + theme.fg(Theme.MUTED, "· " + text));
    }

    /** 键值对行 */
    public void keyValue(String key, String value) {
        System.out.println("   " + theme.fg(Theme.MUTED, key + "：") + (value == null ? "-" : value));
    }

    /** 菜单项行（兼容旧调用） */
    public void menuLine(String key, String title) {
        System.out.println("   " + theme.fgBold(Theme.PRIMARY, key) + ". " + title);
    }

    /** 统一返回体输出：成功绿色 / 失败红色 */
    public void printResult(Result<?> result) {
        if (result == null) {
            return;
        }
        if (result.isSuccess()) {
            success(result.getMessage());
        } else {
            error(result.getMessage());
        }
    }

    /**
     * 底部状态栏：左侧说明 + 右侧快捷键，整行宽 78。
     */
    public void statusBar(String left, String right) {
        String leftText = "· " + (left == null ? "" : left);
        String rightText = (right == null || right.isEmpty()) ? "" : "· " + right;
        int pad = Math.max(1, OUTER_WIDTH - 2 - Theme.displayWidth(leftText) - Theme.displayWidth(rightText));
        System.out.println("  " + theme.fg(Theme.MUTED, leftText) + Theme.repeat(' ', pad)
                + (rightText.isEmpty() ? "" : theme.fg(Theme.MUTED, rightText)));
    }

    /**
     * 输入提示文本：{@code » 提示语 }（供 ConsoleReader 原样打印）。
     */
    public String prompt(String label) {
        return "  " + theme.fgBold(Theme.PRIMARY, "» ") + theme.fg(Theme.MUTED, label + " ");
    }

    // ---------------- 表格 ----------------

    /**
     * 输出带边框的对齐表格：圆角外框 + 表头分隔线，中文按 2 列宽对齐。
     *
     * @param headers 表头
     * @param rows    数据行（每行长度应与表头一致；缺失列按空串处理）
     */
    public void table(String[] headers, List<String[]> rows) {
        if (headers == null || headers.length == 0) {
            return;
        }
        int[] widths = computeWidths(headers, rows);

        // 顶边
        StringBuilder top = new StringBuilder("  ");
        top.append(theme.fg(Theme.BORDER, "┌"));
        for (int i = 0; i < widths.length; i++) {
            top.append(theme.fg(Theme.BORDER, Theme.repeat('─', widths[i] + 2)));
            top.append(theme.fg(Theme.BORDER, i == widths.length - 1 ? "┐" : "┬"));
        }
        System.out.println(top);

        // 表头
        System.out.println(buildRow(headers, widths, true));

        // 表头分隔线
        StringBuilder sep = new StringBuilder("  ");
        sep.append(theme.fg(Theme.BORDER, "├"));
        for (int i = 0; i < widths.length; i++) {
            sep.append(theme.fg(Theme.BORDER, Theme.repeat('─', widths[i] + 2)));
            sep.append(theme.fg(Theme.BORDER, i == widths.length - 1 ? "┤" : "┼"));
        }
        System.out.println(sep);

        // 数据行
        if (rows != null) {
            for (String[] row : rows) {
                System.out.println(buildRow(row, widths, false));
            }
        }

        // 底边
        StringBuilder bottom = new StringBuilder("  ");
        bottom.append(theme.fg(Theme.BORDER, "└"));
        for (int i = 0; i < widths.length; i++) {
            bottom.append(theme.fg(Theme.BORDER, Theme.repeat('─', widths[i] + 2)));
            bottom.append(theme.fg(Theme.BORDER, i == widths.length - 1 ? "┘" : "┴"));
        }
        System.out.println(bottom);
    }

    /** 计算列宽：内容最长者为准，超过最大表宽时从最宽列开始压缩 */
    private int[] computeWidths(String[] headers, List<String[]> rows) {
        int[] widths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            widths[i] = Math.min(Theme.displayWidth(headers[i]), MAX_CELL_WIDTH);
        }
        if (rows != null) {
            for (String[] row : rows) {
                for (int i = 0; i < widths.length && i < row.length; i++) {
                    widths[i] = Math.max(widths[i], Math.min(Theme.displayWidth(row[i]), MAX_CELL_WIDTH));
                }
            }
        }
        // 总宽 = 3 + Σ(w+3)
        int total = 3 + headers.length * 3;
        for (int w : widths) {
            total += w;
        }
        while (total > TABLE_MAX_WIDTH) {
            int widest = 0;
            for (int i = 1; i < widths.length; i++) {
                if (widths[i] > widths[widest]) {
                    widest = i;
                }
            }
            if (widths[widest] <= 6) {
                break; // 已到最小列宽，无法继续压缩
            }
            widths[widest]--;
            total--;
        }
        return widths;
    }

    /** 构建一行（表头加粗；状态词着色） */
    private String buildRow(String[] cells, int[] widths, boolean header) {
        StringBuilder sb = new StringBuilder("  ");
        sb.append(theme.fg(Theme.BORDER, "│"));
        for (int i = 0; i < widths.length; i++) {
            String cell = i < cells.length && cells[i] != null ? cells[i] : "";
            String plain = Theme.padRight(Theme.truncate(cell, widths[i]), widths[i]);
            String styled;
            if (header) {
                styled = theme.fgBold(Theme.TEXT, plain);
            } else {
                styled = colorizeChip(plain, cell);
            }
            sb.append(" ").append(styled).append(" ");
            sb.append(theme.fg(Theme.BORDER, "│"));
        }
        return sb.toString();
    }

    /** 对"状态/结果"类单元格着色（仅当单元格内容完全等于特定词时；不改变显示宽度） */
    private String colorizeChip(String padded, String rawContent) {
        String content = rawContent == null ? "" : rawContent.trim();
        if ("启用".equals(content)) {
            return padded.replace("启用", theme.fg(Theme.SUCCESS, "启用"));
        }
        if ("禁用".equals(content)) {
            return padded.replace("禁用", theme.fg(Theme.MUTED, "禁用"));
        }
        if ("SUCCESS".equals(content)) {
            return padded.replace("SUCCESS", theme.fg(Theme.SUCCESS, "SUCCESS"));
        }
        if ("FAILURE".equals(content)) {
            return padded.replace("FAILURE", theme.fg(Theme.DANGER, "FAILURE"));
        }
        return padded;
    }

    // ---------------- 宽度工具（向后兼容的静态入口） ----------------

    /** 计算字符串显示宽度（ANSI 不算宽度，CJK 按 2 列） */
    public static int displayWidth(String s) {
        return Theme.displayWidth(s);
    }
}

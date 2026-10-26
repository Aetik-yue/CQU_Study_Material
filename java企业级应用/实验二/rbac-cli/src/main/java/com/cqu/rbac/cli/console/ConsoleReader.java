package com.cqu.rbac.cli.console;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 控制台输入器：统一封装字符串/整数/密码/确认等读取，保证非法输入不崩溃。
 * <p>两种模式：</p>
 * <ul>
 *   <li>交互式（System.console() 可用）：使用 Console 读取（密码不回显）</li>
 *   <li>非交互式（IDE/重定向/脚本）：使用 UTF-8 BufferedReader 回退读取</li>
 * </ul>
 *
 * <p><b>输入语义约定</b>（批次1统一）：</p>
 * <ul>
 *   <li><b>操作类</b>输入（ID/序号、表单字段、密码、数字）：整行输入 {@code b} 取消当前操作（抛 {@link CancelSignal}），
 *       输入 {@code q} 退出程序（抛 {@link QuitSignal}）</li>
 *   <li><b>搜索/过滤类</b>输入（关键字、用户名、资源编码等）：{@code b}/{@code q} 作为普通文本，不拦截</li>
 *   <li>空输入：按各提示的默认值处理</li>
 * </ul>
 * <p>输入结束（EOF）抛出 {@link InputClosedException}。</p>
 */
public class ConsoleReader {

    private final BufferedReader bufferedReader;
    private final Console console;
    private final boolean interactive;
    /** 提示语着色（可为 null，表示不着色） */
    private final Theme theme;
    /** 密码明文回退提示是否已输出（避免每次输入都刷屏） */
    private boolean fallbackNoteShown = false;

    public ConsoleReader() {
        this(null);
    }

    public ConsoleReader(Theme theme) {
        this.console = System.console();
        this.interactive = console != null;
        this.theme = theme;
        this.bufferedReader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
    }

    /** 是否交互式终端 */
    public boolean isInteractive() {
        return interactive;
    }

    // ---------------- 文本读取 ----------------

    /** 读取一行（去首尾空白）；搜索/过滤类输入，b/q 作为普通文本 */
    public String readString(String prompt) {
        return rawReadLine(prompt).trim();
    }

    /** 读取一行（保留原样，不做 trim） */
    public String readRawLine(String prompt) {
        return rawReadLine(prompt);
    }

    /**
     * 读取非空字符串（操作类输入）：空输入重试；b 取消；q 退出。
     */
    public String readNonEmpty(String prompt) {
        while (true) {
            String value = readString(prompt);
            if (isCancel(value)) {
                throw new CancelSignal();
            }
            if (isQuit(value)) {
                throw new QuitSignal();
            }
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("  输入不能为空，请重新输入（b 取消 / q 退出）");
        }
    }

    /** 读取可空字符串（搜索/过滤类输入：空输入返回默认值，b/q 为普通文本） */
    public String readOptional(String prompt, String defaultValue) {
        String value = readString(prompt);
        return value.isEmpty() ? (defaultValue == null ? "" : defaultValue) : value;
    }

    /**
     * 读取可空字符串（操作类表单字段）：空输入返回默认值；b 取消；q 退出。
     */
    public String readOptionalOperation(String prompt, String defaultValue) {
        String value = readString(prompt);
        if (isCancel(value)) {
            throw new CancelSignal();
        }
        if (isQuit(value)) {
            throw new QuitSignal();
        }
        return value.isEmpty() ? (defaultValue == null ? "" : defaultValue) : value;
    }

    // ---------------- 数字读取 ----------------

    /** 读取可空整数（操作类：空输入返回默认值；非法数字重试；b 取消 / q 退出） */
    public Integer readOptionalInt(String prompt, Integer defaultValue) {
        while (true) {
            String value = readString(prompt);
            if (isCancel(value)) {
                throw new CancelSignal();
            }
            if (isQuit(value)) {
                throw new QuitSignal();
            }
            if (value.isEmpty()) {
                return defaultValue;
            }
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.out.println("  请输入合法的数字，或直接回车使用默认值（b 取消 / q 退出）");
            }
        }
    }

    /**
     * 读取必填长整数（用于 id 输入）。
     *
     * @return 数值；输入 b 取消时返回 null；输入 q 抛出 QuitSignal
     */
    public Long readLongRequired(String prompt) {
        while (true) {
            String value = readString(prompt);
            if (isCancel(value)) {
                return null; // 取消当前操作
            }
            if (isQuit(value)) {
                throw new QuitSignal();
            }
            try {
                long id = Long.parseLong(value);
                if (id <= 0) {
                    System.out.println("  id 必须为正整数，请重新输入（输入 b 取消）");
                    continue;
                }
                return id;
            } catch (NumberFormatException e) {
                System.out.println("  请输入合法的数字 id（输入 b 取消 / q 退出）");
            }
        }
    }

    /**
     * 确认提示：输入 y/Y 视为确认，其余输入均视为取消（不循环，适合脚本）。
     */
    public boolean confirm(String prompt) {
        String value = readString(prompt + "（y 确认 / 其他取消）");
        return "y".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value);
    }

    // ---------------- 密码读取 ----------------

    /**
     * 读取密码（登录场景：b/q 作为普通密码字符，不拦截）。
     * <p>交互式下不回显；非交互式回退为普通读取（供脚本/IDE 测试）。</p>
     */
    public String readPassword(String prompt) {
        return readPasswordInternal(prompt, false);
    }

    /**
     * 读取密码（操作场景：如重置密码/改密，b 取消、q 退出）。
     */
    public String readPasswordOperation(String prompt) {
        return readPasswordInternal(prompt, true);
    }

    private String readPasswordInternal(String prompt, boolean cancellable) {
        if (console != null) {
            char[] chars = console.readPassword("%s", prompt);
            if (chars == null) {
                throw new InputClosedException();
            }
            String value = new String(chars);
            if (cancellable) {
                if (isCancel(value)) {
                    throw new CancelSignal();
                }
                if (isQuit(value)) {
                    throw new QuitSignal();
                }
            }
            return value;
        }
        if (!fallbackNoteShown) {
            System.out.println();
            System.out.println("  " + color(Theme.MUTED, "（当前环境无法隐藏输入，密码将以明文显示）"));
            fallbackNoteShown = true;
        }
        String value = rawReadLine(prompt);
        if (cancellable) {
            if (isCancel(value)) {
                throw new CancelSignal();
            }
            if (isQuit(value)) {
                throw new QuitSignal();
            }
        }
        return value;
    }

    /**
     * 暂停等待回车（仅交互式终端生效；脚本/重定向环境直接跳过，避免吞掉脚本输入）。
     */
    public void pause() {
        if (!interactive) {
            return;
        }
        System.out.print("  " + color(Theme.MUTED, "按回车继续 …"));
        System.out.flush();
        try {
            String line = console != null ? console.readLine() : bufferedReader.readLine();
            if (line == null) {
                throw new InputClosedException();
            }
        } catch (IOException e) {
            throw new InputClosedException();
        }
    }

    // ---------------- 内部 ----------------

    /** 是否为取消键（整行 b/B） */
    private boolean isCancel(String value) {
        return "b".equalsIgnoreCase(value);
    }

    /** 是否为退出键（整行 q/Q） */
    private boolean isQuit(String value) {
        return "q".equalsIgnoreCase(value);
    }

    /** 打印提示语（未着色的提示统一渲染为灰色，已含 ANSI 的提示原样输出） */
    private void printPrompt(String prompt) {
        if (prompt == null || prompt.isEmpty()) {
            return;
        }
        String text = prompt;
        if (theme != null && theme.isColorEnabled() && prompt.indexOf('\u001B') < 0) {
            text = "  " + theme.fg(Theme.MUTED, prompt);
        }
        System.out.print(text);
        System.out.flush();
    }

    /** 底层读行：打印提示并读取；EOF 抛 InputClosedException */
    private String rawReadLine(String prompt) {
        printPrompt(prompt);
        try {
            String line = console != null ? console.readLine() : bufferedReader.readLine();
            if (line == null) {
                throw new InputClosedException();
            }
            return line;
        } catch (IOException e) {
            throw new InputClosedException();
        }
    }

    /** 着色（theme 为空或未启用时原样返回） */
    private String color(Theme.Color color, String text) {
        return theme == null ? text : theme.fg(color, text);
    }
}

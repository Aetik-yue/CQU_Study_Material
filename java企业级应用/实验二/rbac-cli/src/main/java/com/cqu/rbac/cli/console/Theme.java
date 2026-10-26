package com.cqu.rbac.cli.console;

/**
 * 终端主题：调色板 + 配色渲染（24 位真彩 / 256 色 / 16 色自动降级）+ 渐变 + 显示宽度工具。
 * <p>视觉风格参考开源 TUI 设计（Charm 的 lipgloss/bubbletea：圆角边框、真彩、内边距与对齐），
 * 但不引入任何第三方依赖，全部基于 ANSI 转义序列自封装。</p>
 * <p>Windows Console 支持 {@code ESC[38;2;r;g;b} 真彩；不支持时按"最近色"降级，
 * 也可通过 {@code ui.trueColor=false} 主动使用 256 色。</p>
 */
public final class Theme {

    /** ESC 前缀 */
    private static final String CSI = "\u001B[";
    /** 重置样式（复用基础常量类） */
    public static final String RESET = AnsiColors.RESET;

    /** 颜色渲染级别 */
    public enum Mode {
        /** 24 位真彩 */
        TRUE_COLOR,
        /** 256 色（xterm） */
        ANSI_256,
        /** 纯文本（关闭着色） */
        NONE
    }

    /**
     * 调色板颜色：真彩 RGB + 256 色索引（降级用）。
     */
    public static final class Color {
        private final int r;
        private final int g;
        private final int b;
        private final int index256;

        Color(int r, int g, int b, int index256) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.index256 = index256;
        }

        public int getR() {
            return r;
        }

        public int getG() {
            return g;
        }

        public int getB() {
            return b;
        }

        /** 线性插值混合两个颜色 */
        Color mix(Color other, double t) {
            double clamped = Math.max(0, Math.min(1, t));
            return new Color(
                    (int) Math.round(r + (other.r - r) * clamped),
                    (int) Math.round(g + (other.g - g) * clamped),
                    (int) Math.round(b + (other.b - b) * clamped),
                    index256);
        }
    }

    // ---------------- 调色板（深色终端友好） ----------------

    /** 主色：青蓝 */
    public static final Color PRIMARY = new Color(56, 189, 248, 45);
    /** 强调色：紫 */
    public static final Color ACCENT = new Color(167, 139, 250, 141);
    /** 成功：绿 */
    public static final Color SUCCESS = new Color(63, 185, 80, 78);
    /** 失败：红 */
    public static final Color DANGER = new Color(248, 81, 73, 203);
    /** 警告：黄 */
    public static final Color WARN = new Color(210, 153, 34, 179);
    /** 正文：浅灰白 */
    public static final Color TEXT = new Color(230, 237, 243, 253);
    /** 次要信息：中灰 */
    public static final Color MUTED = new Color(139, 148, 158, 245);
    /** 边框：深灰 */
    public static final Color BORDER = new Color(71, 85, 105, 240);
    /** 强调边框（品红色，用于品牌标题） */
    public static final Color BRAND = new Color(94, 234, 212, 50);

    private final Mode mode;

    public Theme(boolean colorEnabled, boolean trueColor) {
        if (!colorEnabled) {
            this.mode = Mode.NONE;
        } else {
            this.mode = trueColor ? Mode.TRUE_COLOR : Mode.ANSI_256;
        }
    }

    /** 是否启用着色 */
    public boolean isColorEnabled() {
        return mode != Mode.NONE;
    }

    // ---------------- 基础渲染 ----------------

    /** 前景色 */
    public String fg(Color color, String text) {
        if (mode == Mode.NONE || text == null || text.isEmpty()) {
            return text;
        }
        if (mode == Mode.TRUE_COLOR) {
            return CSI + "38;2;" + color.r + ";" + color.g + ";" + color.b + "m" + text + RESET;
        }
        return CSI + "38;5;" + color.index256 + "m" + text + RESET;
    }

    /** 加粗（保留当前颜色设置） */
    public String bold(String text) {
        if (mode == Mode.NONE || text == null || text.isEmpty()) {
            return text;
        }
        return AnsiColors.BOLD + text + RESET;
    }

    /** 前景色 + 加粗 */
    public String fgBold(Color color, String text) {
        if (mode == Mode.NONE) {
            return text;
        }
        if (mode == Mode.TRUE_COLOR) {
            return AnsiColors.BOLD + CSI + "38;2;" + color.r + ";" + color.g + ";" + color.b + "m" + text + RESET;
        }
        return AnsiColors.BOLD + CSI + "38;5;" + color.index256 + "m" + text + RESET;
    }

    /**
     * 横向渐变着色：按字符在整行中的水平位置，从第一个色标平滑过渡到最后一个色标。
     * <p>用于品牌 LOGO 与标题，营造现代终端工具（如 starship / lipgloss）的质感。</p>
     */
    public String gradient(String text, Color... stops) {
        if (mode == Mode.NONE || text == null || text.isEmpty() || stops == null || stops.length == 0) {
            return text;
        }
        int columns = Math.max(1, displayWidth(text));
        StringBuilder sb = new StringBuilder(text.length() * 8);
        int col = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            double t = columns == 1 ? 0 : (double) col / (columns - 1);
            Color color = interpolate(stops, t);
            sb.append(fgRaw(color)).append(c);
            col += isWide(c) ? 2 : 1;
        }
        sb.append(RESET);
        return sb.toString();
    }

    /** 按比例在多个色标间取色 */
    private Color interpolate(Color[] stops, double t) {
        if (stops.length == 1) {
            return stops[0];
        }
        double scaled = t * (stops.length - 1);
        int idx = (int) Math.floor(scaled);
        if (idx >= stops.length - 1) {
            return stops[stops.length - 1];
        }
        return stops[idx].mix(stops[idx + 1], scaled - idx);
    }

    /** 只输出颜色控制码（供逐字符渐变拼接） */
    private String fgRaw(Color color) {
        if (mode == Mode.TRUE_COLOR) {
            return CSI + "38;2;" + color.r + ";" + color.g + ";" + color.b + "m";
        }
        return CSI + "38;5;" + color.index256 + "m";
    }

    // ---------------- 显示宽度工具 ----------------

    /** 去掉字符串中的 ANSI 控制序列 */
    public static String stripAnsi(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        return s.replaceAll("\u001B\\[[0-9;?]*[a-zA-Z]", "");
    }

    /** 计算显示宽度：CJK 宽字符按 2 列，ANSI 控制序列不计宽度 */
    public static int displayWidth(String s) {
        if (s == null || s.isEmpty()) {
            return 0;
        }
        String plain = stripAnsi(s);
        int width = 0;
        for (int i = 0; i < plain.length(); i++) {
            char c = plain.charAt(i);
            width += isWide(c) ? 2 : 1;
        }
        return width;
    }

    /** 是否为宽字符（简化的 CJK 判断） */
    public static boolean isWide(char c) {
        return (c >= 0x1100 && c <= 0x115F)      // 韩文字母
                || (c >= 0x2E80 && c <= 0xA4CF)   // CJK 部首/汉字/日文假名
                || (c >= 0xAC00 && c <= 0xD7A3)   // 韩文音节
                || (c >= 0xF900 && c <= 0xFAFF)   // CJK 兼容汉字
                || (c >= 0xFE30 && c <= 0xFE4F)   // CJK 兼容符号
                || (c >= 0xFF00 && c <= 0xFF60)   // 全角字符
                || (c >= 0xFFE0 && c <= 0xFFE6);
    }

    /** 按显示宽度右侧补空格 */
    public static String padRight(String s, int width) {
        int pad = width - displayWidth(s);
        return pad <= 0 ? s : s + repeat(' ', pad);
    }

    /** 按显示宽度左侧补空格 */
    public static String padLeft(String s, int width) {
        int pad = width - displayWidth(s);
        return pad <= 0 ? s : repeat(' ', pad) + s;
    }

    /** 按显示宽度居中 */
    public static String center(String s, int width) {
        int pad = Math.max(0, width - displayWidth(s));
        int left = pad / 2;
        return repeat(' ', left) + s + repeat(' ', pad - left);
    }

    /** 重复字符 */
    public static String repeat(char c, int count) {
        if (count <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * 按显示宽度截断（超长以省略号结尾）。
     * <p>注意：传入文本应为纯文本（含 ANSI 时可能截断控制序列）。</p>
     */
    public static String truncate(String s, int maxWidth) {
        if (s == null) {
            return "";
        }
        if (maxWidth <= 0 || displayWidth(s) <= maxWidth) {
            return s;
        }
        StringBuilder sb = new StringBuilder();
        int width = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int w = isWide(c) ? 2 : 1;
            if (width + w > maxWidth - 1) {
                sb.append('…');
                break;
            }
            sb.append(c);
            width += w;
        }
        return sb.toString();
    }
}

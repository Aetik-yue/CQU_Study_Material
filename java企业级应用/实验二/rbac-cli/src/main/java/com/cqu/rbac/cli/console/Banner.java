package com.cqu.rbac.cli.console;

import java.util.ArrayList;
import java.util.List;

/**
 * 品牌标识：RBAC 字符画 LOGO（figlet "ANSI Shadow" 风格）+ 副标题。
 * <p>LOGO 宽度在运行时按最长行统一补齐，保证逐行等宽、可安全参与渐变与居中。</p>
 */
public final class Banner {

    private Banner() {
    }

    /** 原始字符画（"RBAC"，6 行） */
    private static final String[] RAW_LOGO = {
            "██████╗ ██████╗  █████╗  ██████╗",
            "██╔══██╗██╔══██╗██╔══██╗██╔════╝",
            "██████╔╝██████╔╝███████║██║     ",
            "██╔══██╗██╔══██╗██╔══██║██║     ",
            "██║  ██║██████╔╝██║  ██║╚██████╗",
            "╚═╝  ╚═╝╚═════╝ ╚═╝  ╚═╝ ╚═════╝"
    };

    /** 副标题 */
    private static final String SUBTITLE_CN = "权 限 管 理 系 统";
    /** 英文副标题 */
    private static final String SUBTITLE_EN = "R B A C   ·   P E R M I S S I O N   S Y S T E M";

    /** LOGO 行（已统一补齐到等宽） */
    public static List<String> logoLines() {
        int width = 0;
        for (String line : RAW_LOGO) {
            width = Math.max(width, Theme.displayWidth(line));
        }
        List<String> lines = new ArrayList<>(RAW_LOGO.length);
        for (String line : RAW_LOGO) {
            lines.add(Theme.padRight(line, width));
        }
        return lines;
    }

    /** LOGO 显示宽度 */
    public static int logoWidth() {
        int width = 0;
        for (String line : RAW_LOGO) {
            width = Math.max(width, Theme.displayWidth(line));
        }
        return width;
    }

    public static String subtitleCn() {
        return SUBTITLE_CN;
    }

    public static String subtitleEn() {
        return SUBTITLE_EN;
    }
}

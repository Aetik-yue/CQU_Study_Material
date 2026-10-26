package com.cqu.rbac.cli.menu;

import com.cqu.rbac.cli.console.BackSignal;
import com.cqu.rbac.cli.console.CancelSignal;
import com.cqu.rbac.cli.console.ConsolePrinter;
import com.cqu.rbac.cli.console.ConsoleReader;
import com.cqu.rbac.cli.console.InputClosedException;
import com.cqu.rbac.cli.console.QuitSignal;
import com.cqu.rbac.cli.console.Theme;
import com.cqu.rbac.cli.session.SessionManager;
import com.cqu.rbac.common.constants.Constants;
import com.cqu.rbac.common.exception.BusinessException;
import com.cqu.rbac.common.exception.GlobalExceptionHandler;
import com.cqu.rbac.domain.vo.LoginUser;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * 菜单引擎：负责渲染菜单、读取选项、调度命令、处理返回/退出与异常。
 * <p>视觉结构（自顶向下）：品牌标题面板 → 当前用户行 → 动态副标题（可选）→ 分组菜单（双列）→ 状态栏 → 输入提示。</p>
 * <p>约定：b 返回上级，q 退出整个程序；非法输入提示后重新输入，绝不崩溃；
 * 业务异常统一转为语义色提示（不打印堆栈）。</p>
 */
public class MenuEngine {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    /** 单次菜单调度的结果 */
    private enum Outcome {
        /** 继续留在当前菜单 */
        CONTINUE,
        /** 退出整个程序 */
        QUIT,
        /** 返回上一级菜单（动作请求，或用户输入 b / 0） */
        BACK
    }

    private final ConsolePrinter printer;
    private final ConsoleReader reader;
    private final SessionManager sessionManager;

    /** 面包屑导航（支持多级菜单嵌套） */
    private final Deque<String> breadcrumb = new ArrayDeque<>();

    /** 全局退出标记：置位后所有嵌套的菜单循环逐层退出 */
    private boolean quitProgram = false;

    public MenuEngine(ConsolePrinter printer, ConsoleReader reader, SessionManager sessionManager) {
        this.printer = printer;
        this.reader = reader;
        this.sessionManager = sessionManager;
    }

    /** 是否已请求退出程序 */
    public boolean isQuitProgram() {
        return quitProgram;
    }

    /**
     * 运行一个菜单（阻塞直至返回/退出）。
     */
    public void run(Menu menu) {
        breadcrumb.addLast(menu.getTitle());
        try {
            while (!quitProgram) {
                render(menu);

                String key = readKey();
                if (key == null || key.isEmpty()) {
                    continue;
                }
                if ("b".equalsIgnoreCase(key) || "0".equals(key) && menu.find("0") == null) {
                    return;
                }
                if ("q".equalsIgnoreCase(key)) {
                    quitProgram = true;
                    return;
                }

                MenuItem item = menu.find(key);
                if (item == null) {
                    printer.warn("无效选项 [" + key + "]，请重新输入");
                    reader.pause();
                    continue;
                }

                if (item.isBack()) {
                    return;
                }
                Outcome outcome = dispatch(item);
                if (outcome == Outcome.QUIT) {
                    quitProgram = true;
                    return;
                }
                if (outcome == Outcome.BACK) {
                    return;
                }
                if (!quitProgram) {
                    reader.pause();
                }
            }
        } finally {
            breadcrumb.removeLast();
        }
    }

    /**
     * 执行菜单项。
     */
    private Outcome dispatch(MenuItem item) {
        try {
            if (item.getAction() != null) {
                item.getAction().run();
            }
            if (item.isExit()) {
                return Outcome.QUIT;
            }
        } catch (QuitSignal e) {
            return Outcome.QUIT;
        } catch (BackSignal e) {
            // 动作主动结束当前层级（如删除对象后关闭工作台）
            return Outcome.BACK;
        } catch (CancelSignal e) {
            printer.warn("操作已取消");
        } catch (InputClosedException e) {
            // 输入流结束（脚本结束）：安静退出
            return Outcome.QUIT;
        } catch (BusinessException e) {
            // 认证类异常用红色（权限拒绝），普通业务规则用黄色提示
            if (e instanceof com.cqu.rbac.common.exception.AuthException) {
                printer.error(e.getMessage());
            } else {
                printer.warn(e.getMessage());
            }
        } catch (Exception e) {
            // 未预期异常：记日志 + 友好提示，不打印堆栈
            printer.error(GlobalExceptionHandler.handle(e));
        }
        return Outcome.CONTINUE;
    }

    /** 读取选项编号（捕获 EOF） */
    private String readKey() {
        try {
            return reader.readString(printer.prompt("请输入选项编号："));
        } catch (InputClosedException e) {
            quitProgram = true;
            return null;
        }
    }

    // ---------------- 渲染 ----------------

    /** 渲染菜单：品牌面板 + 用户行 + 动态副标题 + 分组菜单 + 状态栏 */
    private void render(Menu menu) {
        printer.clearScreen();
        printer.panelTop("◆ " + Constants.APP_NAME, String.join(" / ", breadcrumb));

        LoginUser user = sessionManager.getLoginUserOrNull();
        if (user != null) {
            printer.panelLine(userLine(user));
        }

        String subtitle = menu.getSubtitle();
        if (subtitle != null && !subtitle.isEmpty()) {
            printer.panelLine(" " + Theme.truncate(subtitle, printer.contentWidth() - 2) + " ");
        }

        renderItems(menu);
        printer.panelBottom();

        String shortcuts = breadcrumb.size() > 1 ? "b 返回上级 · q 退出程序" : "q 退出程序";
        printer.statusBar("输入编号执行操作", shortcuts);
        printer.blankLine();
    }

    /** 当前用户行（左：在线状态+用户+角色；右：版本+时间） */
    private String userLine(LoginUser user) {
        Theme theme = printer.theme();
        String leftPlain = "● " + user.getUsername() + "（" + user.roleNamesText() + "）";
        String rightPlain = "v" + Constants.APP_VERSION + " · " + LocalTime.now().format(TIME_FORMAT);

        String leftStyled = theme.fg(Theme.SUCCESS, "●") + " "
                + theme.fgBold(Theme.TEXT, user.getUsername())
                + theme.fg(Theme.MUTED, "（" + user.roleNamesText() + "）");
        String rightStyled = theme.fg(Theme.MUTED, rightPlain);

        int pad = printer.contentWidth() - 2
                - Theme.displayWidth(leftPlain) - Theme.displayWidth(rightPlain);
        if (pad < 1) {
            pad = 1;
        }
        return " " + leftStyled + Theme.repeat(' ', pad) + rightStyled + " ";
    }

    /** 渲染菜单项：按分组标题切分；条目较少且宽度允许时使用双列布局 */
    private void renderItems(Menu menu) {
        int maxCell = 0;
        for (MenuItem item : menu.getItems()) {
            if (!item.isSection()) {
                maxCell = Math.max(maxCell, cellWidth(item));
            }
        }
        maxCell += 2; // 两列之间的间距
        boolean twoColumns = maxCell * 2 <= printer.contentWidth() - 2;

        List<MenuItem> buffer = new ArrayList<>();
        for (MenuItem item : menu.getItems()) {
            if (item.isSection()) {
                flushRows(buffer, maxCell, twoColumns);
                buffer.clear();
                printer.panelDivider(item.getTitle());
            } else {
                buffer.add(item);
            }
        }
        flushRows(buffer, maxCell, twoColumns);
    }

    /** 按每行 1/2 个条目输出 */
    private void flushRows(List<MenuItem> items, int cellWidth, boolean twoColumns) {
        int perRow = twoColumns ? 2 : 1;
        for (int i = 0; i < items.size(); i += perRow) {
            StringBuilder content = new StringBuilder(" ");
            for (int col = 0; col < perRow; col++) {
                if (i + col < items.size()) {
                    content.append(cell(items.get(i + col), cellWidth));
                } else {
                    content.append(Theme.repeat(' ', cellWidth));
                }
            }
            printer.panelLine(content.toString());
        }
    }

    /** 单个菜单条目的显示宽度（不含 ANSI） */
    private int cellWidth(MenuItem item) {
        return item.getKey().length() + 2 + Theme.displayWidth(item.getTitle());
    }

    /** 单个菜单条目：序号高亮 + 标题；返回/退出项使用次要色 */
    private String cell(MenuItem item, int width) {
        Theme theme = printer.theme();
        Theme.Color keyColor = (item.isBack() || item.isExit()) ? Theme.MUTED : Theme.PRIMARY;
        Theme.Color titleColor = (item.isBack() || item.isExit()) ? Theme.MUTED : Theme.TEXT;

        String styled = theme.fgBold(keyColor, item.getKey()) + "  " + theme.fg(titleColor, item.getTitle());
        int pad = width - cellWidth(item);
        return pad <= 0 ? styled : styled + Theme.repeat(' ', pad);
    }
}

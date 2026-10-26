package com.cqu.rbac.cli.screens;

import com.cqu.rbac.cli.console.ConsolePrinter;
import com.cqu.rbac.cli.console.ConsoleReader;
import com.cqu.rbac.cli.menu.Menu;
import com.cqu.rbac.cli.session.UiState;
import com.cqu.rbac.common.constants.Constants;
import com.cqu.rbac.domain.entity.AuditLog;
import com.cqu.rbac.support.ServiceLocator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 审计日志屏：多条件分页查询、快捷视图（登录/操作/失败）、重复上次查询。
 * <p>效率特性：模块支持"编号/名称/模糊匹配"；结果支持 s/f/1/0；时间支持预设（今天/近7天/近30天）；
 * 查询条件在会话内记忆，可一键重复。</p>
 */
public class AuditScreen extends AbstractScreen {

    private static final String[] HEADERS = {"ID", "时间", "操作人", "模块", "操作", "对象", "结果", "耗时ms", "失败原因"};

    private static final DateTimeFormatter MINUTE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /** 模块选择表（与审计模块名保持一致） */
    private static final String[] MODULES = {
            Constants.MODULE_AUTH, Constants.MODULE_USER, Constants.MODULE_ROLE,
            Constants.MODULE_PERMISSION, Constants.MODULE_RESOURCE, Constants.MODULE_AUDIT,
            Constants.MODULE_AUTHZ, Constants.MODULE_PROFILE
    };

    public AuditScreen(ConsolePrinter printer, ConsoleReader reader, ServiceLocator locator) {
        super(printer, reader, locator);
    }

    public Menu build() {
        Menu menu = new Menu("审计日志");
        menu.addSection("查 询");
        menu.add("1", "审计日志查询（多条件）", this::queryLogs);
        menu.add("5", "重复上次查询", this::repeatLastQuery);
        menu.addSection("快捷视图");
        menu.add("2", "登录日志", this::listLoginLogs);
        menu.add("3", "操作日志（不含认证）", this::listOperationLogs);
        menu.add("4", "失败日志（最近 20 条）", this::listFailureLogs);
        menu.addBack("0", "返回上级");
        return menu;
    }

    // ---------------- 1. 多条件查询 ----------------

    private void queryLogs() {
        printer.title("审计日志查询（各条件回车表示不限）");
        UiState.AuditFilter last = UiState.getInstance().getLastAuditFilter();
        if (last != null) {
            printer.info("上次条件：" + last.describe() + "（可用菜单 5 一键重复）");
        }

        String module = readModuleFilter();
        String username = reader.readOptional("操作人用户名（模糊匹配，回车不限）：", "");
        String result = readResultFilter();
        LocalDateTime from = readFromTime();
        LocalDateTime to = readToTime();

        UiState.AuditFilter filter = new UiState.AuditFilter(
                module, username.isEmpty() ? null : username.trim(), result, from, to);
        UiState.getInstance().setLastAuditFilter(filter);
        runPagedQuery(filter, "审计日志查询结果");
    }

    /** 模块筛选：支持编号 / 名称 / 任意关键字（模糊匹配） */
    private String readModuleFilter() {
        printer.info("模块对照：1 认证 / 2 用户管理 / 3 角色管理 / 4 权限管理 / 5 资源管理 / 6 审计管理 / 7 权限校验 / 8 个人中心");
        String input = reader.readOptional("模块（输入编号或名称，回车全部）：", "");
        if (input.isEmpty()) {
            return null;
        }
        if (input.length() == 1 && input.charAt(0) >= '1' && input.charAt(0) <= '8') {
            return MODULES[input.charAt(0) - '1'];
        }
        return input.trim();
    }

    /** 结果筛选：s/f/1/0/回车；非法输入明确提示重试 */
    private String readResultFilter() {
        while (true) {
            String input = reader.readOptional("结果（s 成功 / f 失败 / 1 成功 / 0 失败，回车全部）：", "").toUpperCase();
            if (input.isEmpty()) {
                return null;
            }
            if ("S".equals(input) || "1".equals(input)) {
                return "SUCCESS";
            }
            if ("F".equals(input) || "0".equals(input)) {
                return "FAILURE";
            }
            printer.warn("无效输入：请输入 s/f/1/0，或直接回车表示不限");
        }
    }

    /** 起始时间：支持 1/2/3 预设与自定义日期 */
    private LocalDateTime readFromTime() {
        String input = reader.readOptional("起始时间（1 今天 / 2 近7天 / 3 近30天 / yyyy-MM-dd[ HH:mm]，回车不限）：", "");
        if (input.isEmpty()) {
            return null;
        }
        switch (input) {
            case "1":
                return LocalDate.now().atStartOfDay();
            case "2":
                return LocalDate.now().minusDays(6).atStartOfDay();
            case "3":
                return LocalDate.now().minusDays(29).atStartOfDay();
            default:
                return parseDateTime(input, false);
        }
    }

    /** 截止时间：自定义日期；回车不限 */
    private LocalDateTime readToTime() {
        String input = reader.readOptional("截止时间（格式同上，回车不限）：", "");
        if (input.isEmpty()) {
            return null;
        }
        return parseDateTime(input, true);
    }

    /** 一键重复上次查询 */
    private void repeatLastQuery() {
        UiState.AuditFilter last = UiState.getInstance().getLastAuditFilter();
        if (last == null) {
            printer.warn("还没有查询记录，请先使用「1 审计日志查询（多条件）」执行一次查询");
            return;
        }
        printer.title("重复上次查询");
        printer.info("条件：" + last.describe());
        runPagedQuery(last, "最近一次查询结果");
    }

    private void runPagedQuery(UiState.AuditFilter filter, String title) {
        browsePages(title, "日志", HEADERS,
                page -> locator.auditService().page(filter.getModule(), filter.getUsername(),
                        filter.getResult(), filter.getFrom(), filter.getTo(), page, Constants.DEFAULT_PAGE_SIZE),
                this::toRow);
    }

    // ---------------- 快捷视图 ----------------

    private void listLoginLogs() {
        browsePages("登录日志（认证模块）", "日志", HEADERS,
                page -> locator.auditService().listLoginLogs(page, Constants.DEFAULT_PAGE_SIZE),
                this::toRow);
    }

    private void listOperationLogs() {
        browsePages("操作日志（排除认证模块）", "日志", HEADERS,
                page -> locator.auditService().listOperationLogs(page, Constants.DEFAULT_PAGE_SIZE),
                this::toRow);
    }

    /** 失败日志：一屏直出，无需任何过滤输入 */
    private void listFailureLogs() {
        printer.title("失败日志（最近 20 条）");
        int size = 20;
        var result = locator.auditService().page(null, null, "FAILURE", null, null, 1, size);
        if (result.getTotal() == 0) {
            printer.info("暂无失败日志（说明近期操作都很顺利）");
            return;
        }
        List<String[]> rows = result.getRecords().stream().map(this::toRow).collect(java.util.stream.Collectors.toList());
        printer.table(HEADERS, rows);
        printer.info("共 " + result.getTotal() + " 条失败记录，当前显示最近 " + rows.size() + " 条");
        if (result.getTotal() > rows.size()) {
            printer.info("更多记录请用「1 审计日志查询（多条件）」按条件分页查看");
        }
    }

    // ---------------- 时间与行渲染 ----------------

    /** 解析时间：支持 yyyy-MM-dd 与 yyyy-MM-dd HH:mm；endOfDay 决定只给日期时补 00:00 还是 23:59 */
    private LocalDateTime parseDateTime(String text, boolean endOfDay) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            String value = text.trim();
            if (value.length() <= 10) {
                LocalDate date = LocalDate.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                return endOfDay ? date.atTime(23, 59, 59) : date.atStartOfDay();
            }
            if (value.length() <= 16) {
                return LocalDateTime.parse(value, MINUTE_FORMAT);
            }
            return LocalDateTime.parse(value.replace(' ', 'T'));
        } catch (Exception e) {
            printer.warn("时间 [" + text + "] 格式不正确，已忽略该条件");
            return null;
        }
    }

    private String[] toRow(AuditLog log) {
        return new String[]{
                String.valueOf(log.getId()),
                formatTime(log.getCreateTime()),
                log.getUsername() == null ? "-" : log.getUsername(),
                log.getModule() == null ? "-" : log.getModule(),
                log.getOperation() == null ? "-" : log.getOperation(),
                log.getTargetObject() == null ? "-" : log.getTargetObject(),
                log.getResult(),
                log.getDurationMs() == null ? "0" : String.valueOf(log.getDurationMs()),
                log.getErrorMessage() == null ? "-" : log.getErrorMessage()
        };
    }
}

package com.cqu.rbac.cli.screens;

import com.cqu.rbac.cli.console.ConsolePrinter;
import com.cqu.rbac.cli.console.ConsoleReader;
import com.cqu.rbac.cli.console.QuitSignal;
import com.cqu.rbac.cli.console.Theme;
import com.cqu.rbac.common.exception.BusinessException;
import com.cqu.rbac.common.result.PageResult;
import com.cqu.rbac.domain.vo.LoginUser;
import com.cqu.rbac.domain.vo.MenuNode;
import com.cqu.rbac.support.ServiceLocator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * 界面基类（抽象类）：聚合各功能屏共用的输出/输入/服务工厂，
 * 并提供分页浏览、可选列表（对象工作台入口）、字段级重填等通用交互模板。
 */
public abstract class AbstractScreen {

    /** 控制台输出器 */
    protected final ConsolePrinter printer;
    /** 控制台输入器 */
    protected final ConsoleReader reader;
    /** 服务工厂（提供经代理增强的服务） */
    protected final ServiceLocator locator;

    protected AbstractScreen(ConsolePrinter printer, ConsoleReader reader, ServiceLocator locator) {
        this.printer = printer;
        this.reader = reader;
        this.locator = locator;
    }

    // ---------------- 分页浏览（只读列表） ----------------

    /**
     * 分页浏览循环：加载 → 渲染表格 → 翻页指令，直到用户返回。
     *
     * @param title     列表标题
     * @param itemName  条目名称（用于提示文案，如"用户"）
     * @param headers   表头
     * @param loader    页码 → 分页结果
     * @param rowMapper 记录 → 表格行
     */
    protected <T> void browsePages(String title, String itemName, String[] headers,
                                   Function<Integer, PageResult<T>> loader, Function<T, String[]> rowMapper) {
        printer.title(title);
        int page = 1;
        while (true) {
            PageResult<T> result = loader.apply(page);
            List<String[]> rows = new ArrayList<>();
            for (T record : result.getRecords()) {
                rows.add(rowMapper.apply(record));
            }
            printer.table(headers, rows);
            if (result.getTotal() == 0) {
                printer.warn("暂无" + itemName + "数据");
                return;
            }
            printer.info("共 " + result.getTotal() + " 条" + itemName + "，第 " + result.getPage()
                    + "/" + Math.max(1, result.getPages()) + " 页");
            Integer next = readPageCommand(result, itemName);
            if (next == null) {
                return;
            }
            page = next;
        }
    }

    /** 读取翻页指令：返回目标页码；b/回车 → null（返回上级） */
    protected Integer readPageCommand(PageResult<?> current, String itemName) {
        while (true) {
            String cmd = reader.readString("翻页（n 下一页 / p 上一页 / 数字跳页 / b 返回）：");
            if (cmd.isEmpty() || "b".equalsIgnoreCase(cmd)) {
                return null;
            }
            if ("q".equalsIgnoreCase(cmd)) {
                throw new QuitSignal();
            }
            if ("n".equalsIgnoreCase(cmd)) {
                if (current.hasNext()) {
                    return current.getPage() + 1;
                }
                printer.warn("已经是最后一页");
                continue;
            }
            if ("p".equalsIgnoreCase(cmd)) {
                if (current.hasPrevious()) {
                    return current.getPage() - 1;
                }
                printer.warn("已经是第一页");
                continue;
            }
            try {
                int target = Integer.parseInt(cmd);
                if (target >= 1 && target <= Math.max(1, current.getPages())) {
                    return target;
                }
                printer.warn("页码超出范围（1-" + Math.max(1, current.getPages()) + "）");
            } catch (NumberFormatException e) {
                printer.warn("无效指令，请重新输入");
            }
        }
    }

    // ---------------- 可选列表（对象工作台入口） ----------------

    /**
     * 可选列表的选择结果：取消 / 选中某条记录 / 通过 #ID 指定。
     */
    protected static final class Selection<T> {
        private final T item;
        private final Long explicitId;
        private final boolean canceled;

        private Selection(T item, Long explicitId, boolean canceled) {
            this.item = item;
            this.explicitId = explicitId;
            this.canceled = canceled;
        }

        static <T> Selection<T> canceled() {
            return new Selection<>(null, null, true);
        }

        static <T> Selection<T> of(T item) {
            return new Selection<>(item, null, false);
        }

        static <T> Selection<T> byId(Long id) {
            return new Selection<>(null, id, false);
        }

        public boolean isCanceled() {
            return canceled;
        }

        public T getItem() {
            return item;
        }

        public Long getExplicitId() {
            return explicitId;
        }
    }

    /** 列表选择指令：跳页 / 选中本页序号 / 指定 ID */
    private static final class ListCommand {
        private final Integer page;
        private final Integer pick;
        private final Long explicitId;

        private ListCommand(Integer page, Integer pick, Long explicitId) {
            this.page = page;
            this.pick = pick;
            this.explicitId = explicitId;
        }
    }

    /**
     * 分页浏览并允许"选序号进入操作"：表格首列自动加"序号"（当前页 1..N）。
     * <p>指令：序号=选中本页记录；n/p=翻页；g3=跳到第 3 页；#5=按数据库 ID 选中；b/0=返回。</p>
     *
     * @return 选择结果（取消时 {@link Selection#isCanceled()} 为 true）
     */
    protected <T> Selection<T> browseAndSelect(String title, String itemName, String[] headers,
                                               Function<Integer, PageResult<T>> loader,
                                               Function<T, String[]> rowMapper) {
        printer.title(title);
        int page = 1;
        while (true) {
            PageResult<T> result = loader.apply(page);
            List<String[]> rows = new ArrayList<>();
            int index = 1;
            for (T record : result.getRecords()) {
                String[] base = rowMapper.apply(record);
                String[] row = new String[base.length + 1];
                row[0] = String.valueOf(index++);
                System.arraycopy(base, 0, row, 1, base.length);
                rows.add(row);
            }
            String[] fullHeaders = new String[headers.length + 1];
            fullHeaders[0] = "序号";
            System.arraycopy(headers, 0, fullHeaders, 1, headers.length);

            printer.table(fullHeaders, rows);
            if (result.getTotal() == 0) {
                printer.warn("暂无" + itemName + "数据 · 可返回后选择新增，或调整查询条件");
                return Selection.canceled();
            }
            printer.info("共 " + result.getTotal() + " 条" + itemName + "，第 " + result.getPage()
                    + "/" + Math.max(1, result.getPages()) + " 页");

            ListCommand cmd = readSelectCommand(result);
            if (cmd == null) {
                return Selection.canceled();
            }
            if (cmd.pick != null) {
                return Selection.of(result.getRecords().get(cmd.pick - 1));
            }
            if (cmd.explicitId != null) {
                return Selection.byId(cmd.explicitId);
            }
            page = cmd.page;
        }
    }

    /** 读取"选序号/翻页/按 ID"指令；b、0、回车 → null（返回） */
    private ListCommand readSelectCommand(PageResult<?> current) {
        int pageSize = Math.max(1, current.getRecords().size());
        int pages = Math.max(1, current.getPages());
        while (true) {
            String cmd = reader.readString(
                    "输入序号进入操作（n 下一页 / p 上一页 / g3 跳到第3页 / #5 按ID / b 返回）：");
            if (cmd.isEmpty() || "b".equalsIgnoreCase(cmd) || "0".equals(cmd)) {
                return null;
            }
            if ("q".equalsIgnoreCase(cmd)) {
                throw new QuitSignal();
            }
            if ("n".equalsIgnoreCase(cmd)) {
                if (current.hasNext()) {
                    return new ListCommand(current.getPage() + 1, null, null);
                }
                printer.warn("已经是最后一页");
                continue;
            }
            if ("p".equalsIgnoreCase(cmd)) {
                if (current.hasPrevious()) {
                    return new ListCommand(current.getPage() - 1, null, null);
                }
                printer.warn("已经是第一页");
                continue;
            }
            if (cmd.length() > 1 && (cmd.charAt(0) == 'g' || cmd.charAt(0) == 'G')) {
                try {
                    int target = Integer.parseInt(cmd.substring(1));
                    if (target >= 1 && target <= pages) {
                        return new ListCommand(target, null, null);
                    }
                    printer.warn("页码超出范围（1-" + pages + "）");
                } catch (NumberFormatException e) {
                    printer.warn("无效页码，请输入 g3 这样的格式");
                }
                continue;
            }
            if (cmd.startsWith("#")) {
                try {
                    long id = Long.parseLong(cmd.substring(1));
                    if (id > 0) {
                        return new ListCommand(null, null, id);
                    }
                } catch (NumberFormatException ignored) {
                    // fall through
                }
                printer.warn("ID 无效，请输入 #数字");
                continue;
            }
            try {
                int pick = Integer.parseInt(cmd);
                if (pick >= 1 && pick <= pageSize) {
                    return new ListCommand(null, pick, null);
                }
                printer.warn("序号超出范围（1-" + pageSize + "）");
            } catch (NumberFormatException e) {
                printer.warn("无效输入：请输入序号，或 n/p/g页码/#ID/b");
            }
        }
    }

    // ---------------- 表单字段编辑 ----------------

    /**
     * 编辑字段：回车保持原值；输入 "-" 清空；输入 b 取消当前操作（抛 CancelSignal）；输入 q 退出程序。
     */
    protected String readField(String label, String current) {
        String display = (current == null || current.isEmpty()) ? "空" : current;
        String input = reader.readString(label + "（回车保持 [" + display + "]，输入 - 清空，b 取消）：");
        if ("b".equalsIgnoreCase(input)) {
            throw new com.cqu.rbac.cli.console.CancelSignal();
        }
        if ("q".equalsIgnoreCase(input)) {
            throw new QuitSignal();
        }
        if (input.isEmpty()) {
            return current;
        }
        if ("-".equals(input)) {
            return "";
        }
        return input;
    }

    /**
     * 提交并支持"字段级重填"：服务层校验失败且异常携带字段名时，只重读该字段后自动重试，
     * 其余已填内容保留。
     *
     * @param submit 提交动作（可能抛 {@link BusinessException}）
     * @param refill 字段名 → 是否已重新读取该字段（返回 false 表示无法定位，直接上抛异常）
     */
    protected void submitWithRefill(Runnable submit, Function<String, Boolean> refill) {
        while (true) {
            try {
                submit.run();
                return;
            } catch (BusinessException e) {
                String field = e.getField();
                if (field == null) {
                    throw e;
                }
                printer.warn(e.getMessage());
                printer.info("其余已填内容已保留，请只重新输入该项");
                if (!Boolean.TRUE.equals(refill.apply(field))) {
                    throw e;
                }
            }
        }
    }

    /** 操作页快捷键图例（表单类页面顶部使用） */
    protected void printOperationLegend() {
        printer.info("提示：b 取消当前操作 · q 退出程序 · 回车使用默认值");
    }

    /**
     * 生成字段变更摘要：参数按"标签, 旧值, 新值"三元组传入。
     *
     * @return 如 {@code 邮箱 - → a@b.com；状态 启用 → 禁用}；无变化返回空串
     */
    protected String diffText(Object... triples) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i + 2 < triples.length; i += 3) {
            String label = String.valueOf(triples[i]);
            String oldValue = normalizeValue(triples[i + 1]);
            String newValue = normalizeValue(triples[i + 2]);
            if (!oldValue.equals(newValue)) {
                if (sb.length() > 0) {
                    sb.append("；");
                }
                sb.append(label).append(" ").append(oldValue).append(" → ").append(newValue);
            }
        }
        return sb.toString();
    }

    /** 空值统一显示为 -，便于比较与展示 */
    private String normalizeValue(Object value) {
        if (value == null) {
            return "-";
        }
        String text = String.valueOf(value);
        return text.isEmpty() ? "-" : text;
    }

    // ---------------- 时间与身份展示 ----------------

    /** 时间格式化（列表展示用） */
    protected String formatTime(java.time.LocalDateTime time) {
        return time == null ? "-" : time.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /** 打印"我的身份与权限"：基本信息 + 按前缀分组、自动换行的权限清单 */
    protected void printIdentity(LoginUser me) {
        printer.title("我的身份与权限");
        printer.keyValue("用户名 / 姓名", me.getUsername() + " / " + me.realNameOrUsername());
        printer.keyValue("角色", me.roleNamesText() + "（" + me.roleCodesText() + "）");
        printer.keyValue("有效权限数", String.valueOf(me.getPermissions().size()));

        printer.title("我的权限清单（含角色继承）");
        if (me.getPermissions().isEmpty()) {
            printer.warn("当前账号没有任何权限");
            return;
        }
        Map<String, List<String>> groups = new TreeMap<>();
        for (String code : me.getPermissions()) {
            int idx = code.indexOf(':');
            String prefix = idx > 0 ? code.substring(0, idx) : code;
            String suffix = idx > 0 ? code.substring(idx + 1) : code;
            groups.computeIfAbsent(prefix, key -> new ArrayList<>()).add(suffix);
        }
        int prefixWidth = 0;
        for (String prefix : groups.keySet()) {
            prefixWidth = Math.max(prefixWidth, Theme.displayWidth(prefix));
        }
        for (Map.Entry<String, List<String>> entry : groups.entrySet()) {
            printWrappedGroup(entry.getKey(), entry.getValue(), prefixWidth);
        }
    }

    /** 打印一组权限：前缀列对齐 + 后缀按显示宽度自动换行，避免超长行 */
    private void printWrappedGroup(String prefix, List<String> suffixes, int prefixWidth) {
        String paddedPrefix = Theme.padRight(prefix, prefixWidth);
        StringBuilder line = new StringBuilder("   ")
                .append(printer.theme().fgBold(Theme.PRIMARY, paddedPrefix))
                .append("  ");
        int lineWidth = 3 + prefixWidth + 2;
        int maxWidth = 76;

        for (String suffix : suffixes) {
            int suffixWidth = Theme.displayWidth(suffix) + 3;
            if (lineWidth + suffixWidth > maxWidth && lineWidth > 8) {
                printer.line(line.toString());
                line = new StringBuilder("   ").append(Theme.repeat(' ', prefixWidth + 2));
                lineWidth = 3 + prefixWidth + 2;
            }
            line.append(printer.theme().fg(Theme.TEXT, suffix))
                    .append(printer.theme().fg(Theme.MUTED, "   "));
            lineWidth += suffixWidth;
        }
        printer.line(line.toString());
    }

    /** 打印可访问菜单树 */
    protected void printMenuTree(List<MenuNode> menus, String emptyMessage) {
        printer.title("我的可访问菜单（权限树）");
        if (menus.isEmpty()) {
            printer.warn(emptyMessage);
            return;
        }
        for (MenuNode node : menus) {
            printMenuNode(node, 0);
        }
    }

    /** 递归打印菜单树节点 */
    private void printMenuNode(MenuNode node, int depth) {
        Theme theme = printer.theme();
        StringBuilder indent = new StringBuilder("  ");
        for (int i = 0; i < depth; i++) {
            indent.append("    ");
        }
        String marker = depth == 0
                ? theme.fg(Theme.ACCENT, "◆")
                : theme.fg(Theme.BORDER, "└─");
        printer.line(indent + marker + " " + theme.fgBold(Theme.TEXT, node.getName())
                + theme.fg(Theme.MUTED, "  " + node.getCode()));
        for (MenuNode child : node.getChildren()) {
            printMenuNode(child, depth + 1);
        }
    }
}

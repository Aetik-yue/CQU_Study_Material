package com.cqu.rbac.cli.screens;

import com.cqu.rbac.cli.console.BackSignal;
import com.cqu.rbac.cli.console.ConsolePrinter;
import com.cqu.rbac.cli.console.ConsoleReader;
import com.cqu.rbac.cli.menu.Menu;
import com.cqu.rbac.cli.menu.MenuEngine;
import com.cqu.rbac.common.constants.Constants;
import com.cqu.rbac.common.enums.CommonStatus;
import com.cqu.rbac.common.exception.BusinessException;
import com.cqu.rbac.common.result.PageResult;
import com.cqu.rbac.common.util.SelectionExpression;
import com.cqu.rbac.domain.dto.UserDTO;
import com.cqu.rbac.domain.entity.Role;
import com.cqu.rbac.domain.entity.User;
import com.cqu.rbac.domain.vo.LoginUser;
import com.cqu.rbac.domain.vo.UserVO;
import com.cqu.rbac.service.UserService;
import com.cqu.rbac.support.ServiceLocator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用户管理屏：列表（可选对象）→ 用户工作台（详情 + 操作闭环）。
 * <p>工作台内可连续执行"修改资料/分配角色/启停用/重置密码/删除"，无需重复输入 ID；
 * 旧菜单入口（2-7）保留，仍支持直接按 ID 操作（脚本与熟练用户可用）。</p>
 */
public class UserScreen extends AbstractScreen {

    private static final int PAGE_SIZE = Constants.DEFAULT_PAGE_SIZE;

    private static final String[] HEADERS = {"ID", "用户名", "姓名", "邮箱", "电话", "状态", "角色", "创建时间"};

    private final MenuEngine engine;

    public UserScreen(ConsolePrinter printer, ConsoleReader reader, ServiceLocator locator, MenuEngine engine) {
        super(printer, reader, locator);
        this.engine = engine;
    }

    /** 构建用户管理子菜单 */
    public Menu build() {
        Menu menu = new Menu("用户管理");
        menu.addSection("查 询");
        menu.add("1", "用户列表（选序号进入操作）", this::listUsers);
        menu.addSection("维 护");
        menu.add("2", "新增用户", this::createUser);
        menu.add("3", "修改用户（按 ID）", this::updateUser);
        menu.add("4", "删除用户（按 ID）", this::deleteUser);
        menu.addSection("授权与安全");
        menu.add("5", "分配角色（按 ID）", this::assignRoles);
        menu.add("6", "启用/禁用（按 ID）", this::toggleStatus);
        menu.add("7", "重置密码（按 ID）", this::resetPassword);
        menu.addSection("批 量");
        menu.add("8", "批量操作（按序号多选）", this::batchOperations);
        menu.addBack("0", "返回上级");
        return menu;
    }

    // ---------------- 1. 列表 → 工作台 ----------------

    private void listUsers() {
        printer.title("用户列表（分页/条件查询）");
        String keyword = reader.readOptional("关键字（匹配用户名或姓名，回车跳过）：", "");
        Integer status = readStatusFilter();

        UserService service = locator.userService();
        while (true) {
            Selection<UserVO> selection = browseAndSelect("用户列表", "用户", HEADERS,
                    page -> service.page(keyword, status, page, PAGE_SIZE),
                    this::toRow);
            if (selection.isCanceled()) {
                return;
            }
            Long userId = selection.getExplicitId() != null
                    ? selection.getExplicitId() : selection.getItem().getUser().getId();
            workbench(userId);
        }
    }

    /** 读取状态过滤条件：空 → null（全部） */
    private Integer readStatusFilter() {
        String input = reader.readOptional("状态过滤（1 启用 / 0 禁用，回车全部）：", "");
        if ("1".equals(input)) {
            return Constants.STATUS_ENABLED;
        }
        if ("0".equals(input)) {
            return Constants.STATUS_DISABLED;
        }
        return null;
    }

    private String[] toRow(UserVO vo) {
        User u = vo.getUser();
        return new String[]{
                String.valueOf(u.getId()),
                u.getUsername(),
                u.getRealName() == null ? "-" : u.getRealName(),
                u.getEmail() == null ? "-" : u.getEmail(),
                u.getPhone() == null ? "-" : u.getPhone(),
                CommonStatus.textOf(u.getStatus()),
                vo.roleNamesText(),
                formatTime(u.getCreateTime())
        };
    }

    /**
     * 用户工作台：展示对象实时状态，并提供该对象的操作菜单（按权限动态隐藏）。
     * <p>副标题由 {@link Menu#subtitle} 在每次渲染时重新查询，操作后状态立即刷新。</p>
     */
    private void workbench(Long userId) {
        try {
            locator.userService().get(userId);
        } catch (BusinessException e) {
            printer.warn("该用户不存在或已被删除，返回列表");
            return;
        }

        Menu menu = new Menu("用户工作台 · " + usernameOf(userId));
        menu.subtitle(() -> subtitleOf(userId));

        LoginUser me = locator.sessionManager().getLoginUser();
        if (me.hasPermission("user:update")) {
            menu.add("1", "查看/修改资料", () -> doUpdate(userId));
        }
        if (me.hasPermission("user:assign-role")) {
            menu.add("2", "分配角色", () -> doAssignRoles(userId));
        }
        if (me.hasPermission("user:toggle")) {
            menu.add("3", "启用/禁用", () -> doToggleStatus(userId));
        }
        if (me.hasPermission("user:reset-password")) {
            menu.add("4", "重置密码", () -> doResetPassword(userId));
        }
        if (me.hasPermission("user:delete")) {
            menu.add("5", "删除用户", () -> doDelete(userId, true));
        }
        menu.addBack("0", "返回用户列表");
        engine.run(menu);
    }

    /** 工作台副标题：姓名 · 状态 · 角色 · 邮箱 · 失败次数/锁定 */
    private String subtitleOf(Long userId) {
        try {
            UserVO vo = locator.userService().get(userId);
            User u = vo.getUser();
            StringBuilder sb = new StringBuilder();
            sb.append(u.displayName())
                    .append(" · 状态：").append(CommonStatus.textOf(u.getStatus()))
                    .append(" · 角色：").append(vo.roleNamesText())
                    .append(" · 邮箱：").append(u.getEmail() == null ? "-" : u.getEmail())
                    .append(" · 失败：").append(u.getLoginFailCount() == null ? 0 : u.getLoginFailCount()).append(" 次");
            if (u.isLocked()) {
                sb.append(" · 锁定至 ").append(formatTime(u.getLockUntil()));
            }
            return sb.toString();
        } catch (BusinessException e) {
            return "该用户已删除";
        }
    }

    private String usernameOf(Long userId) {
        try {
            return locator.userService().get(userId).getUser().getUsername();
        } catch (BusinessException e) {
            return "id=" + userId;
        }
    }

    // ---------------- 2. 新增 ----------------

    private void createUser() {
        printer.title("新增用户");
        printOperationLegend();

        UserDTO dto = new UserDTO();
        dto.setUsername(askUsername());
        dto.setPassword(askPassword());
        dto.setRealName(reader.readOptionalOperation("姓名（回车跳过，b 取消）：", ""));
        dto.setEmail(reader.readOptionalOperation("邮箱（回车跳过，b 取消）：", ""));
        dto.setPhone(reader.readOptionalOperation("电话（回车跳过，b 取消）：", ""));

        submitWithRefill(() -> locator.userService().create(dto), field -> {
            switch (field) {
                case "username":
                    dto.setUsername(askUsername());
                    return true;
                case "password":
                    dto.setPassword(askPassword());
                    return true;
                case "realName":
                    dto.setRealName(askAgain("姓名"));
                    return true;
                case "email":
                    dto.setEmail(askAgain("邮箱"));
                    return true;
                case "phone":
                    dto.setPhone(askAgain("电话"));
                    return true;
                default:
                    return false;
            }
        });
        printer.success("创建用户 [" + dto.getUsername() + "] 成功");
    }

    /** 校验失败后重填可选字段：回车表示留空（不回填无效值） */
    private String askAgain(String label) {
        return reader.readOptionalOperation(label + "（重新输入，回车=留空，b 取消）：", "");
    }

    private String askUsername() {
        return reader.readNonEmpty("用户名（3-50 位，字母开头，b 取消）：");
    }

    private String askPassword() {
        String password = reader.readPasswordOperation("密码（6-64 位，b 取消）：");
        if (password.isEmpty()) {
            printer.warn("密码不能为空");
            return askPassword();
        }
        return password;
    }

    // ---------------- 3. 修改 ----------------

    /** 旧入口：按 ID 修改 */
    private void updateUser() {
        Long id = reader.readLongRequired("要修改的用户 ID（b 取消）：");
        if (id == null) {
            printer.warn("操作已取消");
            return;
        }
        doUpdate(id);
    }

    /** 修改资料（工作台入口：ID 已确定） */
    private void doUpdate(Long id) {
        UserVO vo = locator.userService().get(id);
        User u = vo.getUser();
        printer.title("修改资料 · " + u.getUsername());
        printOperationLegend();
        showUserDetail(vo);

        UserDTO dto = new UserDTO();
        dto.setId(id);
        dto.setRealName(readField("姓名", u.getRealName()));
        dto.setEmail(readField("邮箱", u.getEmail()));
        dto.setPhone(readField("电话", u.getPhone()));
        dto.setStatus(reader.readOptionalInt("状态（1 启用 / 0 禁用，回车保持）：", u.getStatus()));

        // 保存旧值用于变更摘要
        String oldName = u.getRealName();
        String oldEmail = u.getEmail();
        String oldPhone = u.getPhone();
        Integer oldStatus = u.getStatus();

        submitWithRefill(() -> locator.userService().update(dto), field -> {
            switch (field) {
                case "realName":
                    dto.setRealName(askAgain("姓名"));
                    return true;
                case "email":
                    dto.setEmail(askAgain("邮箱"));
                    return true;
                case "phone":
                    dto.setPhone(askAgain("电话"));
                    return true;
                case "status":
                    dto.setStatus(reader.readOptionalInt("状态（1 启用 / 0 禁用）：", dto.getStatus()));
                    return true;
                default:
                    return false;
            }
        });
        String changes = diffText(
                "姓名", oldName, dto.getRealName(),
                "邮箱", oldEmail, dto.getEmail(),
                "电话", oldPhone, dto.getPhone(),
                "状态", CommonStatus.textOf(oldStatus), CommonStatus.textOf(dto.getStatus()));
        if (changes.isEmpty()) {
            printer.info("用户 [" + u.getUsername() + "] 资料未发生变化");
        } else {
            printer.success("修改用户 [" + u.getUsername() + "] 成功：" + changes);
        }
    }

    // ---------------- 4. 删除 ----------------

    /** 旧入口：按 ID 删除 */
    private void deleteUser() {
        Long id = reader.readLongRequired("要删除的用户 ID（b 取消）：");
        if (id == null) {
            printer.warn("操作已取消");
            return;
        }
        doDelete(id, false);
    }

    /** 删除用户；inWorkbench=true 时删除成功后自动关闭工作台 */
    private void doDelete(Long id, boolean inWorkbench) {
        UserVO vo = locator.userService().get(id);
        printer.title("删除用户");
        showUserDetail(vo);
        if (!reader.confirm("确认删除用户 [" + vo.getUser().getUsername() + "] 吗？")) {
            printer.warn("操作已取消");
            return;
        }
        locator.userService().delete(id);
        printer.success("删除用户 [" + vo.getUser().getUsername() + "] 成功");
        if (inWorkbench) {
            throw new BackSignal();
        }
    }

    // ---------------- 5. 分配角色 ----------------

    /** 旧入口：按 ID 分配角色 */
    private void assignRoles() {
        Long id = reader.readLongRequired("要分配角色的用户 ID（b 取消）：");
        if (id == null) {
            printer.warn("操作已取消");
            return;
        }
        doAssignRoles(id);
    }

    private void doAssignRoles(Long id) {
        UserVO vo = locator.userService().get(id);
        printer.title("分配角色 · " + vo.getUser().getUsername());
        printer.keyValue("当前角色", vo.roleNamesText());

        // 展示全部可选角色
        List<Role> roles = locator.userService().listRoles();
        List<String[]> rows = new ArrayList<>();
        List<Long> universe = new ArrayList<>();
        Map<Long, String> nameById = new LinkedHashMap<>();
        for (Role role : roles) {
            rows.add(new String[]{
                    String.valueOf(role.getId()),
                    role.getRoleName(),
                    role.getRoleCode(),
                    String.valueOf(role.getParentId()),
                    CommonStatus.textOf(role.getStatus())
            });
            universe.add(role.getId());
            nameById.put(role.getId(), role.getRoleName());
        }
        printer.table(new String[]{"角色ID", "角色名", "编码", "父角色ID", "状态"}, rows);
        printer.info("选择语法：" + SelectionExpression.syntaxHelp());

        Set<Long> currentIds = new LinkedHashSet<>(vo.getRoleIds());
        String input = reader.readString("请输入角色选择（如 4 / 3,4 / -3；输入 0 清空；b 取消）：");
        if (input.isEmpty() || "b".equalsIgnoreCase(input)) {
            printer.warn("操作已取消");
            return;
        }

        SelectionExpression.Selection selection;
        try {
            selection = SelectionExpression.evaluate(input, currentIds, universe, null, null);
        } catch (BusinessException e) {
            printer.warn(e.getMessage());
            return;
        }
        if (selection.isUnchanged()) {
            printer.info("角色未发生变化");
            return;
        }
        printer.info("变更预览：新增 " + selection.getAdded().size() + " 个，移除 "
                + selection.getRemoved().size() + " 个，最终 " + selection.getIds().size() + " 个");
        printRoleDelta("+", selection.getAdded(), nameById);
        printRoleDelta("-", selection.getRemoved(), nameById);
        if (!reader.confirm("确认更新用户 [" + vo.getUser().getUsername() + "] 的角色吗？")) {
            printer.warn("操作已取消");
            return;
        }

        locator.userService().assignRoles(id, new ArrayList<>(selection.getIds()));
        printer.success("已为用户 [" + vo.getUser().getUsername() + "] 更新角色，共 "
                + selection.getIds().size() + " 个");
    }

    /** 角色变更明细（最多列 6 项） */
    private void printRoleDelta(String sign, Set<Long> ids, Map<Long, String> nameById) {
        if (ids.isEmpty()) {
            return;
        }
        StringBuilder line = new StringBuilder("   " + sign + " ");
        int shown = 0;
        for (Long id : ids) {
            if (shown >= 6) {
                line.append("… 共 ").append(ids.size()).append(" 个");
                break;
            }
            if (shown > 0) {
                line.append("、");
            }
            line.append(nameById.getOrDefault(id, "id=" + id));
            shown++;
        }
        printer.line(line.toString());
    }

    // ---------------- 6. 启用/禁用 ----------------

    /** 旧入口：按 ID 启停 */
    private void toggleStatus() {
        Long id = reader.readLongRequired("用户 ID（b 取消）：");
        if (id == null) {
            printer.warn("操作已取消");
            return;
        }
        doToggleStatus(id);
    }

    private void doToggleStatus(Long id) {
        UserVO vo = locator.userService().get(id);
        String action = vo.getUser().isEnabled() ? "禁用" : "启用";
        printer.title(action + "用户 · " + vo.getUser().getUsername());
        printer.keyValue("当前状态", CommonStatus.textOf(vo.getUser().getStatus()));
        if (!reader.confirm("确认" + action + "用户 [" + vo.getUser().getUsername() + "] 吗？")) {
            printer.warn("操作已取消");
            return;
        }
        locator.userService().toggleStatus(id);
        printer.success("已" + action + "用户 [" + vo.getUser().getUsername() + "]");
    }

    // ---------------- 7. 重置密码 ----------------

    /** 旧入口：按 ID 重置密码 */
    private void resetPassword() {
        Long id = reader.readLongRequired("用户 ID（b 取消）：");
        if (id == null) {
            printer.warn("操作已取消");
            return;
        }
        doResetPassword(id);
    }

    private void doResetPassword(Long id) {
        UserVO vo = locator.userService().get(id);
        printer.title("重置密码 · " + vo.getUser().getUsername());

        String[] password = {askNewPasswordTwice()};
        submitWithRefill(() -> locator.userService().resetPassword(id, password[0]), field -> {
            if ("newPassword".equals(field)) {
                password[0] = askNewPasswordTwice();
                return true;
            }
            return false;
        });
        printer.success("已重置用户 [" + vo.getUser().getUsername() + "] 的密码（并解除锁定）");
    }

    /** 两次输入新密码，不一致时只重来一次输入 */
    private String askNewPasswordTwice() {
        while (true) {
            String first = reader.readPasswordOperation("新密码（6-64 位，b 取消）：");
            if (first.isEmpty()) {
                printer.warn("密码不能为空");
                continue;
            }
            String second = reader.readPasswordOperation("确认新密码（b 取消）：");
            if (first.equals(second)) {
                return first;
            }
            printer.warn("两次输入的密码不一致，请重新输入");
        }
    }

    // ---------------- 8. 批量操作（按序号多选） ----------------

    private void batchOperations() {
        printer.title("批量操作（按序号多选）");
        String keyword = reader.readOptional("关键字（匹配用户名或姓名，回车跳过）：", "");
        Integer status = readStatusFilter();
        PageResult<UserVO> page = locator.userService().page(keyword, status, 1, Constants.MAX_PAGE_SIZE);
        if (page.getTotal() == 0) {
            printer.warn("没有符合条件的用户 · 可返回后新增，或调整查询条件");
            return;
        }
        if (page.getTotal() > Constants.MAX_PAGE_SIZE) {
            printer.warn("共 " + page.getTotal() + " 条，仅显示前 " + Constants.MAX_PAGE_SIZE
                    + " 条，请先用关键字缩小范围");
        }
        List<UserVO> users = page.getRecords();
        List<String[]> rows = new ArrayList<>();
        int index = 1;
        for (UserVO vo : users) {
            User u = vo.getUser();
            rows.add(new String[]{
                    String.valueOf(index++),
                    String.valueOf(u.getId()),
                    u.getUsername(),
                    u.displayName(),
                    CommonStatus.textOf(u.getStatus()),
                    vo.roleNamesText()
            });
        }
        printer.table(new String[]{"序号", "ID", "用户名", "姓名", "状态", "角色"}, rows);
        printer.info("可选序号 1-" + users.size() + "，支持 1,3-5 / all；输入 0 取消");

        String input = reader.readString("请输入要批量操作的序号：");
        if (input.isEmpty() || "0".equals(input) || "b".equalsIgnoreCase(input)) {
            printer.warn("操作已取消");
            return;
        }

        List<Long> indexUniverse = new ArrayList<>();
        for (int i = 1; i <= users.size(); i++) {
            indexUniverse.add((long) i);
        }
        SelectionExpression.Selection selection;
        try {
            selection = SelectionExpression.evaluate(input, Collections.emptyList(), indexUniverse, null, null);
        } catch (BusinessException e) {
            printer.warn(e.getMessage());
            return;
        }
        List<Long> userIds = new ArrayList<>();
        for (Long idx : selection.getIds()) {
            userIds.add(users.get(idx.intValue() - 1).getUser().getId());
        }
        batchMenu(userIds);
    }

    /** 批量操作工作台 */
    private void batchMenu(List<Long> userIds) {
        Menu menu = new Menu("批量操作 · 已选 " + userIds.size() + " 个用户");
        menu.subtitle(() -> batchSubtitle(userIds));
        menu.addSection("批量执行");
        menu.add("1", "批量启用", () -> doBatchToggle(userIds, true));
        menu.add("2", "批量禁用", () -> doBatchToggle(userIds, false));
        menu.add("3", "批量分配角色", () -> doBatchAssignRoles(userIds));
        menu.add("4", "批量删除", () -> doBatchDelete(userIds));
        menu.addBack("0", "返回用户管理");
        engine.run(menu);
    }

    private String batchSubtitle(List<Long> userIds) {
        List<String> names = new ArrayList<>();
        for (Long id : userIds) {
            try {
                names.add(locator.userService().get(id).getUser().getUsername());
            } catch (BusinessException e) {
                names.add("id=" + id);
            }
        }
        if (names.size() <= 6) {
            return "已选：" + String.join("、", names);
        }
        return "已选：" + String.join("、", names.subList(0, 6)) + " 等 " + names.size() + " 人";
    }

    private void doBatchToggle(List<Long> userIds, boolean enable) {
        String action = enable ? "启用" : "禁用";
        if (!reader.confirm("确认批量" + action + " " + userIds.size() + " 个用户吗？")) {
            printer.warn("操作已取消");
            return;
        }
        List<String> done = new ArrayList<>();
        List<String> skipped = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        for (Long id : userIds) {
            String name = "id=" + id;
            try {
                UserVO vo = locator.userService().get(id);
                name = vo.getUser().getUsername();
                if (vo.getUser().isEnabled() == enable) {
                    skipped.add(name);
                    continue;
                }
                locator.userService().toggleStatus(id);
                done.add(name);
            } catch (BusinessException e) {
                failed.add(name + "：" + e.getMessage());
            }
        }
        printBatchResult(action, done, skipped, failed);
    }

    private void doBatchAssignRoles(List<Long> userIds) {
        printer.title("批量分配角色 · 已选 " + userIds.size() + " 个用户");
        List<Role> roles = locator.userService().listRoles();
        List<String[]> rows = new ArrayList<>();
        List<Long> universe = new ArrayList<>();
        Map<Long, String> nameById = new LinkedHashMap<>();
        for (Role role : roles) {
            rows.add(new String[]{
                    String.valueOf(role.getId()),
                    role.getRoleName(),
                    role.getRoleCode(),
                    CommonStatus.textOf(role.getStatus())
            });
            universe.add(role.getId());
            nameById.put(role.getId(), role.getRoleName());
        }
        printer.table(new String[]{"角色ID", "角色名", "编码", "状态"}, rows);
        printer.info("选择语法：" + SelectionExpression.syntaxHelp());

        String input = reader.readString("请输入要分配的角色（如 4 / 3,4；输入 0 清空；b 取消）：");
        if (input.isEmpty() || "b".equalsIgnoreCase(input)) {
            printer.warn("操作已取消");
            return;
        }
        SelectionExpression.Selection selection;
        try {
            selection = SelectionExpression.evaluate(input, Collections.emptyList(), universe, null, null);
        } catch (BusinessException e) {
            printer.warn(e.getMessage());
            return;
        }
        String display = selection.getIds().isEmpty() ? "（无角色）"
                : selection.getIds().stream().map(id -> nameById.getOrDefault(id, "id=" + id))
                        .reduce((a, b) -> a + "、" + b).orElse("-");
        if (!reader.confirm("确认把 " + userIds.size() + " 个用户的角色设置为 " + display + " 吗？")) {
            printer.warn("操作已取消");
            return;
        }

        List<String> done = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        List<Long> roleIds = new ArrayList<>(selection.getIds());
        for (Long id : userIds) {
            String name = "id=" + id;
            try {
                name = locator.userService().get(id).getUser().getUsername();
                locator.userService().assignRoles(id, roleIds);
                done.add(name);
            } catch (BusinessException e) {
                failed.add(name + "：" + e.getMessage());
            }
        }
        printBatchResult("分配角色", done, Collections.emptyList(), failed);
    }

    private void doBatchDelete(List<Long> userIds) {
        printer.title("批量删除 · 已选 " + userIds.size() + " 个用户");
        List<String> names = new ArrayList<>();
        for (Long id : userIds) {
            try {
                names.add(locator.userService().get(id).getUser().getUsername());
            } catch (BusinessException e) {
                names.add("id=" + id);
            }
        }
        if (!reader.confirm("确认删除 " + userIds.size() + " 个用户（" + String.join("、", names) + "）吗？")) {
            printer.warn("操作已取消");
            return;
        }
        List<String> done = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        for (Long id : userIds) {
            String name = "id=" + id;
            try {
                name = locator.userService().get(id).getUser().getUsername();
                locator.userService().delete(id);
                done.add(name);
            } catch (BusinessException e) {
                failed.add(name + "：" + e.getMessage());
            }
        }
        printBatchResult("删除", done, Collections.emptyList(), failed);
        throw new BackSignal();
    }

    /** 批量结果汇总：成功/跳过/失败 */
    private void printBatchResult(String action, List<String> done, List<String> skipped, List<String> failed) {
        printer.info("批量" + action + "结果：成功 " + done.size() + " 条，跳过 "
                + skipped.size() + " 条，失败 " + failed.size() + " 条");
        if (!done.isEmpty()) {
            printer.success("已" + action + "：" + String.join("、", done));
        }
        for (String failure : failed) {
            printer.warn(failure);
        }
    }

    // ---------------- 辅助 ----------------

    /** 展示用户详情 */
    private void showUserDetail(UserVO vo) {
        User u = vo.getUser();
        printer.keyValue("用户", u.getUsername() + "（" + u.displayName() + "）");
        printer.keyValue("邮箱 / 电话", (u.getEmail() == null ? "-" : u.getEmail())
                + " / " + (u.getPhone() == null ? "-" : u.getPhone()));
        printer.keyValue("状态", CommonStatus.textOf(u.getStatus()));
        printer.keyValue("角色", vo.roleNamesText());
        printer.keyValue("登录失败次数 / 锁定", (u.getLoginFailCount() == null ? 0 : u.getLoginFailCount())
                + " / " + (u.isLocked() ? "锁定至 " + formatTime(u.getLockUntil()) : "未锁定"));
    }
}

package com.cqu.rbac.cli.screens;

import com.cqu.rbac.cli.console.BackSignal;
import com.cqu.rbac.cli.console.ConsolePrinter;
import com.cqu.rbac.cli.console.ConsoleReader;
import com.cqu.rbac.cli.console.BackSignal;
import com.cqu.rbac.cli.console.ConsolePrinter;
import com.cqu.rbac.cli.console.ConsoleReader;
import com.cqu.rbac.cli.console.Theme;
import com.cqu.rbac.cli.menu.Menu;
import com.cqu.rbac.cli.menu.MenuEngine;
import com.cqu.rbac.common.constants.Constants;
import com.cqu.rbac.common.enums.CommonStatus;
import com.cqu.rbac.common.exception.BusinessException;
import com.cqu.rbac.common.util.SelectionExpression;
import com.cqu.rbac.domain.dto.RoleDTO;
import com.cqu.rbac.domain.entity.Permission;
import com.cqu.rbac.domain.entity.Role;
import com.cqu.rbac.domain.vo.LoginUser;
import com.cqu.rbac.domain.vo.PermissionNode;
import com.cqu.rbac.service.RoleService;
import com.cqu.rbac.support.ServiceLocator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 角色管理屏：列表（可选对象）→ 角色工作台（详情 + 操作闭环）。
 * <p>工作台内可连续执行"修改角色/分配权限/查看权限/删除角色"，无需重复输入角色 ID；
 * 旧菜单入口（2-6）保留，仍支持按 ID 操作。</p>
 */
public class RoleScreen extends AbstractScreen {

    private static final int PAGE_SIZE = Constants.DEFAULT_PAGE_SIZE;

    private static final String[] HEADERS = {"ID", "角色名", "编码", "父角色ID", "状态", "描述"};

    private final MenuEngine engine;

    public RoleScreen(ConsolePrinter printer, ConsoleReader reader, ServiceLocator locator, MenuEngine engine) {
        super(printer, reader, locator);
        this.engine = engine;
    }

    public Menu build() {
        Menu menu = new Menu("角色管理");
        menu.addSection("查 询");
        menu.add("1", "角色列表（选序号进入操作）", this::listRoles);
        menu.add("6", "查看角色权限（按 ID）", this::viewPermissions);
        menu.addSection("维 护");
        menu.add("2", "新增角色", this::createRole);
        menu.add("3", "修改角色（按 ID）", this::updateRole);
        menu.add("4", "删除角色（按 ID）", this::deleteRole);
        menu.addSection("授 权");
        menu.add("5", "分配权限（按 ID）", this::assignPermissions);
        menu.addBack("0", "返回上级");
        return menu;
    }

    // ---------------- 1. 列表 → 工作台 ----------------

    private void listRoles() {
        RoleService service = locator.roleService();
        while (true) {
            Selection<Role> selection = browseAndSelect("角色列表", "角色", HEADERS,
                    page -> service.page(page, PAGE_SIZE),
                    this::toRow);
            if (selection.isCanceled()) {
                return;
            }
            Long roleId = selection.getExplicitId() != null
                    ? selection.getExplicitId() : selection.getItem().getId();
            workbench(roleId);
        }
    }

    private String[] toRow(Role role) {
        return new String[]{
                String.valueOf(role.getId()),
                role.getRoleName(),
                role.getRoleCode(),
                String.valueOf(role.getParentId()),
                CommonStatus.textOf(role.getStatus()),
                role.getDescription() == null ? "-" : role.getDescription()
        };
    }

    /** 角色工作台：展示实时状态并提供按权限过滤的操作菜单 */
    private void workbench(Long roleId) {
        try {
            locator.roleService().get(roleId);
        } catch (BusinessException e) {
            printer.warn("该角色不存在或已被删除，返回列表");
            return;
        }

        Menu menu = new Menu("角色工作台 · " + codeOf(roleId));
        menu.subtitle(() -> subtitleOf(roleId));

        LoginUser me = locator.sessionManager().getLoginUser();
        if (me.hasPermission("role:update")) {
            menu.add("1", "修改角色", () -> doUpdate(roleId));
        }
        if (me.hasPermission("role:assign-permission")) {
            menu.add("2", "分配权限", () -> doAssignPermissions(roleId));
        }
        if (me.hasPermission("role:list")) {
            menu.add("3", "查看角色权限", () -> doViewPermissions(roleId));
        }
        if (me.hasPermission("role:delete")) {
            menu.add("4", "删除角色", () -> doDelete(roleId, true));
        }
        menu.addBack("0", "返回角色列表");
        engine.run(menu);
    }

    /** 工作台副标题：角色名 · 编码 · 父角色 · 状态 · 直接权限数 */
    private String subtitleOf(Long roleId) {
        try {
            Role role = locator.roleService().get(roleId);
            int permissionCount = locator.roleService().listPermissions(roleId).size();
            return role.getRoleName() + " · 编码：" + role.getRoleCode()
                    + " · 父角色：" + role.getParentId()
                    + " · 状态：" + CommonStatus.textOf(role.getStatus())
                    + " · 直接权限：" + permissionCount + " 项";
        } catch (BusinessException e) {
            return "该角色已删除";
        }
    }

    private String codeOf(Long roleId) {
        try {
            return locator.roleService().get(roleId).getRoleCode();
        } catch (BusinessException e) {
            return "id=" + roleId;
        }
    }

    // ---------------- 2. 新增 ----------------

    private void createRole() {
        printer.title("新增角色");
        printOperationLegend();

        RoleDTO dto = new RoleDTO();
        dto.setRoleName(reader.readNonEmpty("角色名称（2-50 位，b 取消）："));
        dto.setRoleCode(reader.readNonEmpty("角色编码（大写字母开头，如 DEPT_MANAGER，b 取消）："));
        dto.setParentId(readParentId(dto.getRoleName()));
        dto.setDescription(reader.readOptionalOperation("描述（回车跳过，b 取消）：", ""));

        submitWithRefill(() -> locator.roleService().create(dto), field -> {
            switch (field) {
                case "roleName":
                    dto.setRoleName(reader.readNonEmpty("角色名称（2-50 位）："));
                    return true;
                case "roleCode":
                    dto.setRoleCode(reader.readNonEmpty("角色编码（大写字母开头）："));
                    return true;
                case "parentId":
                    dto.setParentId(readParentId(dto.getRoleName()));
                    return true;
                case "description":
                    dto.setDescription(readField("描述", dto.getDescription()));
                    return true;
                default:
                    return false;
            }
        });
        printer.success("创建角色 [" + dto.getRoleName() + "]（" + dto.getRoleCode() + "）成功");
    }

    private Long readParentId(String roleName) {
        Integer parentId = reader.readOptionalInt("父角色 ID（回车=0 顶级角色，用于角色继承，b 取消）：", 0);
        return parentId == null ? Constants.ROOT_PARENT_ID : parentId.longValue();
    }

    // ---------------- 3. 修改 ----------------

    private void updateRole() {
        Long id = reader.readLongRequired("要修改的角色 ID（b 取消）：");
        if (id == null) {
            printer.warn("操作已取消");
            return;
        }
        doUpdate(id);
    }

    private void doUpdate(Long id) {
        Role role = locator.roleService().get(id);
        printer.title("修改角色 · " + role.getRoleCode());
        printOperationLegend();
        printer.keyValue("角色", role.getRoleName() + "（" + role.getRoleCode() + "）");
        printer.keyValue("父角色 ID", String.valueOf(role.getParentId()));
        printer.keyValue("当前描述", role.getDescription() == null ? "-" : role.getDescription());

        RoleDTO dto = new RoleDTO();
        dto.setId(id);
        dto.setRoleName(readField("角色名称", role.getRoleName()));
        dto.setDescription(readField("描述", role.getDescription()));
        Integer parentId = reader.readOptionalInt("父角色 ID（回车保持，0 表示顶级）：", null);
        if (parentId != null) {
            dto.setParentId(parentId.longValue());
        }
        dto.setStatus(reader.readOptionalInt("状态（1 启用 / 0 禁用，回车保持）：", null));

        submitWithRefill(() -> locator.roleService().update(dto), field -> {
            switch (field) {
                case "roleName":
                    dto.setRoleName(readField("角色名称", dto.getRoleName()));
                    return true;
                case "parentId":
                    dto.setParentId(readParentId(dto.getRoleName()));
                    return true;
                case "description":
                    dto.setDescription(readField("描述", dto.getDescription()));
                    return true;
                default:
                    return false;
            }
        });
        printer.success("修改角色 [" + role.getRoleCode() + "] 成功");
    }

    // ---------------- 4. 删除 ----------------

    private void deleteRole() {
        Long id = reader.readLongRequired("要删除的角色 ID（b 取消）：");
        if (id == null) {
            printer.warn("操作已取消");
            return;
        }
        doDelete(id, false);
    }

    /** 删除角色；inWorkbench=true 时删除成功后自动关闭工作台 */
    private void doDelete(Long id, boolean inWorkbench) {
        Role role = locator.roleService().get(id);
        printer.title("删除角色");
        printer.keyValue("角色", role.getRoleName() + "（" + role.getRoleCode() + "）");
        if (!reader.confirm("确认删除角色 [" + role.getRoleCode() + "] 吗？")) {
            printer.warn("操作已取消");
            return;
        }
        locator.roleService().delete(id);
        printer.success("删除角色 [" + role.getRoleCode() + "] 成功");
        if (inWorkbench) {
            throw new BackSignal();
        }
    }

    // ---------------- 5. 分配权限 ----------------

    private void assignPermissions() {
        Long roleId = reader.readLongRequired("角色 ID（b 取消）：");
        if (roleId == null) {
            printer.warn("操作已取消");
            return;
        }
        doAssignPermissions(roleId);
    }

    private void doAssignPermissions(Long roleId) {
        Role role = locator.roleService().get(roleId);
        printer.title("分配权限 · " + role.getRoleCode());
        printer.keyValue("角色", role.getRoleName() + "（" + role.getRoleCode() + "）");

        // 权限树 + 编码/类型映射（一次查询）
        List<PermissionNode> tree = locator.permissionService().tree(null);
        Map<String, Long> codeToId = new LinkedHashMap<>();
        Map<Long, String> codeById = new LinkedHashMap<>();
        Map<Long, String> typeById = new LinkedHashMap<>();
        List<Long> universe = new ArrayList<>();
        collectPermissions(tree, codeToId, codeById, typeById, universe);

        // 当前权限集合
        Set<Long> currentIds = new LinkedHashSet<>();
        for (Permission permission : locator.roleService().listPermissions(roleId)) {
            currentIds.add(permission.getId());
        }
        printer.keyValue("当前权限数", String.valueOf(currentIds.size()));
        printer.info("选择语法：" + SelectionExpression.syntaxHelp());

        while (true) {
            printer.title("权限树（√ 表示当前已授予）");
            printPermissionTree(tree, currentIds);

            String input = reader.readString("请选择权限（输入 ? 查看语法，b 取消）：");
            if ("b".equalsIgnoreCase(input)) {
                printer.warn("操作已取消");
                return;
            }
            if (input.isEmpty() || "?".equals(input)) {
                printer.info(SelectionExpression.syntaxHelp());
                continue;
            }

            SelectionExpression.Selection selection;
            try {
                selection = SelectionExpression.evaluate(input, currentIds, universe, codeToId, typeById);
            } catch (BusinessException e) {
                printer.warn(e.getMessage());
                continue;
            }
            if (selection.isUnchanged()) {
                printer.info("权限未发生变化");
                continue;
            }
            showPermissionChanges(selection, codeById);
            if (!reader.confirm("确认应用以上变更吗？")) {
                printer.warn("操作已取消");
                return;
            }
            locator.roleService().assignPermissions(roleId, new ArrayList<>(selection.getIds()));
            printer.success("已更新角色 [" + role.getRoleCode() + "] 权限，共 "
                    + selection.getIds().size() + " 项");
            return;
        }
    }

    /** 递归收集权限树上的编码/类型映射与全集 */
    private void collectPermissions(List<PermissionNode> nodes, Map<String, Long> codeToId,
                                    Map<Long, String> codeById, Map<Long, String> typeById,
                                    List<Long> universe) {
        for (PermissionNode node : nodes) {
            Permission permission = node.getPermission();
            codeToId.put(permission.getPermissionCode(), permission.getId());
            codeById.put(permission.getId(), permission.getPermissionCode());
            typeById.put(permission.getId(), permission.getPermissionType());
            universe.add(permission.getId());
            collectPermissions(node.getChildren(), codeToId, codeById, typeById, universe);
        }
    }

    /** 树形展示权限（√ 标记当前已授予） */
    private void printPermissionTree(List<PermissionNode> nodes, Set<Long> granted) {
        for (PermissionNode node : nodes) {
            printPermissionNode(node, 0, granted);
        }
    }

    private void printPermissionNode(PermissionNode node, int depth, Set<Long> granted) {
        Permission permission = node.getPermission();
        Theme theme = printer.theme();
        StringBuilder indent = new StringBuilder("  ");
        for (int i = 0; i < depth; i++) {
            indent.append("   ");
        }
        String marker = depth == 0 ? "◆" : "└";
        String id = Theme.padLeft(String.valueOf(permission.getId()), 2);
        String tick = granted.contains(permission.getId()) ? " " + theme.fg(Theme.SUCCESS, "√") : "";
        printer.line(indent + theme.fg(Theme.BORDER, marker) + " " + theme.fgBold(Theme.PRIMARY, id)
                + "  " + theme.fg(Theme.TEXT, permission.getPermissionCode())
                + theme.fg(Theme.MUTED, "  " + permission.getPermissionName()) + tick);
        for (PermissionNode child : node.getChildren()) {
            printPermissionNode(child, depth + 1, granted);
        }
    }

    /** 变更预览：新增/移除明细（最多列 6 项） */
    private void showPermissionChanges(SelectionExpression.Selection selection, Map<Long, String> codeById) {
        printer.info("变更预览：新增 " + selection.getAdded().size() + " 项，移除 "
                + selection.getRemoved().size() + " 项，最终 " + selection.getIds().size() + " 项");
        printPermissionDelta("+", selection.getAdded(), codeById);
        printPermissionDelta("-", selection.getRemoved(), codeById);
    }

    private void printPermissionDelta(String sign, Set<Long> ids, Map<Long, String> codeById) {
        if (ids.isEmpty()) {
            return;
        }
        StringBuilder line = new StringBuilder("   " + sign + " ");
        int shown = 0;
        for (Long id : ids) {
            if (shown >= 6) {
                line.append("… 共 ").append(ids.size()).append(" 项");
                break;
            }
            if (shown > 0) {
                line.append("  ");
            }
            line.append(id).append(" ").append(codeById.getOrDefault(id, "?"));
            shown++;
        }
        printer.line(line.toString());
    }

    // ---------------- 6. 查看角色权限 ----------------

    private void viewPermissions() {
        Long roleId = reader.readLongRequired("角色 ID（b 取消）：");
        if (roleId == null) {
            return;
        }
        doViewPermissions(roleId);
    }

    private void doViewPermissions(Long roleId) {
        Role role = locator.roleService().get(roleId);
        List<Permission> permissions = locator.roleService().listPermissions(roleId);
        printer.title("角色权限 · " + role.getRoleCode());
        printer.keyValue("角色", role.getRoleName() + "（" + role.getRoleCode() + "）");
        if (permissions.isEmpty()) {
            printer.warn("该角色没有直接配置权限（若配置了父角色，权限将从父角色继承）");
            return;
        }
        List<String[]> rows = new ArrayList<>();
        for (Permission permission : permissions) {
            rows.add(new String[]{
                    String.valueOf(permission.getId()),
                    permission.getPermissionCode(),
                    permission.getPermissionName(),
                    permission.getPermissionType()
            });
        }
        printer.table(new String[]{"ID", "权限编码", "名称", "类型"}, rows);
        printer.info("共 " + permissions.size() + " 项直接权限（不包含继承自父角色的权限）");
    }
}

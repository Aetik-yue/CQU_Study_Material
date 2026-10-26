package com.cqu.rbac.cli.screens;

import com.cqu.rbac.cli.console.ConsolePrinter;
import com.cqu.rbac.cli.console.ConsoleReader;
import com.cqu.rbac.cli.menu.Menu;
import com.cqu.rbac.common.enums.CommonStatus;
import com.cqu.rbac.common.enums.PermissionType;
import com.cqu.rbac.domain.entity.Permission;
import com.cqu.rbac.domain.vo.PermissionNode;
import com.cqu.rbac.support.ServiceLocator;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限管理屏：权限树、按类型查询、新增、修改、删除。
 */
public class PermissionScreen extends AbstractScreen {

    public PermissionScreen(ConsolePrinter printer, ConsoleReader reader, ServiceLocator locator) {
        super(printer, reader, locator);
    }

    public Menu build() {
        Menu menu = new Menu("权限管理");
        menu.addSection("查 询");
        menu.add("1", "权限树（树形展示）", this::showTree);
        menu.add("2", "权限列表（全部）", this::listAll);
        menu.add("3", "按类型查询", this::listByType);
        menu.addSection("维 护");
        menu.add("4", "新增权限", this::createPermission);
        menu.add("5", "修改权限", this::updatePermission);
        menu.add("6", "删除权限", this::deletePermission);
        menu.addBack("0", "返回上级");
        return menu;
    }

    // ---------------- 1. 权限树 ----------------

    private void showTree() {
        printer.title("权限树");
        String typeInput = reader.readOptional("类型过滤（MENU/OPERATION/RESOURCE，回车=全部）：", "");
        String type = typeInput.isEmpty() ? null : typeInput.toUpperCase();

        List<PermissionNode> tree = locator.permissionService().tree(type);
        if (tree.isEmpty()) {
            printer.warn("暂无权限数据");
            return;
        }
        for (PermissionNode node : tree) {
            printNode(node);
        }
        printer.info("说明：括号内依次为 类型/权限编码/ID");
    }

    /** 递归打印权限树节点 */
    private void printNode(PermissionNode node) {
        Permission p = node.getPermission();
        StringBuilder indent = new StringBuilder("  ");
        for (int i = 0; i < node.getLevel(); i++) {
            indent.append("    ");
        }
        String prefix = node.getLevel() == 0 ? "● " : "└─ ";
        printer.line(indent + prefix + p.getPermissionName()
                + "（" + p.getPermissionType() + "/" + p.getPermissionCode() + "/" + p.getId() + "）");
        for (PermissionNode child : node.getChildren()) {
            printNode(child);
        }
    }

    // ---------------- 2. 全部列表 ----------------

    private void listAll() {
        printer.title("权限列表（全部）");
        printTable(locator.permissionService().listAll());
    }

    // ---------------- 3. 按类型查询 ----------------

    private void listByType() {
        printer.title("按类型查询权限");
        String type = reader.readNonEmpty("类型（MENU / OPERATION / RESOURCE，b 取消）：");
        printTable(locator.permissionService().listByType(type));
    }

    private void printTable(List<Permission> permissions) {
        List<String[]> rows = new ArrayList<>();
        for (Permission p : permissions) {
            rows.add(new String[]{
                    String.valueOf(p.getId()),
                    p.getPermissionCode(),
                    p.getPermissionName(),
                    p.getPermissionType(),
                    String.valueOf(p.getParentId()),
                    p.getResourceId() == null ? "-" : String.valueOf(p.getResourceId()),
                    CommonStatus.textOf(p.getStatus())
            });
        }
        printer.table(new String[]{"ID", "权限编码", "名称", "类型", "父权限ID", "资源ID", "状态"}, rows);
        printer.info("共 " + permissions.size() + " 项");
    }

    // ---------------- 4. 新增 ----------------

    private void createPermission() {
        printer.title("新增权限");
        printOperationLegend();
        String name = reader.readNonEmpty("权限名称（2-80 位，b 取消）：");
        String code = reader.readNonEmpty("权限编码（小写字母开头，如 report:export，b 取消）：");
        String type = reader.readNonEmpty("权限类型（MENU / OPERATION / RESOURCE，b 取消）：");
        Integer parentId = reader.readOptionalInt("父权限 ID（回车=0 顶级，b 取消）：", 0);
        Integer resourceId = reader.readOptionalInt("关联资源 ID（回车=不关联，b 取消）：", null);
        String path = reader.readOptionalOperation("菜单路径/标识（回车跳过，b 取消）：", "");
        Integer sortOrder = reader.readOptionalInt("排序号（回车=0，b 取消）：", 0);

        Permission permission = new Permission(name, code, type.toUpperCase());
        if (parentId != null && parentId != 0) {
            permission.setParentId(parentId.longValue());
        }
        if (resourceId != null) {
            permission.setResourceId(resourceId.longValue());
        }
        permission.setPath(path);
        permission.setSortOrder(sortOrder);

        locator.permissionService().create(permission);
        printer.success("创建权限 [" + code + "] 成功");
    }

    // ---------------- 5. 修改 ----------------

    private void updatePermission() {
        printer.title("修改权限（编码不可修改；输入 b 取消）");
        Long id = reader.readLongRequired("要修改的权限 ID：");
        if (id == null) {
            printer.warn("操作已取消");
            return;
        }
        Permission permission = locator.permissionService().get(id);
        printer.keyValue("权限", permission.getPermissionCode() + "（" + permission.getPermissionName() + "）");
        printer.keyValue("类型 / 父权限 / 资源", permission.getPermissionType()
                + " / " + permission.getParentId()
                + " / " + (permission.getResourceId() == null ? "-" : permission.getResourceId()));

        String name = readField("权限名称", permission.getPermissionName());
        Integer parentId = reader.readOptionalInt("父权限 ID（回车保持）：", null);
        Integer resourceId = reader.readOptionalInt("关联资源 ID（回车保持，0 解除关联）：", null);
        String path = readField("菜单路径/标识", permission.getPath());
        Integer sortOrder = reader.readOptionalInt("排序号（回车保持）：", null);
        Integer status = reader.readOptionalInt("状态（1 启用 / 0 禁用，回车保持）：", null);

        permission.setPermissionName(name);
        if (parentId != null) {
            permission.setParentId(parentId.longValue());
        }
        if (resourceId != null) {
            permission.setResourceId(resourceId == 0 ? null : resourceId.longValue());
        }
        permission.setPath(path);
        if (sortOrder != null) {
            permission.setSortOrder(sortOrder);
        }
        if (status != null) {
            permission.setStatus(status);
        }

        locator.permissionService().update(permission);
        printer.success("修改权限 [" + permission.getPermissionCode() + "] 成功");
    }

    // ---------------- 6. 删除 ----------------

    private void deletePermission() {
        printer.title("删除权限（输入 b 取消）");
        Long id = reader.readLongRequired("要删除的权限 ID：");
        if (id == null) {
            printer.warn("操作已取消");
            return;
        }
        Permission permission = locator.permissionService().get(id);
        printer.keyValue("权限", permission.getPermissionCode() + "（" + permission.getPermissionName() + "）");
        if (!reader.confirm("确认删除权限 [" + permission.getPermissionCode() + "] 吗？")) {
            printer.warn("操作已取消");
            return;
        }
        locator.permissionService().delete(id);
        printer.success("删除权限 [" + permission.getPermissionCode() + "] 成功");
    }
}

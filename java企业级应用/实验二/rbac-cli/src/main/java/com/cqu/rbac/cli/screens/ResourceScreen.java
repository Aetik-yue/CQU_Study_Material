package com.cqu.rbac.cli.screens;

import com.cqu.rbac.cli.console.ConsolePrinter;
import com.cqu.rbac.cli.console.ConsoleReader;
import com.cqu.rbac.cli.menu.Menu;
import com.cqu.rbac.common.constants.Constants;
import com.cqu.rbac.common.enums.CommonStatus;
import com.cqu.rbac.domain.entity.Resource;
import com.cqu.rbac.support.ServiceLocator;

import java.util.ArrayList;
import java.util.List;

/**
 * 资源管理屏：资源增删改查 + "某用户能否访问某资源"校验。
 */
public class ResourceScreen extends AbstractScreen {

    private static final String[] HEADERS = {"ID", "资源名称", "资源编码", "类型", "路径/URI", "状态", "描述"};

    public ResourceScreen(ConsolePrinter printer, ConsoleReader reader, ServiceLocator locator) {
        super(printer, reader, locator);
    }

    public Menu build() {
        Menu menu = new Menu("资源管理");
        menu.addSection("查 询");
        menu.add("1", "资源列表", this::listResources);
        menu.add("5", "资源访问校验（某用户能否访问某资源）", this::checkAccess);
        menu.addSection("维 护");
        menu.add("2", "新增资源", this::createResource);
        menu.add("3", "修改资源", this::updateResource);
        menu.add("4", "删除资源", this::deleteResource);
        menu.addBack("0", "返回上级");
        return menu;
    }

    // ---------------- 1. 列表 ----------------

    private void listResources() {
        printer.title("资源列表");
        List<Resource> resources = locator.resourceService().listAll();
        List<String[]> rows = new ArrayList<>();
        for (Resource r : resources) {
            rows.add(toRow(r));
        }
        printer.table(HEADERS, rows);
        printer.info("共 " + resources.size() + " 项资源");
    }

    private String[] toRow(Resource r) {
        return new String[]{
                String.valueOf(r.getId()),
                r.getResourceName(),
                r.getResourceCode(),
                r.getResourceType(),
                r.getResourcePath() == null ? "-" : r.getResourcePath(),
                CommonStatus.textOf(r.getStatus()),
                r.getDescription() == null ? "-" : r.getDescription()
        };
    }

    // ---------------- 2. 新增 ----------------

    private void createResource() {
        printer.title("新增资源");
        printOperationLegend();
        String name = reader.readNonEmpty("资源名称（2-80 位，b 取消）：");
        String code = reader.readNonEmpty("资源编码（如 report:finance，b 取消）：");
        String type = reader.readNonEmpty("资源类型（MENU / FILE / API / DATA，b 取消）：");
        String path = reader.readOptionalOperation("资源路径/URI（回车跳过，b 取消）：", "");
        String description = reader.readOptionalOperation("描述（回车跳过，b 取消）：", "");

        Resource resource = new Resource(name, code, type.toUpperCase());
        resource.setResourcePath(path);
        resource.setDescription(description);

        locator.resourceService().create(resource);
        printer.success("创建资源 [" + code + "] 成功");
    }

    // ---------------- 3. 修改 ----------------

    private void updateResource() {
        printer.title("修改资源（编码不可修改；输入 b 取消）");
        Long id = reader.readLongRequired("要修改的资源 ID：");
        if (id == null) {
            printer.warn("操作已取消");
            return;
        }
        Resource resource = locator.resourceService().get(id);
        printer.keyValue("资源", resource.getResourceName() + "（" + resource.getResourceCode() + "）");

        String name = readField("资源名称", resource.getResourceName());
        String path = readField("资源路径/URI", resource.getResourcePath());
        String description = readField("描述", resource.getDescription());
        Integer status = reader.readOptionalInt("状态（1 启用 / 0 禁用，回车保持）：", null);

        resource.setResourceName(name);
        resource.setResourcePath(path);
        resource.setDescription(description);
        if (status != null) {
            resource.setStatus(status);
        }

        locator.resourceService().update(resource);
        printer.success("修改资源 [" + resource.getResourceCode() + "] 成功");
    }

    // ---------------- 4. 删除 ----------------

    private void deleteResource() {
        printer.title("删除资源（输入 b 取消）");
        Long id = reader.readLongRequired("要删除的资源 ID：");
        if (id == null) {
            printer.warn("操作已取消");
            return;
        }
        Resource resource = locator.resourceService().get(id);
        printer.keyValue("资源", resource.getResourceName() + "（" + resource.getResourceCode() + "）");
        if (!reader.confirm("确认删除资源 [" + resource.getResourceCode() + "] 吗？")) {
            printer.warn("操作已取消");
            return;
        }
        locator.resourceService().delete(id);
        printer.success("删除资源 [" + resource.getResourceCode() + "] 成功");
    }

    // ---------------- 5. 资源访问校验 ----------------

    private void checkAccess() {
        printer.title("资源访问校验（本质：检查用户是否拥有关联该资源的 RESOURCE 权限）");
        Long userId = reader.readLongRequired("用户 ID（b 取消）：");
        if (userId == null) {
            return;
        }
        if (userId == null) {
            return;
        }
        String code = reader.readNonEmpty("资源编码（如 report:finance，b 取消）：");

        boolean allowed = locator.resourceService().checkAccess(userId, code);
        if (allowed) {
            printer.success("校验结果：用户 [id=" + userId + "] 可以访问资源 [" + code + "]");
        } else {
            printer.warn("校验结果：用户 [id=" + userId + "] 不能访问资源 [" + code + "]");
        }
        printer.info("提示：资源可访问性取决于其关联权限是否被授予该用户的角色（含继承）");
    }
}

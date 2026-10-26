package com.cqu.rbac.cli.screens;

import com.cqu.rbac.cli.console.ConsolePrinter;
import com.cqu.rbac.cli.console.ConsoleReader;
import com.cqu.rbac.cli.menu.Menu;
import com.cqu.rbac.common.exception.AuthException;
import com.cqu.rbac.domain.dto.RoleDTO;
import com.cqu.rbac.domain.dto.UserDTO;
import com.cqu.rbac.domain.vo.LoginUser;
import com.cqu.rbac.support.ServiceLocator;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限校验演示屏：展示"动态菜单 + 操作级双重鉴权 + 资源权限"的完整效果。
 * <ul>
 *   <li>查看我的身份/权限/菜单</li>
 *   <li>单个/批量权限校验（AND / OR）</li>
 *   <li>资源访问校验（RESOURCE 权限）</li>
 *   <li>模拟越权调用：动态代理拦截并记录审计</li>
 * </ul>
 */
public class AuthzTestScreen extends AbstractScreen {

    public AuthzTestScreen(ConsolePrinter printer, ConsoleReader reader, ServiceLocator locator) {
        super(printer, reader, locator);
    }

    public Menu build() {
        Menu menu = new Menu("权限校验演示");
        menu.addSection("本账号");
        menu.add("1", "我的身份与权限", this::showMyIdentity);
        menu.add("6", "查看我的动态菜单树", this::showMyMenus);
        menu.addSection("校 验");
        menu.add("2", "校验单个权限码", this::checkSingle);
        menu.add("3", "批量校验（任意满足 / 全部满足）", this::checkBatch);
        menu.add("4", "校验资源访问（某用户能否访问某资源）", this::checkResource);
        menu.addSection("演 示");
        menu.add("5", "模拟越权调用（动态代理拦截演示）", this::simulateDenied);
        menu.addBack("0", "返回上级");
        return menu;
    }

    // ---------------- 1. 我的身份 ----------------

    private void showMyIdentity() {
        printIdentity(currentUser());
    }

    // ---------------- 2. 单个权限校验 ----------------

    private void checkSingle() {
        printer.title("校验单个权限码");
        String code = reader.readNonEmpty("权限码（如 user:create，b 取消）：");
        boolean has = locator.authorizationFacade().hasPermission(currentUser().getUserId(), code);
        if (has) {
            printer.success("当前用户拥有权限 [" + code + "]");
        } else {
            printer.warn("当前用户没有权限 [" + code + "]");
        }
    }

    // ---------------- 3. 批量校验 ----------------

    private void checkBatch() {
        printer.title("批量权限校验（体现 AND / OR 组合逻辑）");
        String input = reader.readNonEmpty("权限码列表（逗号分隔，如 user:list,user:create，b 取消）：");
        List<String> codes = new ArrayList<>();
        for (String code : input.split("[，,\\s]+")) {
            if (!code.isEmpty()) {
                codes.add(code);
            }
        }
        Long userId = currentUser().getUserId();

        boolean any = locator.authorizationFacade().hasAny(userId, codes);
        boolean all = locator.authorizationFacade().hasAll(userId, codes);

        printer.blankLine();
        for (String code : codes) {
            boolean has = locator.authorizationFacade().hasPermission(userId, code);
            printer.line("   " + (has ? "[有]" : "[无]") + " " + code);
        }
        printer.blankLine();
        printer.info("ANY（任意满足，OR 策略）: " + (any ? "满足" : "不满足"));
        printer.info("ALL（全部满足，AND 策略）: " + (all ? "满足" : "不满足"));
    }

    // ---------------- 4. 资源访问校验 ----------------

    private void checkResource() {
        printer.title("资源访问校验（RESOURCE 权限）");
        Integer rawId = reader.readOptionalInt("用户 ID（回车=校验我自己）：", null);
        Long userId = rawId == null ? currentUser().getUserId() : rawId.longValue();
        String code = reader.readNonEmpty("资源编码（如 report:finance，b 取消）：");
        boolean allowed = locator.resourceService().checkAccess(userId, code);
        if (allowed) {
            printer.success("用户 [id=" + userId + "] 可以访问资源 [" + code + "]");
        } else {
            printer.warn("用户 [id=" + userId + "] 不能访问资源 [" + code + "]");
        }
    }

    // ---------------- 5. 模拟越权调用 ----------------

    private void simulateDenied() {
        printer.title("模拟越权调用（动态代理拦截演示）");
        printer.info("说明：仅对『当前用户没有的权限』发起真实服务调用，代理层将在方法执行前拦截");
        printer.info("拦截结果同时写入审计日志（模块=权限校验 或 方法自身模块，结果=FAILURE）");
        printer.blankLine();

        Long userId = currentUser().getUserId();
        // 注意：这里用真实服务方法演示拦截；因先判断无权限才调用，业务方法体不会真正执行，无副作用
        tryInvoke(userId, "user:create", "创建用户", () -> {
            UserDTO dto = new UserDTO();
            dto.setUsername("__denied_probe__");
            dto.setPassword("probe123456");
            locator.userService().create(dto);
        });
        tryInvoke(userId, "role:create", "创建角色", () -> {
            RoleDTO dto = new RoleDTO();
            dto.setRoleName("__denied_probe__");
            dto.setRoleCode("__DENIED_PROBE__");
            locator.roleService().create(dto);
        });
        tryInvoke(userId, "permission:delete", "删除权限", () -> locator.permissionService().delete(1L));
        tryInvoke(userId, "resource:delete", "删除资源", () -> locator.resourceService().delete(1L));
        tryInvoke(userId, "audit:login", "查询登录日志", () -> locator.auditService().listLoginLogs(1, 1));
    }

    /** 仅当"缺权限"时发起调用，验证代理拦截（保证无副作用） */
    private void tryInvoke(Long userId, String permissionCode, String actionName, Runnable call) {
        if (locator.authorizationFacade().hasPermission(userId, permissionCode)) {
            printer.info("你拥有权限 [" + permissionCode + "]，跳过该项演示：" + actionName);
            return;
        }
        printer.line("  尝试执行需要 [" + permissionCode + "] 的操作：" + actionName + " ...");
        try {
            call.run();
            printer.warn("未被拦截（异常情况，请检查代理配置）");
        } catch (AuthException e) {
            printer.success("已被动态代理拦截：" + e.getMessage());
            printer.info("本次拒绝已自动记入审计日志");
        }
    }

    // ---------------- 6. 动态菜单树 ----------------

    private void showMyMenus() {
        printMenuTree(currentUser().getMenus(), "当前用户没有任何可见菜单");
    }

    // ---------------- 辅助 ----------------

    private LoginUser currentUser() {
        return locator.sessionManager().getLoginUser();
    }
}

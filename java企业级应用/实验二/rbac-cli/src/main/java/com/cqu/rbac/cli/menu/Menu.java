package com.cqu.rbac.cli.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 菜单：一组有序列的 {@link MenuItem}（LinkedHashMap 语义，保证展示顺序）。
 * <p>提供链式 API，便于 Screen 以声明式代码构建菜单；支持分组标题（视觉分区）
 * 与动态副标题（如"对象工作台"的实时状态行，每次渲染时重新求值）。</p>
 */
public class Menu {

    private final String title;
    private final List<MenuItem> items = new ArrayList<>();
    /** 副标题（动态求值，可为 null） */
    private Supplier<String> subtitle;

    public Menu(String title) {
        this.title = title;
    }

    /** 添加普通动作项（链式） */
    public Menu add(String key, String title, Runnable action) {
        items.add(new MenuItem(key, title, action));
        return this;
    }

    /** 添加分组标题（链式，仅视觉分组，不可选择） */
    public Menu addSection(String sectionTitle) {
        items.add(MenuItem.section(sectionTitle));
        return this;
    }

    /** 添加"返回上级"项（链式） */
    public Menu addBack(String key, String title) {
        items.add(MenuItem.back(key, title));
        return this;
    }

    /** 添加"退出程序"项（链式，可带退出前动作） */
    public Menu addExit(String key, String title, Runnable action) {
        items.add(MenuItem.exit(key, title, action));
        return this;
    }

    /** 设置动态副标题（每次渲染时重新求值，用于展示对象的实时状态） */
    public Menu subtitle(Supplier<String> subtitleSupplier) {
        this.subtitle = subtitleSupplier;
        return this;
    }

    /** 取当前副标题（无则返回 null） */
    public String getSubtitle() {
        return subtitle == null ? null : subtitle.get();
    }

    /** 按 key 查找菜单项（分组标题不参与） */
    public MenuItem find(String key) {
        for (MenuItem item : items) {
            if (!item.isSection() && item.getKey().equals(key)) {
                return item;
            }
        }
        return null;
    }

    public String getTitle() {
        return title;
    }

    public List<MenuItem> getItems() {
        return items;
    }
}

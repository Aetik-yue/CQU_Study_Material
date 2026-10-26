package com.cqu.rbac.cli.menu;

/**
 * 菜单项（命令模式）。
 * <p>设计模式：命令 Command——把"按键 → 动作"封装成对象；
 * 动作用 {@link Runnable}（常用 Lambda / 方法引用实现）。</p>
 * <p>额外支持"分组标题"（{@link Kind#SECTION}）：仅用于视觉分组，不参与选择。</p>
 */
public class MenuItem {

    /** 菜单项类型 */
    public enum Kind {
        /** 普通动作 */
        ACTION,
        /** 返回上级 */
        BACK,
        /** 退出程序（先执行动作，如注销，再结束） */
        EXIT,
        /** 分组标题（不可选择） */
        SECTION
    }

    private final String key;
    private final String title;
    private final Runnable action;
    private final Kind kind;

    public MenuItem(String key, String title, Runnable action) {
        this(key, title, action, Kind.ACTION);
    }

    private MenuItem(String key, String title, Runnable action, Kind kind) {
        this.key = key;
        this.title = title;
        this.action = action;
        this.kind = kind;
    }

    /** 构建"返回上级"项（由菜单引擎直接返回，无需动作） */
    public static MenuItem back(String key, String title) {
        return new MenuItem(key, title, null, Kind.BACK);
    }

    /** 构建"退出程序"项（可选执行退出前动作，如注销） */
    public static MenuItem exit(String key, String title, Runnable action) {
        return new MenuItem(key, title, action, Kind.EXIT);
    }

    /** 构建"分组标题"（不可选择，仅渲染为分隔线） */
    public static MenuItem section(String title) {
        return new MenuItem(null, title, null, Kind.SECTION);
    }

    public String getKey() {
        return key;
    }

    public String getTitle() {
        return title;
    }

    public Runnable getAction() {
        return action;
    }

    public boolean isBack() {
        return kind == Kind.BACK;
    }

    public boolean isExit() {
        return kind == Kind.EXIT;
    }

    public boolean isSection() {
        return kind == Kind.SECTION;
    }

    public Kind getKind() {
        return kind;
    }
}

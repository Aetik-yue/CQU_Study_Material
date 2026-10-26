package com.atm.ui;

import com.atm.hardware.Display;
import com.atm.hardware.Keypad;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用菜单：把“显示选项、读取选择、非法重试”这套重复逻辑做成一个泛型组件。
 *
 * <p>选项的“值”由类型参数 T 决定：主菜单用 {@link MainMenuChoice}，
 * 取款菜单用金额档位——菜单只管收集用户选择，返回值交给业务层，
 * 这正是第 5 章泛型的价值：一份代码适配所有选择类型。</p>
 *
 * @param <T> 每个选项对应的值类型
 */
public final class Menu<T> {

    /** 一个菜单项：显示文案 + 对应的值。 */
    public record Item<T>(String label, T value) {
    }

    private final String title;
    private final List<Item<T>> items = new ArrayList<>();
    private final Display display;
    private final Keypad keypad;

    public Menu(String title, Display display, Keypad keypad) {
        this.title = title;
        this.display = display;
        this.keypad = keypad;
    }

    /** 追加一个菜单项，返回自身以便链式添加。 */
    public Menu<T> add(String label, T value) {
        items.add(new Item<>(label, value));
        return this;
    }

    /**
     * 显示菜单并等待用户选择。
     *
     * <p>选项编号从 1 开始；数字越界时提示并重新选择。</p>
     *
     * @return 被选中项的值
     */
    public T select() {
        display.showMessageLine("");
        display.showMessageLine("【" + title + "】");
        for (int i = 0; i < items.size(); i++) {
            display.showMessageLine((i + 1) + " - " + items.get(i).label());
        }
        while (true) {
            int choice = keypad.readInt("请输入您的选择：");
            if (choice >= 1 && choice <= items.size()) {
                return items.get(choice - 1).value();
            }
            display.showMessageLine("输入无效，请重新选择（1-" + items.size() + "）。");
        }
    }
}

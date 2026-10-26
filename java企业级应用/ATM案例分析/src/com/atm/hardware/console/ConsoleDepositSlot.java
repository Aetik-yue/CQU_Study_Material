package com.atm.hardware.console;

import com.atm.hardware.DepositSlot;
import com.atm.hardware.Display;
import com.atm.hardware.Keypad;

/**
 * 存款槽的控制台实现：用一次问答模拟“信封是否投入”。
 */
public class ConsoleDepositSlot implements DepositSlot {

    private final Display display;
    private final Keypad keypad;

    public ConsoleDepositSlot(Display display, Keypad keypad) {
        this.display = display;
        this.keypad = keypad;
    }

    @Override
    public boolean isEnvelopeReceived() {
        display.showMessageLine("请将存款信封投入存款槽。");
        while (true) {
            int answer = keypad.readInt("（模拟存款槽）是否已投入信封？（1-是，0-否）：");
            if (answer == 1) {
                display.showMessageLine("（存款槽）已收到您的信封。");
                return true;
            }
            if (answer == 0) {
                display.showMessageLine("（存款槽）未收到信封。");
                return false;
            }
            display.showMessageLine("请输入 1 或 0。");
        }
    }
}

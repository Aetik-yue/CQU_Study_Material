package com.atm.hardware.console;

import com.atm.hardware.Display;

/**
 * 屏幕的控制台实现：直接打印到标准输出。
 */
public class ConsoleDisplay implements Display {

    @Override
    public void showMessage(String message) {
        System.out.print(message);
    }

    @Override
    public void showMessageLine(String message) {
        System.out.println(message);
    }

    @Override
    public void showAmountLine(String label, double amount) {
        System.out.println(String.format("%s：￥%,.2f", label, amount));
    }
}

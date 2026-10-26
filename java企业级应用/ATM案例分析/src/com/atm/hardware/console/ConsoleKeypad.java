package com.atm.hardware.console;

import com.atm.hardware.Display;
import com.atm.hardware.Keypad;

import java.util.Scanner;

/**
 * 小键盘的控制台实现：从标准输入读取。
 *
 * <p>输入完成后把内容回显到屏幕，保证“重定向输入”跑出的会话记录
 * 与人工操作时终端里看到的画面一致。</p>
 */
public class ConsoleKeypad implements Keypad {

    private final Display display;
    private final Scanner scanner = new Scanner(System.in);

    public ConsoleKeypad(Display display) {
        this.display = display;
    }

    @Override
    public String readLine(String prompt) {
        display.showMessage(prompt);
        if (!scanner.hasNextLine()) {
            // 输入流结束（如脚本输入耗尽）：安全退出，避免异常堆栈
            display.showMessageLine("");
            display.showMessageLine("输入流已结束，程序退出。");
            System.exit(0);
            return "";
        }
        String line = scanner.nextLine().trim();
        display.showMessageLine(line);
        return line;
    }

    @Override
    public int readInt(String prompt) {
        while (true) {
            String line = readLine(prompt);
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                display.showMessageLine("输入无效，请输入数字。");
            }
        }
    }

    @Override
    public double readAmount(String prompt) {
        while (true) {
            String line = readLine(prompt);
            try {
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                display.showMessageLine("输入无效，请输入金额数字。");
            }
        }
    }
}

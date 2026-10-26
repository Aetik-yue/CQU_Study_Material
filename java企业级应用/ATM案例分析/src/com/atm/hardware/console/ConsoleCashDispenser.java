package com.atm.hardware.console;

import com.atm.hardware.CashDispenser;
import com.atm.hardware.Display;

/**
 * 出钞机的控制台实现：用内存里的张数模拟钞箱。
 */
public class ConsoleCashDispenser implements CashDispenser {

    /** 初始钞箱：100 张 100 元，共 10000 元。 */
    private static final int INITIAL_BILL_COUNT = 100;

    private int billCount = INITIAL_BILL_COUNT;
    private final Display display;

    public ConsoleCashDispenser(Display display) {
        this.display = display;
    }

    @Override
    public boolean isSufficientCashAvailable(double amount) {
        return billCount >= amount / BILL_VALUE;
    }

    @Override
    public void dispenseCash(double amount) {
        int bills = (int) (amount / BILL_VALUE);
        billCount -= bills;
        display.showMessageLine(String.format(
                "（出钞口）请取走您的现金：￥%,.2f，共 %d 张 100 元。", amount, bills));
    }
}

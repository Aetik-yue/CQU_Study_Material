package com.atm;

import com.atm.bank.BankDatabase;
import com.atm.hardware.CardReader;
import com.atm.hardware.CashDispenser;
import com.atm.hardware.DepositSlot;
import com.atm.hardware.Display;
import com.atm.hardware.Keypad;
import com.atm.hardware.ReceiptPrinter;
import com.atm.hardware.console.ConsoleCardReader;
import com.atm.hardware.console.ConsoleCashDispenser;
import com.atm.hardware.console.ConsoleDepositSlot;
import com.atm.hardware.console.ConsoleDisplay;
import com.atm.hardware.console.ConsoleKeypad;
import com.atm.hardware.console.ConsoleReceiptPrinter;

/**
 * 程序入口：负责“装配”整套系统。
 *
 * <p>所有依赖在这里一次性组装：具体用哪套硬件实现（本工程为控制台版）、
 * 用哪个银行数据库（单例），都由入口决定；ATM 与交易类只认接口。
 * 将来换成图形界面版本，只需替换这里的六个 Console 实现。</p>
 */
public final class AtmApplication {

    private AtmApplication() {
    }

    public static void main(String[] args) {
        BankDatabase bankDatabase = BankDatabase.getInstance();

        // 装配六个硬件部件（控制台实现）
        Display display = new ConsoleDisplay();
        Keypad keypad = new ConsoleKeypad(display);
        CardReader cardReader = new ConsoleCardReader(display, keypad, bankDatabase::findCard);
        CashDispenser cashDispenser = new ConsoleCashDispenser(display);
        DepositSlot depositSlot = new ConsoleDepositSlot(display, keypad);
        ReceiptPrinter receiptPrinter = new ConsoleReceiptPrinter(display);

        // 启动会话
        new ATM(bankDatabase, display, keypad, cardReader, cashDispenser,
                depositSlot, receiptPrinter).run();
    }
}

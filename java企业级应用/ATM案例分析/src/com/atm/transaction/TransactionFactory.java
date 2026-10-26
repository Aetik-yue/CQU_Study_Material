package com.atm.transaction;

import com.atm.bank.BankDatabase;
import com.atm.hardware.CashDispenser;
import com.atm.hardware.DepositSlot;
import com.atm.hardware.Display;
import com.atm.hardware.Keypad;
import com.atm.hardware.ReceiptPrinter;
import com.atm.ui.MainMenuChoice;

/**
 * 交易工厂：根据用户在主菜单的选择，装配出对应的交易对象。
 *
 * <p>会话层拿到的是 {@code Transaction} 引用，不关心具体子类；
 * 菜单新增一项、交易新增一族，都只改工厂，不改会话层的执行流程。
 * switch 表达式对枚举穷尽列出，编译器保证不漏分支。</p>
 */
public final class TransactionFactory {

    private final BankDatabase bankDatabase;
    private final Display display;
    private final Keypad keypad;
    private final CashDispenser cashDispenser;
    private final DepositSlot depositSlot;
    private final ReceiptPrinter receiptPrinter;

    public TransactionFactory(BankDatabase bankDatabase, Display display, Keypad keypad,
                              CashDispenser cashDispenser, DepositSlot depositSlot,
                              ReceiptPrinter receiptPrinter) {
        this.bankDatabase = bankDatabase;
        this.display = display;
        this.keypad = keypad;
        this.cashDispenser = cashDispenser;
        this.depositSlot = depositSlot;
        this.receiptPrinter = receiptPrinter;
    }

    /**
     * 创建一笔交易。
     *
     * @param choice        主菜单选项
     * @param accountNumber 当前认证通过的账户
     */
    public Transaction create(MainMenuChoice choice, int accountNumber) {
        return switch (choice) {
            case BALANCE_INQUIRY ->
                    new BalanceInquiry(accountNumber, bankDatabase, display, receiptPrinter);
            case HISTORY ->
                    new HistoryInquiry(accountNumber, bankDatabase, display, receiptPrinter);
            case WITHDRAWAL ->
                    new Withdrawal(accountNumber, bankDatabase, display, receiptPrinter,
                            keypad, cashDispenser);
            case DEPOSIT ->
                    new Deposit(accountNumber, bankDatabase, display, receiptPrinter,
                            keypad, depositSlot);
            case TRANSFER ->
                    new Transfer(accountNumber, bankDatabase, display, receiptPrinter, keypad);
            case EXIT ->
                    throw new IllegalArgumentException("退出不是一种交易，不应交给工厂创建。");
        };
    }
}

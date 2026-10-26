package com.atm;

import com.atm.bank.AuthResult;
import com.atm.bank.BankCard;
import com.atm.bank.BankDatabase;
import com.atm.hardware.CardReader;
import com.atm.hardware.CashDispenser;
import com.atm.hardware.DepositSlot;
import com.atm.hardware.Display;
import com.atm.hardware.Keypad;
import com.atm.hardware.ReceiptPrinter;
import com.atm.transaction.Transaction;
import com.atm.transaction.TransactionFactory;
import com.atm.ui.MainMenuChoice;
import com.atm.ui.Menu;

/**
 * ATM 会话主控：组织“插卡 → 认证 → 循环办理交易 → 退卡”的完整流程。
 *
 * <p>职责边界：ATM 只负责<b>流程</b>与<b>会话状态</b>（当前是哪张卡、认证通过没有），
 * 具体交易规则交给交易子类，数据交给银行数据库，输入输出交给硬件接口——
 * 任何一个类都不越界，这是单一职责原则的落地。</p>
 */
public class ATM {

    private final BankDatabase bankDatabase;
    private final Display display;
    private final Keypad keypad;
    private final CardReader cardReader;
    private final CashDispenser cashDispenser;
    private final DepositSlot depositSlot;
    private final ReceiptPrinter receiptPrinter;
    private final TransactionFactory transactionFactory;
    private final Menu<MainMenuChoice> mainMenu;

    public ATM(BankDatabase bankDatabase, Display display, Keypad keypad,
               CardReader cardReader, CashDispenser cashDispenser,
               DepositSlot depositSlot, ReceiptPrinter receiptPrinter) {
        this.bankDatabase = bankDatabase;
        this.display = display;
        this.keypad = keypad;
        this.cardReader = cardReader;
        this.cashDispenser = cashDispenser;
        this.depositSlot = depositSlot;
        this.receiptPrinter = receiptPrinter;
        this.transactionFactory = new TransactionFactory(bankDatabase, display, keypad,
                cashDispenser, depositSlot, receiptPrinter);
        this.mainMenu = new Menu<MainMenuChoice>("主菜单", display, keypad)
                .add(MainMenuChoice.BALANCE_INQUIRY.getLabel(), MainMenuChoice.BALANCE_INQUIRY)
                .add(MainMenuChoice.HISTORY.getLabel(), MainMenuChoice.HISTORY)
                .add(MainMenuChoice.WITHDRAWAL.getLabel(), MainMenuChoice.WITHDRAWAL)
                .add(MainMenuChoice.DEPOSIT.getLabel(), MainMenuChoice.DEPOSIT)
                .add(MainMenuChoice.TRANSFER.getLabel(), MainMenuChoice.TRANSFER)
                .add(MainMenuChoice.EXIT.getLabel(), MainMenuChoice.EXIT);
    }

    /** 启动 ATM 主循环：不断接受新卡片，直到用户选择不再插卡。 */
    public void run() {
        display.showMessageLine("========================================");
        display.showMessageLine("   欢迎使用 Java 银行自动柜员机（ATM）");
        display.showMessageLine("========================================");

        while (true) {
            BankCard card = cardReader.readCard();
            if (card == null) {
                break; // 用户放弃插卡
            }
            if (authenticate(card)) {
                performTransactions(card);
            }
            // 认证失败且未吞卡（正常不会发生：错满 3 次即吞卡）时回到欢迎界面
        }

        display.showMessageLine("");
        display.showMessageLine("感谢使用，再见！");
    }

    /**
     * 密码认证：最多允许 {@link BankDatabase#MAX_PIN_ATTEMPTS} 次输入机会。
     *
     * @return 认证是否通过
     */
    private boolean authenticate(BankCard card) {
        int accountNumber = card.getAccountNumber();
        while (true) {
            String pin = keypad.readLine("请输入密码：");
            AuthResult result = bankDatabase.authenticate(accountNumber, pin);
            switch (result) {
                case SUCCESS -> {
                    display.showMessageLine("登录成功！");
                    return true;
                }
                case WRONG_PIN -> {
                    int remaining = bankDatabase.getRemainingPinAttempts(accountNumber);
                    display.showMessageLine("密码错误，您还有 " + remaining + " 次机会。");
                }
                case CARD_RETAINED -> {
                    card.retain();
                    cardReader.retainCard();
                    display.showMessageLine("密码连续输错次数过多，银行卡已被吞卡，请联系银行网点处理。");
                    return false;
                }
            }
        }
    }

    /** 认证通过后的交易主循环：反复显示主菜单，直到用户选择退出。 */
    private void performTransactions(BankCard card) {
        int accountNumber = card.getAccountNumber();
        display.showMessageLine(String.format("尊敬的 %s，欢迎您！",
                bankDatabase.findAccount(accountNumber).getOwnerName()));

        while (true) {
            MainMenuChoice choice = mainMenu.select();
            if (choice == MainMenuChoice.EXIT) {
                cardReader.ejectCard();
                return;
            }
            // 多态：声明类型是父类 Transaction，实际执行哪个子类的方法由运行时对象决定
            Transaction transaction = transactionFactory.create(choice, accountNumber);
            transaction.execute();
            // 成功、失败、取消都回到主菜单，由用户决定下一步
        }
    }
}

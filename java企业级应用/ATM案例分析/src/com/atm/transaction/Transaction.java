package com.atm.transaction;

import com.atm.bank.BankDatabase;
import com.atm.bank.TransactionRecord;
import com.atm.hardware.Display;
import com.atm.hardware.ReceiptPrinter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 交易抽象父类（sealed + 模板方法）。
 *
 * <p><b>继承与多态的核心：</b></p>
 * <ul>
 *   <li>四类交易共有的数据（账号、数据库、屏幕、凭条打印机）上提到本类；</li>
 *   <li>{@link #execute()} 是 <b>final 模板方法</b>：固定“执行规则 → 显示结果 → 记流水 → 打凭条”
 *       这一骨架，任何子类都无法更改；</li>
 *   <li>{@link #perform()} 是抽象方法：每类交易各自的业务规则，是子类的唯一必答项；</li>
 *   <li>{@link #buildRecords(TransactionResult)} 是钩子方法：默认记一条流水，转账子类可扩展为两条；</li>
 *   <li>sealed 限定只有四个交易的子类可以直接继承本类——交易家族是封闭的，
 *       将来新增交易必须显式写进 permits，编译器帮你把关。</li>
 * </ul>
 *
 * <p>ATM 会话层只持有 {@code Transaction} 引用调用 {@link #execute()}，
 * 运行时由实际对象决定执行哪段规则——这就是动态绑定。</p>
 */
public sealed abstract class Transaction
        permits BalanceInquiry, Deposit, HistoryInquiry, Transfer, Withdrawal {

    private final int accountNumber;
    private final BankDatabase bankDatabase;
    private final Display display;
    private final ReceiptPrinter receiptPrinter;

    protected Transaction(int accountNumber, BankDatabase bankDatabase,
                          Display display, ReceiptPrinter receiptPrinter) {
        this.accountNumber = accountNumber;
        this.bankDatabase = bankDatabase;
        this.display = display;
        this.receiptPrinter = receiptPrinter;
    }

    /**
     * 执行本次交易（模板方法，final：骨架不允许子类覆盖）。
     *
     * @return 交易结果，供会话层决定后续流程
     */
    public final TransactionResult execute() {
        display.showMessageLine("");
        display.showMessageLine("—— " + getTypeName() + " ——");

        TransactionResult result = perform();

        // 模板固定的一步：按结果展示文字
        if (result.isSuccess()) {
            display.showMessageLine(result.message());
        } else if (result.isCancelled()) {
            display.showMessageLine("已取消本次" + getTypeName() + "。");
        } else {
            display.showMessageLine("交易未完成：" + result.message());
        }

        // 模板固定的一步：记流水（钩子可扩展）＋成功时打凭条
        List<TransactionRecord> records = buildRecords(result);
        for (TransactionRecord record : records) {
            bankDatabase.getTransactionLog().append(record);
        }
        if (result.isSuccess()) {
            receiptPrinter.printReceipt(records.get(0));
        }
        return result;
    }

    /**
     * 子类实现的交易规则：这是本类留给子类的唯一必答项。
     */
    protected abstract TransactionResult perform();

    /** 交易类型名，用于菜单、流水与凭条的显示。 */
    public abstract String getTypeName();

    /**
     * 钩子方法：生成要记入流水的记录，默认一条（本账户）。
     * 转账子类覆盖为两条（转出方 + 转入方）。
     */
    protected List<TransactionRecord> buildRecords(TransactionResult result) {
        double balanceAfter = result.isSuccess()
                ? bankDatabase.getTotalBalance(accountNumber)
                : Double.NaN;
        TransactionRecord record = new TransactionRecord(
                LocalDateTime.now(), accountNumber, getTypeName(),
                result.amount(), balanceAfter, result.status().getLabel());
        return List.of(record);
    }

    // ------------------------------------------------------------------
    // 子类可用的受保护访问器
    // ------------------------------------------------------------------

    protected int getAccountNumber() {
        return accountNumber;
    }

    protected BankDatabase getBankDatabase() {
        return bankDatabase;
    }

    protected Display getDisplay() {
        return display;
    }
}

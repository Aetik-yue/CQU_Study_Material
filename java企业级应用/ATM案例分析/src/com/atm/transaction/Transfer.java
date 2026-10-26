package com.atm.transaction;

import com.atm.bank.Account;
import com.atm.bank.BankDatabase;
import com.atm.bank.TransactionRecord;
import com.atm.hardware.Display;
import com.atm.hardware.Keypad;
import com.atm.hardware.ReceiptPrinter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 转账：向本行另一个账户转入资金（相对教材版新增的交易类型）。
 *
 * <p>它同时演示了两点：</p>
 * <ul>
 *   <li>新增交易只需新增一个子类并实现 {@link #perform()}，
 *       会话层调用 {@code execute()} 的那行代码一个字都不用改；</li>
 *   <li>覆写钩子方法 {@link #buildRecords(TransactionResult)}，
 *       让一笔转账在转出、转入两个账户下各留一条流水。</li>
 * </ul>
 */
public final class Transfer extends Transaction {

    private final Keypad keypad;
    /** 本次转账的目标账号，由 perform() 记录，供钩子方法复用。 */
    private int targetAccountNumber = -1;

    public Transfer(int accountNumber, BankDatabase bankDatabase,
                    Display display, ReceiptPrinter receiptPrinter, Keypad keypad) {
        super(accountNumber, bankDatabase, display, receiptPrinter);
        this.keypad = keypad;
    }

    @Override
    public String getTypeName() {
        return "转账";
    }

    @Override
    protected TransactionResult perform() {
        // 输入并校验目标账号
        int target = keypad.readInt("请输入转入账号（输入 0 取消交易）：");
        if (target == 0) {
            return TransactionResult.cancelled();
        }
        if (target == getAccountNumber()) {
            return TransactionResult.failure(TransactionResult.Status.TARGET_ACCOUNT_INVALID,
                    "不能向本账户转账。");
        }
        Account targetAccount = getBankDatabase().findAccount(target);
        if (targetAccount == null) {
            return TransactionResult.failure(TransactionResult.Status.TARGET_ACCOUNT_INVALID,
                    "转入账号不存在，请核对后重试。");
        }

        // 输入金额并校验余额
        double amount = keypad.readAmount("请输入转账金额（输入 0 取消交易）：");
        if (amount == 0) {
            return TransactionResult.cancelled();
        }
        if (amount < 0) {
            return TransactionResult.failure(TransactionResult.Status.INVALID_AMOUNT,
                    "转账金额必须大于 0。");
        }
        double available = getBankDatabase().getAvailableBalance(getAccountNumber());
        if (amount > available) {
            return TransactionResult.failure(TransactionResult.Status.INSUFFICIENT_BALANCE,
                    String.format("账户余额不足（可用余额 ￥%,.2f），无法完成转账。", available));
        }

        // 记账：本方扣款、对方入账
        getBankDatabase().transfer(getAccountNumber(), target, amount);
        targetAccountNumber = target;

        return TransactionResult.success(
                String.format("转账成功，已向 %s（账号 %d）转入 ￥%,.2f。",
                        targetAccount.getOwnerName(), target, amount),
                amount, getBankDatabase().getTotalBalance(getAccountNumber()));
    }

    @Override
    protected List<TransactionRecord> buildRecords(TransactionResult result) {
        if (!result.isSuccess() || targetAccountNumber < 0) {
            return super.buildRecords(result);
        }
        LocalDateTime now = LocalDateTime.now();
        TransactionRecord outRecord = new TransactionRecord(now, getAccountNumber(),
                "转账-转出", result.amount(),
                getBankDatabase().getTotalBalance(getAccountNumber()), result.status().getLabel());
        TransactionRecord inRecord = new TransactionRecord(now, targetAccountNumber,
                "转账-转入", result.amount(),
                getBankDatabase().getTotalBalance(targetAccountNumber), result.status().getLabel());
        return List.of(outRecord, inRecord);
    }
}

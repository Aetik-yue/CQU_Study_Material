package com.atm.transaction;

import com.atm.bank.BankDatabase;
import com.atm.hardware.DepositSlot;
import com.atm.hardware.Display;
import com.atm.hardware.Keypad;
import com.atm.hardware.ReceiptPrinter;

/**
 * 存款：先收到信封、再记账。
 *
 * <p>与取款的顺序恰好相反——存款的“先收钱后入账”由现实世界的物理动作决定：
 * 信封没投进来就入账，等于银行凭空亏钱。</p>
 */
public final class Deposit extends Transaction {

    private final Keypad keypad;
    private final DepositSlot depositSlot;

    public Deposit(int accountNumber, BankDatabase bankDatabase,
                   Display display, ReceiptPrinter receiptPrinter,
                   Keypad keypad, DepositSlot depositSlot) {
        super(accountNumber, bankDatabase, display, receiptPrinter);
        this.keypad = keypad;
        this.depositSlot = depositSlot;
    }

    @Override
    public String getTypeName() {
        return "存款";
    }

    @Override
    protected TransactionResult perform() {
        double amount = keypad.readAmount("请输入存款金额（输入 0 取消交易）：");
        if (amount == 0) {
            return TransactionResult.cancelled();
        }
        if (amount < 0) {
            return TransactionResult.failure(TransactionResult.Status.INVALID_AMOUNT,
                    "存款金额必须大于 0。");
        }

        // 先确认收到信封，再记账
        if (!depositSlot.isEnvelopeReceived()) {
            return TransactionResult.failure(TransactionResult.Status.ENVELOPE_NOT_RECEIVED,
                    "未收到存款信封，本次存款已取消（账户未入账）。");
        }
        getBankDatabase().credit(getAccountNumber(), amount);

        return TransactionResult.success(
                String.format("存款成功，本次存入 ￥%,.2f。", amount),
                amount, getBankDatabase().getTotalBalance(getAccountNumber()));
    }
}

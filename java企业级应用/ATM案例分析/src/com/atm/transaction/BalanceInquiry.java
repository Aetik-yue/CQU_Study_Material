package com.atm.transaction;

import com.atm.bank.BankDatabase;
import com.atm.hardware.Display;
import com.atm.hardware.ReceiptPrinter;

/**
 * 查询余额：只读交易，不改动数据库里的任何数据。
 */
public final class BalanceInquiry extends Transaction {

    public BalanceInquiry(int accountNumber, BankDatabase bankDatabase,
                          Display display, ReceiptPrinter receiptPrinter) {
        super(accountNumber, bankDatabase, display, receiptPrinter);
    }

    @Override
    public String getTypeName() {
        return "查询余额";
    }

    @Override
    protected TransactionResult perform() {
        double available = getBankDatabase().getAvailableBalance(getAccountNumber());
        double total = getBankDatabase().getTotalBalance(getAccountNumber());

        getDisplay().showAmountLine("可用余额", available);
        getDisplay().showAmountLine("总余额　", total);
        getDisplay().showMessageLine("（可用余额是当前能取走的部分；总余额含未结算资金）");

        return TransactionResult.success("余额查询完成。", 0, total);
    }
}

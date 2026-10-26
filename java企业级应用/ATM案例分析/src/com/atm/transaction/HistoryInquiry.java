package com.atm.transaction;

import com.atm.bank.BankDatabase;
import com.atm.bank.TransactionRecord;
import com.atm.hardware.Display;
import com.atm.hardware.ReceiptPrinter;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 查询交易流水：列出当前账户最近若干笔交易记录（相对教材版新增的用例）。
 */
public final class HistoryInquiry extends Transaction {

    /** 一次最多显示多少条流水。 */
    private static final int MAX_ROWS = 10;

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("MM-dd HH:mm:ss");

    public HistoryInquiry(int accountNumber, BankDatabase bankDatabase,
                          Display display, ReceiptPrinter receiptPrinter) {
        super(accountNumber, bankDatabase, display, receiptPrinter);
    }

    @Override
    public String getTypeName() {
        return "交易流水";
    }

    @Override
    protected TransactionResult perform() {
        List<TransactionRecord> records =
                getBankDatabase().getTransactionLog().recentOf(getAccountNumber(), MAX_ROWS);

        if (records.isEmpty()) {
            getDisplay().showMessageLine("暂无交易记录。");
        } else {
            getDisplay().showMessageLine(String.format("%-14s %-10s %12s %12s  %s",
                    "时间", "类型", "金额(元)", "余额(元)", "结果"));
            for (TransactionRecord record : records) {
                String amountText = record.amount() > 0
                        ? String.format("%,.2f", record.amount()) : "-";
                String balanceText = record.isSuccess()
                        ? String.format("%,.2f", record.balanceAfter()) : "-";
                getDisplay().showMessageLine(String.format("%-14s %-10s %12s %12s  %s",
                        TIME_FORMAT.format(record.time()), record.type(),
                        amountText, balanceText, record.statusText()));
            }
        }
        return TransactionResult.success("已显示最近 " + records.size() + " 条交易记录。",
                0, getBankDatabase().getTotalBalance(getAccountNumber()));
    }
}

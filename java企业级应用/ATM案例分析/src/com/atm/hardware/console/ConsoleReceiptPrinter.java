package com.atm.hardware.console;

import com.atm.bank.TransactionRecord;
import com.atm.hardware.Display;
import com.atm.hardware.ReceiptPrinter;

import java.time.format.DateTimeFormatter;

/**
 * 凭条打印机的控制台实现：把凭条内容打印到屏幕。
 */
public class ConsoleReceiptPrinter implements ReceiptPrinter {

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Display display;

    public ConsoleReceiptPrinter(Display display) {
        this.display = display;
    }

    @Override
    public void printReceipt(TransactionRecord record) {
        display.showMessageLine("");
        display.showMessageLine("        —— 交易凭条 ——");
        display.showMessageLine("时间：" + TIME_FORMAT.format(record.time()));
        display.showMessageLine("账号：" + record.accountNumber());
        display.showMessageLine("类型：" + record.type());
        if (record.amount() > 0) {
            display.showMessageLine(String.format("金额：￥%,.2f", record.amount()));
        }
        if (record.isSuccess()) {
            display.showMessageLine(String.format("余额：￥%,.2f", record.balanceAfter()));
        }
        display.showMessageLine("结果：" + record.statusText());
        display.showMessageLine("（凭条打印完成）");
    }
}

package com.atm.hardware;

import com.atm.bank.TransactionRecord;

/**
 * 凭条打印机：把一笔成功交易打印成凭条。
 *
 * <p>这是相对教材版新增的硬件部件——新增它只需要实现接口，
 * 交易模板方法里的“打凭条”一步不用改。</p>
 */
public interface ReceiptPrinter {

    /** 打印一张交易凭条。 */
    void printReceipt(TransactionRecord record);
}

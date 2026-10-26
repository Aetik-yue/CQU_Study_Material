package com.atm.bank;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 交易流水账本：按时间顺序保存所有账户的交易记录，支持按账户查询。
 */
public class TransactionLog {

    private final List<TransactionRecord> records = new ArrayList<>();

    /** 追加一条流水。 */
    public void append(TransactionRecord record) {
        records.add(record);
    }

    /**
     * 查询某账户最近的若干条流水（按时间倒序，最新的在前）。
     *
     * @param accountNumber 账户号
     * @param limit         最多返回条数
     */
    public List<TransactionRecord> recentOf(int accountNumber, int limit) {
        return records.stream()
                .filter(r -> r.accountNumber() == accountNumber)
                .sorted(Comparator.comparing(TransactionRecord::time).reversed())
                .limit(limit)
                .toList();
    }

    /** 全部记录条数（供测试核对）。 */
    public int size() {
        return records.size();
    }
}

package com.atm.bank;

import java.time.LocalDateTime;

/**
 * 一条交易流水记录（不可变的值对象）。
 *
 * <p>使用 record：交易记录一旦生成就不允许修改，天然适合不可变数据。</p>
 *
 * @param time          交易发生时间
 * @param accountNumber 所属账户
 * @param type          交易类型，如“取款”“转账-转出”
 * @param amount        涉及金额（查询类交易为 0）
 * @param balanceAfter  交易后总余额（失败/取消时为 NaN，表示无意义）
 * @param statusText    结果描述，如“成功”“失败-余额不足”“已取消”
 */
public record TransactionRecord(LocalDateTime time, int accountNumber, String type,
                                double amount, double balanceAfter, String statusText) {

    /** 该记录是否为成功交易。 */
    public boolean isSuccess() {
        return !Double.isNaN(balanceAfter);
    }
}

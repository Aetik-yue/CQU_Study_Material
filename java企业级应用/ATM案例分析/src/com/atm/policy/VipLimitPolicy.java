package com.atm.policy;

/**
 * VIP 账户限额策略：单笔不超过 20000 元，当日累计不超过 50000 元。
 *
 * <p>与普通策略的唯一差别是两组数值——新增一类客户，不需要改动任何交易代码。</p>
 */
public class VipLimitPolicy extends AbstractLimitPolicy {

    @Override
    public String name() {
        return "VIP 账户限额策略";
    }

    @Override
    public double perTransactionLimit() {
        return 20000.00;
    }

    @Override
    public double dailyLimit() {
        return 50000.00;
    }
}

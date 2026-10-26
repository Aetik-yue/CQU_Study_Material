package com.atm.policy;

/**
 * 普通账户限额策略：单笔不超过 2500 元，当日累计不超过 5000 元。
 */
public class StandardLimitPolicy extends AbstractLimitPolicy {

    @Override
    public String name() {
        return "普通账户限额策略";
    }

    @Override
    public double perTransactionLimit() {
        return 2500.00;
    }

    @Override
    public double dailyLimit() {
        return 5000.00;
    }
}

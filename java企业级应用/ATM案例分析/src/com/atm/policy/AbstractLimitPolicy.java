package com.atm.policy;

import java.util.Optional;

/**
 * 限额策略的公共骨架：把“单笔 + 当日累计”两级校验的通用流程
 * 沉到抽象类里，子类只需要给出两个上限数值和策略名称。
 *
 * <p>这是第 5 章“抽象类 + 模板方法”的又一次应用：校验流程固定不变，
 * 变化点（限额数值）延迟到子类。</p>
 */
public abstract class AbstractLimitPolicy implements WithdrawalLimitPolicy {

    @Override
    public Optional<String> validate(double amount, double withdrawnToday) {
        if (amount > perTransactionLimit()) {
            return Optional.of(String.format("超出单笔取款限额（本账户单笔上限 ￥%,.2f）", perTransactionLimit()));
        }
        double remaining = dailyLimit() - withdrawnToday;
        if (amount > remaining) {
            return Optional.of(String.format(
                    "超出当日累计取款限额（本账户日限额 ￥%,.2f，今日剩余额度 ￥%,.2f）",
                    dailyLimit(), Math.max(remaining, 0)));
        }
        return Optional.empty();
    }
}

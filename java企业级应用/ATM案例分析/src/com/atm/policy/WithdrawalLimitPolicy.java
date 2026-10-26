package com.atm.policy;

import java.util.Optional;

/**
 * 取款限额策略：定义“一次取款最多能取多少、当天累计最多能取多少”的规则。
 *
 * <p>不同账户可以配置不同策略（普通账户、VIP 账户），取款交易只依赖本接口，
 * 不关心具体规则——这就是“策略模式”的注入点。</p>
 */
public interface WithdrawalLimitPolicy {

    /** 策略名称，用于界面提示（如“标准账户限额策略”）。 */
    String name();

    /** 单笔取款上限（元）。 */
    double perTransactionLimit();

    /** 当日累计取款上限（元）。 */
    double dailyLimit();

    /**
     * 校验一笔取款是否在限额之内。
     *
     * @param amount        本次取款金额
     * @param withdrawnToday 该账户当日已累计取款金额
     * @return 通过时返回 {@link Optional#empty()}；不通过时返回拒绝原因
     */
    Optional<String> validate(double amount, double withdrawnToday);
}

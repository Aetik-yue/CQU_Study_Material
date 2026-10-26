package com.atm.hardware;

/**
 * 出钞机：持有钞箱，负责判断现金是否足够并执行吐钞。
 */
public interface CashDispenser {

    /** 单张钞票面额（元）。 */
    int BILL_VALUE = 100;

    /** 钞箱里的现金是否够支付指定金额。 */
    boolean isSufficientCashAvailable(double amount);

    /** 吐钞（业务规则校验通过后才会被调用）。 */
    void dispenseCash(double amount);
}

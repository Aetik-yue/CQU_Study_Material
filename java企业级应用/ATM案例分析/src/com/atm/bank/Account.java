package com.atm.bank;

import com.atm.policy.WithdrawalLimitPolicy;

/**
 * 银行账户：保存账号、户名、密码、余额，以及本账户适用的取款限额策略。
 *
 * <p>所有属性私有，对外只提供读方法和受控的记账方法（debit / credit），
 * 余额永远不能被外部直接修改——这是封装的第一道防线。</p>
 */
public class Account {

    private final int accountNumber;
    private final String ownerName;
    private final String pin;
    private final WithdrawalLimitPolicy limitPolicy;

    private double availableBalance;
    private double totalBalance;
    /** 今日已累计取款金额（模拟银行端记录的当日数据，用于限额策略判断）。 */
    private double withdrawnToday;

    public Account(int accountNumber, String ownerName, String pin,
                   double availableBalance, double totalBalance,
                   double withdrawnToday, WithdrawalLimitPolicy limitPolicy) {
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.pin = pin;
        this.availableBalance = availableBalance;
        this.totalBalance = totalBalance;
        this.withdrawnToday = withdrawnToday;
        this.limitPolicy = limitPolicy;
    }

    /**
     * 支出（取款、转出）：可用余额与总余额同时减少。
     */
    public void debit(double amount) {
        availableBalance -= amount;
        totalBalance -= amount;
    }

    /**
     * 收入（存款、转入）：可用余额与总余额同时增加。
     */
    public void credit(double amount) {
        availableBalance += amount;
        totalBalance += amount;
    }

    /** 记录一笔当日取现（用于限额策略的“当日累计”判断）。 */
    public void addWithdrawnToday(double amount) {
        withdrawnToday += amount;
    }

    public boolean checkPin(String inputPin) {
        return pin.equals(inputPin);
    }

    public int getAccountNumber() {
        return accountNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public double getAvailableBalance() {
        return availableBalance;
    }

    public double getTotalBalance() {
        return totalBalance;
    }

    public double getWithdrawnToday() {
        return withdrawnToday;
    }

    public WithdrawalLimitPolicy getLimitPolicy() {
        return limitPolicy;
    }
}

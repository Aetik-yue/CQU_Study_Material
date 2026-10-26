package com.atm.bank;

import com.atm.policy.StandardLimitPolicy;
import com.atm.policy.VipLimitPolicy;

import java.util.HashMap;
import java.util.Map;

/**
 * 银行数据库：账户与卡片数据的唯一入口，采用饿汉式单例。
 *
 * <p>ATM 的任何业务类都不允许直接持有账户数据，必须通过本类的方法读写，
 * 保证数据来源单一、认证与记账规则集中。</p>
 *
 * <p>本工程是控制台模拟程序，数据库以内存 Map 实现并预置三组演示数据，
 * 不涉及真实持久化（见文档“改进方向”）。</p>
 */
public final class BankDatabase {

    /** 密码连续输错多少次触发吞卡。 */
    public static final int MAX_PIN_ATTEMPTS = 3;

    private static final BankDatabase INSTANCE = new BankDatabase();

    private final Map<Integer, Account> accounts = new HashMap<>();
    private final Map<Integer, BankCard> cards = new HashMap<>();
    /** 账户号 -> 当前连续输错密码次数。 */
    private final Map<Integer, Integer> pinFailures = new HashMap<>();
    private final TransactionLog transactionLog = new TransactionLog();

    private BankDatabase() {
        initDemoData();
    }

    /** 全局唯一实例。 */
    public static BankDatabase getInstance() {
        return INSTANCE;
    }

    /**
     * 构造一份独立的新数据库（含同样的演示数据）。
     *
     * <p>仅用于自动化测试：让每个用例互不干扰。业务代码一律使用 {@link #getInstance()}。</p>
     */
    public static BankDatabase createForTesting() {
        return new BankDatabase();
    }

    // ------------------------------------------------------------------
    // 数据查询
    // ------------------------------------------------------------------

    /** 按卡号找卡；找不到返回 null。 */
    public BankCard findCard(int cardNumber) {
        return cards.get(cardNumber);
    }

    /** 按账号找账户；找不到返回 null。 */
    public Account findAccount(int accountNumber) {
        return accounts.get(accountNumber);
    }

    /** 查询可用余额。 */
    public double getAvailableBalance(int accountNumber) {
        return requireAccount(accountNumber).getAvailableBalance();
    }

    /** 查询总余额。 */
    public double getTotalBalance(int accountNumber) {
        return requireAccount(accountNumber).getTotalBalance();
    }

    private Account requireAccount(int accountNumber) {
        Account account = accounts.get(accountNumber);
        if (account == null) {
            throw new IllegalArgumentException("账号不存在：" + accountNumber);
        }
        return account;
    }

    // ------------------------------------------------------------------
    // 认证
    // ------------------------------------------------------------------

    /**
     * 校验密码。
     *
     * <p>连续输错达到 {@link #MAX_PIN_ATTEMPTS} 次时返回 {@link AuthResult#CARD_RETAINED}；
     * 认证成功后输错计数清零。</p>
     */
    public AuthResult authenticate(int accountNumber, String pin) {
        Account account = requireAccount(accountNumber);
        if (account.checkPin(pin)) {
            pinFailures.put(accountNumber, 0);
            return AuthResult.SUCCESS;
        }
        int failures = pinFailures.getOrDefault(accountNumber, 0) + 1;
        pinFailures.put(accountNumber, failures);
        return failures >= MAX_PIN_ATTEMPTS ? AuthResult.CARD_RETAINED : AuthResult.WRONG_PIN;
    }

    /** 认证失败后还剩几次输入机会。 */
    public int getRemainingPinAttempts(int accountNumber) {
        int failures = pinFailures.getOrDefault(accountNumber, 0);
        return Math.max(MAX_PIN_ATTEMPTS - failures, 0);
    }

    // ------------------------------------------------------------------
    // 记账
    // ------------------------------------------------------------------

    /** 支出：取款、转出。 */
    public void debit(int accountNumber, double amount) {
        requireAccount(accountNumber).debit(amount);
    }

    /** 收入：存款、转入。 */
    public void credit(int accountNumber, double amount) {
        requireAccount(accountNumber).credit(amount);
    }

    /** 记录一笔当日取现（供限额策略使用）。 */
    public void registerWithdrawal(int accountNumber, double amount) {
        requireAccount(accountNumber).addWithdrawnToday(amount);
    }

    /** 账户间转账：从 from 扣款、向 to 入账，两步都通过本入口完成。 */
    public void transfer(int fromAccountNumber, int toAccountNumber, double amount) {
        Account from = requireAccount(fromAccountNumber);
        Account to = requireAccount(toAccountNumber);
        from.debit(amount);
        to.credit(amount);
    }

    /** 交易流水账本。 */
    public TransactionLog getTransactionLog() {
        return transactionLog;
    }

    // ------------------------------------------------------------------
    // 演示数据
    // ------------------------------------------------------------------

    /**
     * 预置演示数据（三张卡、三个账户）：
     *
     * <pre>
     * 卡号 62220001 -> 账户 1001 张伟  密码 123456  可用 1000.00  总 1200.00  今日已取    0.00  普通
     * 卡号 62220002 -> 账户 1002 李娜  密码 654321  可用 8000.00  总 9000.00  今日已取 4000.00  普通
     * 卡号 62220003 -> 账户 1003 王强  密码 888888  可用 50000.00 总 50000.00 今日已取    0.00  VIP
     * </pre>
     */
    private void initDemoData() {
        addAccount(new Account(1001, "张伟", "123456", 1000.00, 1200.00, 0.00,
                new StandardLimitPolicy()), 62220001);
        addAccount(new Account(1002, "李娜", "654321", 8000.00, 9000.00, 4000.00,
                new StandardLimitPolicy()), 62220002);
        addAccount(new Account(1003, "王强", "888888", 50000.00, 50000.00, 0.00,
                new VipLimitPolicy()), 62220003);
    }

    private void addAccount(Account account, int cardNumber) {
        accounts.put(account.getAccountNumber(), account);
        cards.put(cardNumber, new BankCard(cardNumber, account.getAccountNumber()));
    }
}

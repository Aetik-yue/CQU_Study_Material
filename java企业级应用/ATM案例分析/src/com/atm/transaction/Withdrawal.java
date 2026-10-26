package com.atm.transaction;

import com.atm.bank.Account;
import com.atm.bank.BankDatabase;
import com.atm.hardware.CashDispenser;
import com.atm.hardware.Display;
import com.atm.hardware.Keypad;
import com.atm.hardware.ReceiptPrinter;
import com.atm.ui.Menu;

import java.util.Optional;

/**
 * 取款：本案例中判断最多的交易。
 *
 * <p>校验顺序（与取款活动图一致）：</p>
 * <ol>
 *   <li>金额本身是否合法（100 元的整数倍）；</li>
 *   <li>账户可用余额是否足够；</li>
 *   <li>是否超出本账户的取款限额策略；</li>
 *   <li>钞箱现金是否足够。</li>
 * </ol>
 *
 * <p>先做与用户直接相关、代价低的判断，最后才检查机器硬件，
 * 这样用户不会因为机器状态而收到与自己无关的报错。
 * 记账（debit）在吐钞之前完成：账已扣、钞未出可以人工对账；
 * 反过来钱出去了账没扣则无法追回。</p>
 */
public final class Withdrawal extends Transaction {

    /** 取款菜单的可选项：五个常用金额 + 自定义 + 取消。 */
    public enum Option {
        Y100(100), Y200(200), Y500(500), Y1000(1000), Y2000(2000),
        CUSTOM(-1), CANCEL(0);

        private final double amount;

        Option(double amount) {
            this.amount = amount;
        }

        public double getAmount() {
            return amount;
        }
    }

    private final Keypad keypad;
    private final CashDispenser cashDispenser;

    public Withdrawal(int accountNumber, BankDatabase bankDatabase,
                      Display display, ReceiptPrinter receiptPrinter,
                      Keypad keypad, CashDispenser cashDispenser) {
        super(accountNumber, bankDatabase, display, receiptPrinter);
        this.keypad = keypad;
        this.cashDispenser = cashDispenser;
    }

    @Override
    public String getTypeName() {
        return "取款";
    }

    @Override
    protected TransactionResult perform() {
        // 第一步：选择金额
        Menu<Option> menu = new Menu<Option>("取款菜单", getDisplay(), keypad)
                .add("￥100", Option.Y100)
                .add("￥200", Option.Y200)
                .add("￥500", Option.Y500)
                .add("￥1000", Option.Y1000)
                .add("￥2000", Option.Y2000)
                .add("其他金额", Option.CUSTOM)
                .add("取消交易", Option.CANCEL);
        Option choice = menu.select();

        if (choice == Option.CANCEL) {
            return TransactionResult.cancelled();
        }
        double amount = choice == Option.CUSTOM
                ? keypad.readAmount("请输入取款金额（须为 100 元的整数倍，输入 0 取消交易）：")
                : choice.getAmount();
        if (amount == 0) {
            return TransactionResult.cancelled();
        }

        // 判断一：金额本身合法吗（正数、100 的整数倍）
        if (amount < 0 || Math.abs(amount % CashDispenser.BILL_VALUE) > 1e-9) {
            return TransactionResult.failure(TransactionResult.Status.INVALID_AMOUNT,
                    "取款金额必须为 100 元的整数倍。");
        }

        // 判断二：账户余额够吗
        double available = getBankDatabase().getAvailableBalance(getAccountNumber());
        if (amount > available) {
            return TransactionResult.failure(TransactionResult.Status.INSUFFICIENT_BALANCE,
                    String.format("账户余额不足（可用余额 ￥%,.2f），请重新选择取款金额。", available));
        }

        // 判断三：限额策略允许吗
        Account account = getBankDatabase().findAccount(getAccountNumber());
        Optional<String> limitViolation = account.getLimitPolicy()
                .validate(amount, account.getWithdrawnToday());
        if (limitViolation.isPresent()) {
            return TransactionResult.failure(TransactionResult.Status.LIMIT_EXCEEDED,
                    limitViolation.get());
        }

        // 判断四：钞箱里的现金够吗
        if (!cashDispenser.isSufficientCashAvailable(amount)) {
            return TransactionResult.failure(TransactionResult.Status.INSUFFICIENT_CASH,
                    "取款机现金不足，请选择更小的取款金额。");
        }

        // 先记账、再吐钞
        getBankDatabase().debit(getAccountNumber(), amount);
        getBankDatabase().registerWithdrawal(getAccountNumber(), amount);
        cashDispenser.dispenseCash(amount);

        return TransactionResult.success(
                "取款成功。",
                amount, getBankDatabase().getTotalBalance(getAccountNumber()));
    }
}

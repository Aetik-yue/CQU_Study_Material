import com.atm.bank.AuthResult;
import com.atm.bank.BankCard;
import com.atm.bank.BankDatabase;
import com.atm.bank.TransactionRecord;
import com.atm.hardware.CashDispenser;
import com.atm.hardware.DepositSlot;
import com.atm.hardware.Display;
import com.atm.hardware.Keypad;
import com.atm.hardware.ReceiptPrinter;
import com.atm.transaction.BalanceInquiry;
import com.atm.transaction.Deposit;
import com.atm.transaction.HistoryInquiry;
import com.atm.transaction.Transaction;
import com.atm.transaction.TransactionResult;
import com.atm.transaction.Transfer;
import com.atm.transaction.Withdrawal;
import com.atm.ui.Menu;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 冒烟测试：用“假硬件”直接驱动交易类与银行层，不经过真实控制台输入。
 *
 * <p>这正是“硬件面向接口”的收益：测试替身（Stub）替代屏幕、键盘、出钞机，
 * 每一条业务规则都可以脱离真实设备自动验证。</p>
 *
 * <p>编译运行：</p>
 * <pre>
 * javac -encoding UTF-8 -d out-test -cp out test\SmokeTest.java
 * java -Dstdout.encoding=UTF-8 -cp "out;out-test" SmokeTest
 * </pre>
 */
public class SmokeTest {

    private static int passed = 0;
    private static int failed = 0;

    // ------------------------------------------------------------------
    // 测试替身：假硬件
    // ------------------------------------------------------------------

    /** 脚本化键盘：按事先写好的输入依次“按键”。 */
    static class ScriptedKeypad implements Keypad {
        private final Deque<String> inputs = new ArrayDeque<>();

        ScriptedKeypad(String... values) {
            for (String value : values) {
                inputs.add(value);
            }
        }

        @Override
        public String readLine(String prompt) {
            return inputs.removeFirst();
        }

        @Override
        public int readInt(String prompt) {
            return Integer.parseInt(inputs.removeFirst());
        }

        @Override
        public double readAmount(String prompt) {
            return Double.parseDouble(inputs.removeFirst());
        }
    }

    /** 记录型屏幕：把所有输出收集起来供断言检查。 */
    static class CapturingDisplay implements Display {
        private final StringBuilder log = new StringBuilder();

        @Override
        public void showMessage(String message) {
        }

        @Override
        public void showMessageLine(String message) {
            log.append(message).append('\n');
        }

        @Override
        public void showAmountLine(String label, double amount) {
            log.append(label).append(amount).append('\n');
        }

        boolean contains(String text) {
            return log.toString().contains(text);
        }
    }

    /** 假出钞机：只维护钞箱张数。 */
    static class StubDispenser implements CashDispenser {
        int billCount = 100;

        @Override
        public boolean isSufficientCashAvailable(double amount) {
            return billCount >= amount / BILL_VALUE;
        }

        @Override
        public void dispenseCash(double amount) {
            billCount -= (int) (amount / BILL_VALUE);
        }
    }

    /** 假存款槽：收不收信封由参数决定。 */
    static class StubDepositSlot implements DepositSlot {
        private final boolean received;

        StubDepositSlot(boolean received) {
            this.received = received;
        }

        @Override
        public boolean isEnvelopeReceived() {
            return received;
        }
    }

    /** 假凭条打印机：只数打印了几张。 */
    static class StubReceiptPrinter implements ReceiptPrinter {
        int printed = 0;

        @Override
        public void printReceipt(TransactionRecord record) {
            printed++;
        }
    }

    // ------------------------------------------------------------------
    // 断言与辅助
    // ------------------------------------------------------------------

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("[PASS] " + name);
        } else {
            failed++;
            System.out.println("[FAIL] " + name);
        }
    }

    private static boolean eq(double actual, double expected) {
        return Math.abs(actual - expected) < 1e-6;
    }

    private static TransactionResult run(Transaction transaction) {
        return transaction.execute();
    }

    // ------------------------------------------------------------------
    // 用例
    // ------------------------------------------------------------------

    static void testAuthentication() {
        BankDatabase db = BankDatabase.createForTesting();
        check("认证：密码错误返回 WRONG_PIN",
                db.authenticate(1001, "000000") == AuthResult.WRONG_PIN);
        check("认证：第二次错误后剩余 1 次机会",
                db.authenticate(1001, "111111") == AuthResult.WRONG_PIN
                        && db.getRemainingPinAttempts(1001) == 1);
        check("认证：第三次错误触发吞卡",
                db.authenticate(1001, "222222") == AuthResult.CARD_RETAINED);
        check("认证：正确密码重置计数并成功",
                db.authenticate(1002, "654321") == AuthResult.SUCCESS);
        BankCard card = db.findCard(62220001);
        check("银行卡：初始状态正常", !card.isRetained());
        card.retain();
        check("银行卡：吞卡后状态为 RETAINED", card.isRetained());
    }

    static void testBalanceInquiry() {
        BankDatabase db = BankDatabase.createForTesting();
        CapturingDisplay display = new CapturingDisplay();
        StubReceiptPrinter printer = new StubReceiptPrinter();

        TransactionResult result = run(new BalanceInquiry(1001, db, display, printer));

        check("查询余额：成功", result.isSuccess());
        check("查询余额：可用余额不变", eq(db.getAvailableBalance(1001), 1000.00));
        check("查询余额：总余额不变", eq(db.getTotalBalance(1001), 1200.00));
        check("查询余额：打印凭条 1 张", printer.printed == 1);
        check("查询余额：流水记录 1 条", db.getTransactionLog().size() == 1);
        check("查询余额：屏幕显示可用余额", display.contains("可用余额"));
    }

    static void testWithdrawalInsufficientBalance() {
        BankDatabase db = BankDatabase.createForTesting();
        StubDispenser dispenser = new StubDispenser();
        StubReceiptPrinter printer = new StubReceiptPrinter();

        // 账户 1001 可用余额 1000，取 2000（菜单第 5 项）
        TransactionResult result = run(new Withdrawal(1001, db, new CapturingDisplay(),
                printer, new ScriptedKeypad("5"), dispenser));

        check("取款-余额不足：返回 INSUFFICIENT_BALANCE",
                result.status() == TransactionResult.Status.INSUFFICIENT_BALANCE);
        check("取款-余额不足：余额未被扣减", eq(db.getAvailableBalance(1001), 1000.00));
        check("取款-余额不足：钞箱未吐钞", dispenser.billCount == 100);
        check("取款-余额不足：不打印凭条", printer.printed == 0);
    }

    static void testWithdrawalSuccess() {
        BankDatabase db = BankDatabase.createForTesting();
        StubDispenser dispenser = new StubDispenser();
        StubReceiptPrinter printer = new StubReceiptPrinter();

        // 账户 1001 取 500（菜单第 3 项）
        TransactionResult result = run(new Withdrawal(1001, db, new CapturingDisplay(),
                printer, new ScriptedKeypad("3"), dispenser));

        check("取款-成功：返回 SUCCESS", result.isSuccess());
        check("取款-成功：可用余额 1000-500=500", eq(db.getAvailableBalance(1001), 500.00));
        check("取款-成功：总余额 1200-500=700", eq(db.getTotalBalance(1001), 700.00));
        check("取款-成功：钞箱减少 5 张", dispenser.billCount == 95);
        check("取款-成功：今日已取累计 500",
                eq(db.findAccount(1001).getWithdrawnToday(), 500.00));
        check("取款-成功：打印凭条 1 张", printer.printed == 1);
        check("取款-成功：流水记录 1 条", db.getTransactionLog().size() == 1);
    }

    static void testWithdrawalLimitExceeded() {
        BankDatabase db = BankDatabase.createForTesting();
        StubDispenser dispenser = new StubDispenser();

        // 账户 1002 可用余额 8000，但今日已取 4000、日限额 5000，再取 2000 超限
        TransactionResult result = run(new Withdrawal(1002, db, new CapturingDisplay(),
                new StubReceiptPrinter(), new ScriptedKeypad("5"), dispenser));

        check("取款-超限：返回 LIMIT_EXCEEDED",
                result.status() == TransactionResult.Status.LIMIT_EXCEEDED);
        check("取款-超限：余额未被扣减", eq(db.getAvailableBalance(1002), 8000.00));
        check("取款-超限：提示含“当日累计取款限额”", result.message().contains("当日累计"));
    }

    static void testWithdrawalInsufficientCash() {
        BankDatabase db = BankDatabase.createForTesting();
        StubDispenser dispenser = new StubDispenser(); // 100 张 = 10000 元

        // 账户 1003 是 VIP：余额、单笔、日累计都通过，但钞箱只有 10000 元，取 20000
        TransactionResult result = run(new Withdrawal(1003, db, new CapturingDisplay(),
                new StubReceiptPrinter(), new ScriptedKeypad("6", "20000"), dispenser));

        check("取款-钞箱不足：返回 INSUFFICIENT_CASH",
                result.status() == TransactionResult.Status.INSUFFICIENT_CASH);
        check("取款-钞箱不足：余额未被扣减", eq(db.getAvailableBalance(1003), 50000.00));
        check("取款-钞箱不足：钞箱数字不变", dispenser.billCount == 100);
    }

    static void testWithdrawalInvalidAmountAndCancel() {
        BankDatabase db = BankDatabase.createForTesting();
        StubDispenser dispenser = new StubDispenser();

        // 自定义金额 250：不是 100 的整数倍
        TransactionResult invalid = run(new Withdrawal(1001, db, new CapturingDisplay(),
                new StubReceiptPrinter(), new ScriptedKeypad("6", "250"), dispenser));
        check("取款-金额非法：返回 INVALID_AMOUNT",
                invalid.status() == TransactionResult.Status.INVALID_AMOUNT);

        // 取消交易（菜单第 7 项）
        TransactionResult cancel = run(new Withdrawal(1001, db, new CapturingDisplay(),
                new StubReceiptPrinter(), new ScriptedKeypad("7"), dispenser));
        check("取款-取消：返回 CANCELLED", cancel.isCancelled());
        check("取款-取消：余额不变", eq(db.getAvailableBalance(1001), 1000.00));
    }

    static void testDeposit() {
        BankDatabase db = BankDatabase.createForTesting();
        StubReceiptPrinter printer = new StubReceiptPrinter();

        // 未收到信封：不入账
        TransactionResult noEnvelope = run(new Deposit(1001, db, new CapturingDisplay(),
                printer, new ScriptedKeypad("1000"), new StubDepositSlot(false)));
        check("存款-未收到信封：返回 ENVELOPE_NOT_RECEIVED",
                noEnvelope.status() == TransactionResult.Status.ENVELOPE_NOT_RECEIVED);
        check("存款-未收到信封：余额不变", eq(db.getTotalBalance(1001), 1200.00));
        check("存款-未收到信封：不打印凭条", printer.printed == 0);

        // 收到信封：入账
        TransactionResult success = run(new Deposit(1001, db, new CapturingDisplay(),
                printer, new ScriptedKeypad("1000"), new StubDepositSlot(true)));
        check("存款-成功：返回 SUCCESS", success.isSuccess());
        check("存款-成功：可用余额 1000+1000=2000", eq(db.getAvailableBalance(1001), 2000.00));
        check("存款-成功：总余额 1200+1000=2200", eq(db.getTotalBalance(1001), 2200.00));
        check("存款-成功：打印凭条 1 张", printer.printed == 1);

        // 输入 0 取消
        TransactionResult cancel = run(new Deposit(1001, db, new CapturingDisplay(),
                printer, new ScriptedKeypad("0"), new StubDepositSlot(true)));
        check("存款-取消：返回 CANCELLED", cancel.isCancelled());
    }

    static void testTransfer() {
        BankDatabase db = BankDatabase.createForTesting();
        StubReceiptPrinter printer = new StubReceiptPrinter();

        // 成功：1001 -> 1002 转 200
        TransactionResult success = run(new Transfer(1001, db, new CapturingDisplay(),
                printer, new ScriptedKeypad("1002", "200")));
        check("转账-成功：返回 SUCCESS", success.isSuccess());
        check("转账-成功：转出方余额 1000-200=800",
                eq(db.getAvailableBalance(1001), 800.00));
        check("转账-成功：转入方余额 8000+200=8200",
                eq(db.getAvailableBalance(1002), 8200.00));
        check("转账-成功：两个账户各记一条流水", db.getTransactionLog().size() == 2);
        check("转账-成功：打印凭条 1 张", printer.printed == 1);

        // 目标账号不存在
        TransactionResult notFound = run(new Transfer(1001, db, new CapturingDisplay(),
                printer, new ScriptedKeypad("9999", "100")));
        check("转账-目标不存在：返回 TARGET_ACCOUNT_INVALID",
                notFound.status() == TransactionResult.Status.TARGET_ACCOUNT_INVALID);

        // 向本账户转账
        TransactionResult self = run(new Transfer(1001, db, new CapturingDisplay(),
                printer, new ScriptedKeypad("1001", "100")));
        check("转账-本账户：返回 TARGET_ACCOUNT_INVALID",
                self.status() == TransactionResult.Status.TARGET_ACCOUNT_INVALID);

        // 余额不足
        TransactionResult poor = run(new Transfer(1001, db, new CapturingDisplay(),
                printer, new ScriptedKeypad("1002", "99999")));
        check("转账-余额不足：返回 INSUFFICIENT_BALANCE",
                poor.status() == TransactionResult.Status.INSUFFICIENT_BALANCE);

        // 取消（目标账号输入 0）
        TransactionResult cancel = run(new Transfer(1001, db, new CapturingDisplay(),
                printer, new ScriptedKeypad("0")));
        check("转账-取消：返回 CANCELLED", cancel.isCancelled());
    }

    static void testHistoryInquiry() {
        BankDatabase db = BankDatabase.createForTesting();
        CapturingDisplay display = new CapturingDisplay();

        // 先产生一笔取款、一笔转账，再查流水
        run(new Withdrawal(1001, db, new CapturingDisplay(), new StubReceiptPrinter(),
                new ScriptedKeypad("1"), new StubDispenser()));
        run(new Transfer(1001, db, new CapturingDisplay(), new StubReceiptPrinter(),
                new ScriptedKeypad("1002", "100")));
        TransactionResult result = run(new HistoryInquiry(1001, db, display, new StubReceiptPrinter()));

        check("流水查询：成功", result.isSuccess());
        check("流水查询：显示取款记录", display.contains("取款"));
        check("流水查询：显示转账-转出记录", display.contains("转账-转出"));
        check("流水查询：显示最近 2 条", result.message().contains("2 条"));
    }

    static void testMenuRetryOnInvalidChoice() {
        CapturingDisplay display = new CapturingDisplay();
        // 先输入越界数字 9，再输入 2 → 应返回第二项
        Menu<String> menu = new Menu<String>("测试菜单", display, new ScriptedKeypad("9", "2"))
                .add("选项一", "A")
                .add("选项二", "B");
        String picked = menu.select();
        check("菜单：越界输入后重试并返回正确选项", "B".equals(picked));
        check("菜单：提示了输入无效", display.contains("输入无效"));
    }

    // ------------------------------------------------------------------

    public static void main(String[] args) {
        System.out.println("================ ATM 冒烟测试 ================");
        testAuthentication();
        testBalanceInquiry();
        testWithdrawalInsufficientBalance();
        testWithdrawalSuccess();
        testWithdrawalLimitExceeded();
        testWithdrawalInsufficientCash();
        testWithdrawalInvalidAmountAndCancel();
        testDeposit();
        testTransfer();
        testHistoryInquiry();
        testMenuRetryOnInvalidChoice();
        System.out.println("==============================================");
        System.out.printf("通过 %d 项，失败 %d 项。%n", passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
    }
}

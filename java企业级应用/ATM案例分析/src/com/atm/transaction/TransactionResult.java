package com.atm.transaction;

/**
 * 一次交易的执行结果（不可变值对象）。
 *
 * <p>交易不再用 boolean 表示成败：失败原因、金额、交易后余额都封在结果对象里，
 * 由调用方（模板方法、流水账本）统一处理。</p>
 *
 * @param status       结果状态
 * @param message      面向用户的说明文字
 * @param amount       涉及的金额（查询类交易为 0）
 * @param balanceAfter 成功时的交易后总余额，其余情况为 {@link Double#NaN}
 */
public record TransactionResult(Status status, String message, double amount, double balanceAfter) {

    /** 交易结果状态。每个状态自带一句简短中文说明，用于流水与凭条。 */
    public enum Status {
        SUCCESS("成功"),
        CANCELLED("已取消"),
        INVALID_AMOUNT("金额无效"),
        INSUFFICIENT_BALANCE("余额不足"),
        LIMIT_EXCEEDED("超出限额"),
        INSUFFICIENT_CASH("钞箱现金不足"),
        ENVELOPE_NOT_RECEIVED("未收到信封"),
        TARGET_ACCOUNT_INVALID("目标账号无效"),
        FAILED("失败");

        private final String label;

        Status(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public static TransactionResult success(String message, double amount, double balanceAfter) {
        return new TransactionResult(Status.SUCCESS, message, amount, balanceAfter);
    }

    public static TransactionResult failure(Status status, String message) {
        return new TransactionResult(status, message, 0, Double.NaN);
    }

    public static TransactionResult cancelled() {
        return new TransactionResult(Status.CANCELLED, "用户取消", 0, Double.NaN);
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public boolean isCancelled() {
        return status == Status.CANCELLED;
    }
}

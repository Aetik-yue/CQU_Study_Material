package com.atm.bank;

/**
 * 银行卡：记录卡号、关联的银行账户号以及卡片当前状态。
 *
 * <p>与教材版不同，本设计把“卡”从一个输入的数字提升为独立的对象：
 * 卡会随认证失败被吞（状态改变），这个状态必须由系统记住。</p>
 */
public class BankCard {

    private final int cardNumber;
    private final int accountNumber;
    private CardStatus status;

    public BankCard(int cardNumber, int accountNumber) {
        this.cardNumber = cardNumber;
        this.accountNumber = accountNumber;
        this.status = CardStatus.NORMAL;
    }

    public int getCardNumber() {
        return cardNumber;
    }

    public int getAccountNumber() {
        return accountNumber;
    }

    public CardStatus getStatus() {
        return status;
    }

    /** 吞卡：把卡的状态改为“已吞卡”，此后本机不再接受这张卡。 */
    public void retain() {
        this.status = CardStatus.RETAINED;
    }

    public boolean isRetained() {
        return status == CardStatus.RETAINED;
    }
}

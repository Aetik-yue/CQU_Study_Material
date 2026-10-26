package com.atm.hardware;

/**
 * 存款槽：负责接收用户投入的存款信封。
 */
public interface DepositSlot {

    /** 是否已收到存款信封。 */
    boolean isEnvelopeReceived();
}

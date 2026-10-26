package com.atm.hardware;

import com.atm.bank.BankCard;

/**
 * 读卡器：负责接管用户插入的银行卡。
 */
public interface CardReader {

    /**
     * 等待用户插卡并读取卡片信息。
     *
     * @return 读到的银行卡；用户放弃插卡（输入 0）时返回 null
     */
    BankCard readCard();

    /** 退卡：把卡片交还给用户，并提示取走。 */
    void ejectCard();

    /** 吞卡：把卡片收入机器（密码连续输错次数过多时执行）。 */
    void retainCard();
}

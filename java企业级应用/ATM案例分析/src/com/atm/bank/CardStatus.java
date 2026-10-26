package com.atm.bank;

/**
 * 银行卡状态。
 */
public enum CardStatus {
    /** 正常可用的卡。 */
    NORMAL("正常"),
    /** 因密码连续输错次数过多被机器吞卡。 */
    RETAINED("已吞卡");

    private final String label;

    CardStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

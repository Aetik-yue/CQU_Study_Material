package com.atm.ui;

/**
 * 主菜单选项。
 */
public enum MainMenuChoice {
    BALANCE_INQUIRY("查询余额"),
    HISTORY("交易流水"),
    WITHDRAWAL("取款"),
    DEPOSIT("存款"),
    TRANSFER("转账"),
    EXIT("退出");

    private final String label;

    MainMenuChoice(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

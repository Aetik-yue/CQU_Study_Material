package com.atm.hardware;

/**
 * 屏幕：只负责把文字与金额显示给用户，不参与任何业务判断。
 */
public interface Display {

    /** 显示一段文字，不换行（通常用于输入提示，用户输入后同一行接续）。 */
    void showMessage(String message);

    /** 显示一行文字。 */
    void showMessageLine(String message);

    /** 以统一的金额格式显示一行“标签：￥金额”。 */
    void showAmountLine(String label, double amount);
}

package com.atm.hardware;

/**
 * 小键盘：只负责把用户按键转换成输入数据。
 *
 * <p>输入设备只保证“格式合法”（是整数、是数字金额），
 * 业务规则（够不够、超没超限额）一律由交易类判断。</p>
 */
public interface Keypad {

    /** 读取一行文本。 */
    String readLine(String prompt);

    /** 读取一个整数；输入不是数字时提示后重新输入。 */
    int readInt(String prompt);

    /** 读取一个金额（十进制数字）；输入无法解析时提示后重新输入。 */
    double readAmount(String prompt);
}

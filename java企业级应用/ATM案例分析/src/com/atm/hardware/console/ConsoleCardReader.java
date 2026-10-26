package com.atm.hardware.console;

import com.atm.bank.BankCard;
import com.atm.hardware.CardReader;
import com.atm.hardware.Display;
import com.atm.hardware.Keypad;

import java.util.function.IntFunction;

/**
 * 读卡器的控制台实现：用户“插入”的卡用输入卡号来模拟。
 *
 * <p>读卡器不直接依赖银行数据库：查卡的动作以 {@code IntFunction<BankCard>}
 * 依赖注入进来（入口处传入 {@code bankDatabase::findCard}），
 * 硬件层与银行层因此保持解耦——这也是函数式接口与方法引用的实际用法。</p>
 */
public class ConsoleCardReader implements CardReader {

    private final Display display;
    private final Keypad keypad;
    private final IntFunction<BankCard> cardLookup;

    public ConsoleCardReader(Display display, Keypad keypad, IntFunction<BankCard> cardLookup) {
        this.display = display;
        this.keypad = keypad;
        this.cardLookup = cardLookup;
    }

    @Override
    public BankCard readCard() {
        while (true) {
            int cardNumber = keypad.readInt("请插入银行卡（输入卡号；输入 0 不插卡并退出系统）：");
            if (cardNumber == 0) {
                return null;
            }
            BankCard card = cardLookup.apply(cardNumber);
            if (card == null) {
                display.showMessageLine("无法识别的卡号，请重新插卡。");
                continue;
            }
            if (card.isRetained()) {
                display.showMessageLine("该卡已因密码错误过多被本机吞卡，无法使用，请联系银行网点。");
                continue;
            }
            display.showMessageLine("（读卡器）正在读取银行卡信息……");
            return card;
        }
    }

    @Override
    public void ejectCard() {
        display.showMessageLine("（读卡器）请取回您的银行卡。");
    }

    @Override
    public void retainCard() {
        display.showMessageLine("（读卡器）银行卡已收入吞卡槽。");
    }
}

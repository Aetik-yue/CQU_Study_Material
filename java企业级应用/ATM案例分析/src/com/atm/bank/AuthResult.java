package com.atm.bank;

/**
 * 密码认证结果。
 */
public enum AuthResult {
    /** 密码正确。 */
    SUCCESS,
    /** 密码错误，但还有机会重试。 */
    WRONG_PIN,
    /** 密码连续错误次数达到上限，卡片被吞。 */
    CARD_RETAINED
}

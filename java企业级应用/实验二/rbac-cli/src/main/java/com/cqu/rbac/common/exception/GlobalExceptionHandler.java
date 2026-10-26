package com.cqu.rbac.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 全局异常处理：把各类异常转换为"用户可读的一句话"，同时按级别写日志。
 * <p>约定：不向用户直接打印原始堆栈；未预期异常以 ERROR 级别记录完整堆栈到日志文件。</p>
 */
public final class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    /**
     * 文件专用日志：完整堆栈只写入 logs/rbac.log，控制台只保留一行友好提示。
     * （logback.xml 中 FATAL_FILE 仅绑定 FILE appender）
     */
    private static final Logger fileLog = LoggerFactory.getLogger("FATAL_FILE");

    private GlobalExceptionHandler() {
    }

    /**
     * 处理一般异常，返回给用户展示的友好文案。
     * <ul>
     *   <li>BusinessException / AuthException：业务可预期，原样返回 message，记 WARN</li>
     *   <li>DaoException：数据访问异常，记 ERROR（堆栈只进文件），返回脱敏文案</li>
     *   <li>其他：未预期异常，记 ERROR（堆栈只进文件），返回通用文案</li>
     * </ul>
     */
    public static String handle(Throwable e) {
        if (e instanceof BusinessException) {
            // 业务异常属于可预期范围，WARN 记录即可
            log.warn("业务异常[{}]: {}", ((BusinessException) e).getCode(), e.getMessage());
            return e.getMessage();
        }
        if (e instanceof DaoException) {
            fileLog.error("数据访问异常: {}", e.getMessage(), e);
            log.error("数据访问异常: {}", e.getMessage());
            return "数据访问异常，请检查数据库状态或查看日志文件 logs/rbac.log";
        }
        fileLog.error("未预期异常: {}", e.getMessage(), e);
        log.error("未预期异常: {}", e.getMessage());
        return "系统发生未预期异常：" + e.getMessage() + "（详情见日志文件）";
    }

    /**
     * 处理致命异常（程序无法继续运行）：完整堆栈写文件，控制台仅输出友好提示。
     */
    public static void handleFatal(Throwable e) {
        fileLog.error("程序发生致命错误，即将退出: {}", e.getMessage(), e);
        log.error("程序发生致命错误，即将退出: {}", e.getMessage());
        printFriendly(e);
    }

    /**
     * 输出友好提示（不打印堆栈）。
     */
    public static void printFriendly(Throwable e) {
        String msg;
        if (e instanceof BusinessException) {
            msg = e.getMessage();
        } else if (e instanceof DaoException) {
            msg = "数据库访问失败：" + e.getMessage()
                    + System.lineSeparator() + "请确认 MySQL 已启动、db.properties 中的账号密码正确。";
        } else {
            msg = "程序发生错误：" + e.getMessage() + "（详情见日志文件 logs/rbac.log）";
        }
        System.err.println(msg);
    }
}

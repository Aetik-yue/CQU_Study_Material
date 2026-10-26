package com.cqu.rbac.cli.session;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 会话级界面状态记忆（单例）：记住"上一次审计查询条件"等，减少重复输入。
 * <p>仅存于内存，不落库；进程退出即失效。</p>
 */
public final class UiState {

    private static final UiState INSTANCE = new UiState();

    private AuditFilter lastAuditFilter;

    private UiState() {
    }

    public static UiState getInstance() {
        return INSTANCE;
    }

    public AuditFilter getLastAuditFilter() {
        return lastAuditFilter;
    }

    public void setLastAuditFilter(AuditFilter filter) {
        this.lastAuditFilter = filter;
    }

    /**
     * 审计查询条件快照。
     */
    public static final class AuditFilter {
        private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("MM-dd HH:mm");

        private final String module;
        private final String username;
        private final String result;
        private final LocalDateTime from;
        private final LocalDateTime to;

        public AuditFilter(String module, String username, String result,
                           LocalDateTime from, LocalDateTime to) {
            this.module = module;
            this.username = username;
            this.result = result;
            this.from = from;
            this.to = to;
        }

        public String getModule() {
            return module;
        }

        public String getUsername() {
            return username;
        }

        public String getResult() {
            return result;
        }

        public LocalDateTime getFrom() {
            return from;
        }

        public LocalDateTime getTo() {
            return to;
        }

        /** 条件的可读描述 */
        public String describe() {
            StringBuilder sb = new StringBuilder();
            sb.append("模块=").append(module == null ? "全部" : module)
                    .append(" · 用户=").append(username == null ? "全部" : username)
                    .append(" · 结果=").append(result == null ? "全部"
                            : ("SUCCESS".equals(result) ? "成功" : "失败"));
            if (from != null || to != null) {
                sb.append(" · 时间=")
                        .append(from == null ? "不限" : from.format(TIME))
                        .append("~")
                        .append(to == null ? "不限" : to.format(TIME));
            }
            return sb.toString();
        }
    }
}

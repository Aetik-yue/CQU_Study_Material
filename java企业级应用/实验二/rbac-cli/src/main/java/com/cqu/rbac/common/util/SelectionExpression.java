package com.cqu.rbac.common.util;

import com.cqu.rbac.common.exception.BusinessException;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 选择表达式解析器：把"快捷输入"解析为 ID 集合，供授权/批量操作复用。
 *
 * <p>支持语法：</p>
 * <ul>
 *   <li>{@code 7,8,10} / {@code 7 8 10}：直接给出 ID（兼容原有输入）</li>
 *   <li>{@code 7-13}：ID 区间（含两端）</li>
 *   <li>{@code user:*}：按编码前缀匹配（需要提供 code→id 映射）</li>
 *   <li>{@code @menu} / {@code @op} / {@code @res}：按类型全选（需要提供 id→type 映射）</li>
 *   <li>{@code +5} / {@code -10}：在现有集合上增/删（首个 token 决定"增量模式"）</li>
 *   <li>{@code 0} / {@code clear} / {@code 清空}：清空</li>
 *   <li>{@code all}：全选（需调用方二次确认）</li>
 * </ul>
 *
 * <p>表达式非法时抛出带提示的 {@link BusinessException}，由界面层提示后重新输入。</p>
 */
public final class SelectionExpression {

    private static final Pattern RANGE = Pattern.compile("^(\\d+)-(\\d+)$");
    private static final Pattern NUMBER = Pattern.compile("^\\d+$");

    private SelectionExpression() {
    }

    /** 解析结果：最终集合 + 相对现有的新增/移除明细 */
    public static final class Selection {
        private final Set<Long> ids;
        private final Set<Long> added;
        private final Set<Long> removed;
        private final boolean unchanged;

        private Selection(Set<Long> ids, Set<Long> added, Set<Long> removed) {
            this.ids = ids;
            this.added = added;
            this.removed = removed;
            this.unchanged = added.isEmpty() && removed.isEmpty();
        }

        public Set<Long> getIds() {
            return ids;
        }

        public Set<Long> getAdded() {
            return added;
        }

        public Set<Long> getRemoved() {
            return removed;
        }

        /** 与现有集合相比是否没有变化 */
        public boolean isUnchanged() {
            return unchanged;
        }
    }

    /**
     * 解析选择表达式。
     *
     * @param expression 表达式
     * @param current    现有集合（增量模式与变更统计的基准）
     * @param universe   可选项全集（用于校验 ID 是否存在；为空表示不校验）
     * @param codeToId   编码 → ID（通配模式使用，可为空）
     * @param typeById   ID → 类型（@menu/@op/@res 使用，可为空）
     */
    public static Selection evaluate(String expression, Collection<Long> current,
                                     Collection<Long> universe, Map<String, Long> codeToId,
                                     Map<Long, String> typeById) {
        String expr = expression == null ? "" : expression.trim();
        if (expr.isEmpty()) {
            throw new BusinessException("E1101", "选择表达式不能为空（输入 ? 查看语法）");
        }
        Set<Long> currentSet = new LinkedHashSet<>(current == null ? Collections.emptyList() : current);
        Set<Long> universeSet = new LinkedHashSet<>(universe == null ? Collections.emptyList() : universe);

        if (expr.equals("0") || expr.equalsIgnoreCase("clear") || expr.equals("清空")) {
            Set<Long> removed = new LinkedHashSet<>(currentSet);
            return new Selection(new LinkedHashSet<>(), new LinkedHashSet<>(), removed);
        }

        String[] tokens = expr.split("[，,\\s]+");
        Set<Long> result = new LinkedHashSet<>();
        boolean modifyMode = tokens.length > 0
                && (tokens[0].startsWith("+") || tokens[0].startsWith("-"));
        if (modifyMode) {
            result.addAll(currentSet);
        }

        for (String raw : tokens) {
            if (raw.isEmpty()) {
                continue;
            }
            boolean remove = false;
            String token = raw;
            if (token.startsWith("+")) {
                token = token.substring(1).trim();
            } else if (token.startsWith("-")) {
                remove = true;
                token = token.substring(1).trim();
            }
            if (token.isEmpty()) {
                continue;
            }
            Set<Long> base = resolve(token, universeSet, codeToId, typeById);
            if (remove) {
                result.removeAll(base);
            } else {
                result.addAll(base);
            }
        }

        Set<Long> added = new LinkedHashSet<>();
        for (Long id : result) {
            if (!currentSet.contains(id)) {
                added.add(id);
            }
        }
        Set<Long> removed = new LinkedHashSet<>();
        for (Long id : currentSet) {
            if (!result.contains(id)) {
                removed.add(id);
            }
        }
        return new Selection(result, added, removed);
    }

    /** 解析单个 token 为 ID 集合 */
    private static Set<Long> resolve(String token, Set<Long> universe,
                                     Map<String, Long> codeToId, Map<Long, String> typeById) {
        String lower = token.toLowerCase();

        if (lower.equals("all")) {
            if (universe.isEmpty()) {
                throw new BusinessException("E1102", "当前场景没有可全选的项");
            }
            return new LinkedHashSet<>(universe);
        }

        // 精确编码（如 role:list）：最直观的写法
        if (codeToId != null && !codeToId.isEmpty()) {
            Long exactId = codeToId.get(token);
            if (exactId == null) {
                exactId = codeToId.get(lower);
            }
            if (exactId != null) {
                Set<Long> ids = new LinkedHashSet<>();
                ids.add(exactId);
                return ids;
            }
        }

        if (token.startsWith("@")) {
            String type = switch (lower.substring(1)) {
                case "menu" -> "MENU";
                case "op", "operation" -> "OPERATION";
                case "res", "resource" -> "RESOURCE";
                default -> throw new BusinessException("E1103",
                        "未知类型标记：" + token + "（可用 @menu / @op / @res）");
            };
            if (typeById == null || typeById.isEmpty()) {
                throw new BusinessException("E1104", "当前场景不支持按类型选择");
            }
            Set<Long> ids = new LinkedHashSet<>();
            typeById.forEach((id, t) -> {
                if (type.equals(t)) {
                    ids.add(id);
                }
            });
            if (ids.isEmpty()) {
                throw new BusinessException("E1105", "没有类型为 " + type + " 的可选项");
            }
            return ids;
        }

        if (token.endsWith(":*")) {
            if (codeToId == null || codeToId.isEmpty()) {
                throw new BusinessException("E1106", "当前场景不支持按编码通配");
            }
            String prefixWithColon = token.substring(0, token.length() - 2) + ":";
            Set<Long> ids = new LinkedHashSet<>();
            for (Map.Entry<String, Long> entry : codeToId.entrySet()) {
                if (entry.getKey().startsWith(prefixWithColon)) {
                    ids.add(entry.getValue());
                }
            }
            if (ids.isEmpty()) {
                throw new BusinessException("E1107", "没有匹配的编码：" + token);
            }
            return ids;
        }

        Matcher range = RANGE.matcher(token);
        if (range.matches()) {
            long from = Long.parseLong(range.group(1));
            long to = Long.parseLong(range.group(2));
            if (from <= 0 || to <= 0 || from > to) {
                throw new BusinessException("E1108", "ID 区间无效：" + token);
            }
            Set<Long> ids = new LinkedHashSet<>();
            for (long id = from; id <= to; id++) {
                if (universe.isEmpty() || universe.contains(id)) {
                    ids.add(id);
                }
            }
            if (ids.isEmpty()) {
                throw new BusinessException("E1109", "ID 区间内没有可选项：" + token);
            }
            return ids;
        }

        if (NUMBER.matcher(token).matches()) {
            long id = Long.parseLong(token);
            if (id <= 0) {
                throw new BusinessException("E1110", "ID 必须为正整数：" + token);
            }
            if (!universe.isEmpty() && !universe.contains(id)) {
                throw new BusinessException("E1111", "ID 不存在：" + id);
            }
            Set<Long> ids = new LinkedHashSet<>();
            ids.add(id);
            return ids;
        }

        throw new BusinessException("E1112", "无法识别的选择项：" + token);
    }

    /** 语法提示文本 */
    public static String syntaxHelp() {
        return "7,8,10（ID） · 7-13（区间） · user:list（精确编码） · user:*（编码前缀） · @menu/@op/@res（按类型） · +5/-10（增删） · 0 清空 · all 全选";
    }
}

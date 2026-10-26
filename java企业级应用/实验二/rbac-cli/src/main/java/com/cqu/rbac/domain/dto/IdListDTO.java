package com.cqu.rbac.domain.dto;

import com.cqu.rbac.common.exception.BusinessException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * id 列表 DTO：用于"给用户分配角色 / 给角色分配权限"这类多选操作的入参。
 * <p>同时提供从命令行文本（如 "1,2,3"）解析与校验的能力。</p>
 */
public class IdListDTO {

    /** 目标对象 id（如 userId / roleId） */
    private Long targetId;
    /** 待分配的 id 列表（已去重、保序） */
    private List<Long> ids = new ArrayList<>();

    public IdListDTO() {
    }

    public IdListDTO(Long targetId, List<Long> ids) {
        this.targetId = targetId;
        if (ids != null) {
            this.ids = new ArrayList<>(new LinkedHashSet<>(ids));
        }
    }

    public static IdListDTO of(Long targetId, List<Long> ids) {
        return new IdListDTO(targetId, ids);
    }

    /**
     * 解析命令行输入的 id 列表文本（逗号/中文逗号/空格分隔，允许空串表示"清空"）。
     *
     * @param text 如 "1,2,3" 或 "1，2 3"
     * @return id 列表（去重保序）
     * @throws BusinessException 存在非法数字时抛出
     */
    public static List<Long> parseIds(String text) {
        List<Long> result = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return result;
        }
        // 兼容中文逗号与空格分隔
        String[] tokens = text.trim().split("[，,\\s]+");
        Set<Long> seen = new LinkedHashSet<>();
        for (String token : tokens) {
            if (token.isEmpty()) {
                continue;
            }
            try {
                long id = Long.parseLong(token);
                if (id <= 0) {
                    throw new NumberFormatException("id 必须为正整数");
                }
                seen.add(id);
            } catch (NumberFormatException e) {
                throw new BusinessException("E1003", "无效的 id 列表：[" + token + "]，请输入正整数并用逗号分隔");
            }
        }
        result.addAll(seen);
        return result;
    }

    /** 是否为空列表（表示清空绑定） */
    public boolean isEmpty() {
        return ids == null || ids.isEmpty();
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids == null ? new ArrayList<>() : new ArrayList<>(new LinkedHashSet<>(ids));
    }

    @Override
    public String toString() {
        return "IdListDTO{targetId=" + targetId + ", ids=" + ids + "}";
    }
}

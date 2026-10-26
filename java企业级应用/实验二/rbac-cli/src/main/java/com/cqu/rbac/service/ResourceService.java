package com.cqu.rbac.service;

import com.cqu.rbac.domain.entity.Resource;

import java.util.List;

/**
 * 资源管理服务。
 * <p>权限注解 / 审计注解标注在实现类方法上。</p>
 */
public interface ResourceService {

    /** 查询全部资源 */
    List<Resource> listAll();

    /** 查询资源详情 */
    Resource get(Long id);

    /** 新增资源 */
    void create(Resource resource);

    /** 修改资源（编码不可修改） */
    void update(Resource resource);

    /** 删除资源（被权限关联时禁止） */
    void delete(Long id);

    /**
     * 校验某用户能否访问某资源（本质：用户是否拥有"关联该资源的 RESOURCE 权限"）。
     */
    boolean checkAccess(Long userId, String resourceCode);
}

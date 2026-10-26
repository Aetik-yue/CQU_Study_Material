package com.cqu.rbac.dao;

import com.cqu.rbac.domain.entity.Resource;

/**
 * 资源 DAO：单表 CRUD + 编码查询。
 */
public interface ResourceDao extends BaseDao<Resource, Long> {

    /** 按资源编码查询（唯一键） */
    Resource findByCode(String code);
}

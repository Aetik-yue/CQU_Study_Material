package com.cqu.rbac.dao;

import java.util.List;

/**
 * 泛型基础 DAO 接口：定义单表通用 CRUD 契约。
 * <p>泛型落地点：{@code BaseDao<T, ID>}——T 为实体类型，ID 为主键类型。</p>
 *
 * @param <T>  实体类型
 * @param <ID> 主键类型
 */
public interface BaseDao<T, ID> {

    /** 插入实体（成功后将回填自增主键到实体） */
    int insert(T entity);

    /** 按主键更新实体 */
    int update(T entity);

    /** 按主键删除 */
    int deleteById(ID id);

    /** 按主键查询，不存在返回 null */
    T findById(ID id);

    /** 查询全表 */
    List<T> findAll();

    /** 统计总行数 */
    long count();
}

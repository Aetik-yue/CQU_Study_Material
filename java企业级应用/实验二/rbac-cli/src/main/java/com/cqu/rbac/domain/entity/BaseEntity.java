package com.cqu.rbac.domain.entity;

import java.time.LocalDateTime;

/**
 * 实体基类（抽象类）：抽取所有实体公共的 id / createTime / updateTime。
 * <p>体现"继承"：全部实体继承本类，并配合多态在 DAO 泛型中统一处理。</p>
 */
public abstract class BaseEntity {

    /** 主键 */
    protected Long id;
    /** 创建时间 */
    protected LocalDateTime createTime;
    /** 更新时间 */
    protected LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}

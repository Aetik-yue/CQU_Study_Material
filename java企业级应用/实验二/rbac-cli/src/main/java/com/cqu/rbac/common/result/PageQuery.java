package com.cqu.rbac.common.result;

import com.cqu.rbac.common.constants.Constants;

/**
 * 分页查询参数：页码 + 每页大小，并提供参数归一化（防止非法页码/超大页）。
 */
public class PageQuery {

    private int page = 1;
    private int size = Constants.DEFAULT_PAGE_SIZE;

    public PageQuery() {
    }

    public PageQuery(int page, int size) {
        this.page = page;
        this.size = size;
        normalize();
    }

    public static PageQuery of(int page, int size) {
        return new PageQuery(page, size);
    }

    /** 归一化：页码至少为 1，页大小限制在 [1, MAX_PAGE_SIZE] */
    public void normalize() {
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = Constants.DEFAULT_PAGE_SIZE;
        }
        if (size > Constants.MAX_PAGE_SIZE) {
            size = Constants.MAX_PAGE_SIZE;
        }
    }

    /** SQL LIMIT 的偏移量 */
    public int getOffset() {
        return (page - 1) * size;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}

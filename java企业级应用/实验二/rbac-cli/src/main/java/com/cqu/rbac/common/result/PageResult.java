package com.cqu.rbac.common.result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 分页结果（泛型）：承载"当前页数据 + 总数 + 页码信息"。
 *
 * @param <T> 记录类型
 */
public class PageResult<T> {

    /** 当前页数据 */
    private List<T> records = new ArrayList<>();
    /** 满足条件的总记录数 */
    private long total;
    /** 当前页码（从 1 开始） */
    private int page = 1;
    /** 每页大小 */
    private int size = 10;

    public PageResult() {
    }

    public PageResult(List<T> records, long total, int page, int size) {
        this.records = records == null ? new ArrayList<>() : records;
        this.total = total;
        this.page = page;
        this.size = size;
    }

    /** 静态工厂：构造分页结果 */
    public static <T> PageResult<T> of(List<T> records, long total, int page, int size) {
        return new PageResult<>(records, total, page, size);
    }

    /** 空页结果 */
    public static <T> PageResult<T> empty(int page, int size) {
        return new PageResult<>(Collections.emptyList(), 0L, page, size);
    }

    /** 总页数 */
    public int getPages() {
        if (size <= 0) {
            return 0;
        }
        return (int) ((total + size - 1) / size);
    }

    /** 是否为空白页 */
    public boolean isEmpty() {
        return records == null || records.isEmpty();
    }

    /** 是否存在上一页/下一页 */
    public boolean hasPrevious() {
        return page > 1;
    }

    public boolean hasNext() {
        return page < getPages();
    }

    // ---------------- getter / setter ----------------

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
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

    @Override
    public String toString() {
        return "PageResult{total=" + total + ", page=" + page + ", size=" + size
                + ", pages=" + getPages() + ", records=" + records.size() + "}";
    }
}

package com.leyou.common.vo;

import lombok.Data;

import java.util.List;
@Data
public class PageResult<T> {
//由于该PageResult不一定封装品牌集合，所以要设计成一个泛型类
    private Long total;//总条数
    private Long totalPage;//总页数
    private List<T> items;//

    public PageResult() {
    }

    public PageResult(Long total, List<T> items) {
        this.total = total;
        this.items = items;
    }


    public PageResult(Long total, Long totalPage, List<T> items) {
        this.total = total;
        this.totalPage = totalPage;
        this.items = items;
    }
}

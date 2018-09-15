package com.leyou.search.pojo;

import com.leyou.common.vo.PageResult;
import com.leyou.pojo.Brand;
import com.leyou.pojo.Category;

import java.util.List;
import java.util.Map;

/**
 * 将该对象作为后台返回给前台的对象
 */
public class SearchResult extends PageResult<Goods> {
 //用于存储根据搜索结果，聚合有相同分类的集合
 private List<Category> categories;
 //存储根据搜索结果，聚合有相同品牌的集合
 private List<Brand> brands;
 private List<Map<String,Object>> specs;

    public List<Map<String, Object>> getSpecs() {
        return specs;

    }

    public void setSpecs(List<Map<String, Object>> specs) {
        this.specs = specs;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public List<Brand> getBrands() {
        return brands;
    }

    public void setBrands(List<Brand> brands) {
        this.brands = brands;
    }

    public SearchResult() {
    }

    public SearchResult(List<Category> categories, List<Brand> brands) {
        this.categories = categories;
        this.brands = brands;
    }

    public SearchResult(Long total, Long totalPage, List<Goods> items, List<Category> categories, List<Brand> brands) {
        super(total, totalPage, items);
        this.categories = categories;
        this.brands = brands;
    }

    public SearchResult(Long total, Long totalPage, List<Goods> items, List<Category> categories, List<Brand> brands, List<Map<String, Object>> specs) {
        super(total, totalPage, items);
        this.categories = categories;
        this.brands = brands;
        this.specs = specs;
    }
}

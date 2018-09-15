package com.leyou.api;

import com.leyou.pojo.Category;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
@RequestMapping("category")
public interface CategoryApi {
    @GetMapping("list")
    public List<Category> findCategory(@RequestParam("pid")Long pid);
    /**
     * 通过品牌id查询分类
     */
    @GetMapping("bid/{id}")//看前台的映射路径
    public List<Category> findCategorysBybid(@PathVariable("id")Long bid);

    /**
     * 通过分类ids去查分类集合
     */
    @GetMapping("list/ids")
    public List<Category> findCategorysBycids(@RequestParam("ids")List<Long>cids);
    @GetMapping("list/{cid3}")
    public List<Category> findCategorysBycid3(@PathVariable("cid3") Long cid3);

}

package com.leyou.controller;

import com.leyou.Service.CategoryService;
import com.leyou.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("category")
public class CategoryController {
    //category/list?pid=0
    @Autowired
    private CategoryService categoryService;

    /**
     * 根据父id查询分类集合
     * @param pid
     * @return
     */
    @GetMapping("list")
    public ResponseEntity<List<Category>> findCategory(@RequestParam("pid")Long pid){
       List<Category> list=categoryService.findCategory(pid);

       return ResponseEntity.ok(list);
    }
    /**
     * 通过品牌id查询分类
     */
    @GetMapping("bid/{id}")//看前台的映射路径
    public ResponseEntity<List<Category>> findCategorysBybid(@PathVariable("id")Long bid){
        List<Category> list=categoryService.findCategorysById(bid);
        ResponseEntity<List<Category>> ok = ResponseEntity.ok(list);
        return ok;
    }
    /**
     * 通过分类ids去查分类集合
     */
    @GetMapping("list/ids")
    public ResponseEntity<List<Category>> findCategorysBycids(@RequestParam("ids")List<Long>cids){
        List<Category> categories = categoryService.findCategoryByCids(cids);
       return ResponseEntity.ok(categories);
    }
    @GetMapping("list/{cid3}")
    public ResponseEntity<List<Category>> findCategorysBycid3(@PathVariable("cid3") Long cid3){
        List<Category>categories=categoryService.findCategorysBycid3(cid3);
        return ResponseEntity.ok(categories);
    }

}

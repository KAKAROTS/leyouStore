package com.leyou.search.controller;

import com.leyou.common.vo.PageResult;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SearchController {
    @Autowired
    private SearchService searchService;
    @PostMapping("page")
    public ResponseEntity<SearchResult> findGoodsByKey(@RequestBody SearchRequest searchRequest){
           SearchResult pageResult=searchService.findGoodsByKey(searchRequest);

        return ResponseEntity.ok(pageResult);
    }
    //规格参数名要跟局分类id从数据库中查，
    //规格参数的可选值要根据搜索结果的规格参数字段聚合
    //前台不仅需要拿到规格参数的名字还要那到规格参数的可选值，这个时候就不能只用map将其一一对应起来
    //而是用一个key对应一个规格参数名，一个key对应这个规格参数的可选值，放入同一个map中，这样一个map就是一个对象
    //而现在需要多个规格参数对象，就再使用一个list将其包裹
}

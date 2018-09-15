package com.leyou.controller;

import com.leyou.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Controller
public class PageController {
    @Autowired
    private PageService pageService;
    @GetMapping("item/{spuId}.html")
    public String findPage(@PathVariable("spuId")Long spuId,Model model){
         Map<String,Object> map= pageService.findSpu(spuId);
         //为了是的页面静态化不影响服务端渲染页面，所以选择开启一个新的线程来进行页面的静态化
         pageService.asynCreateHtml(spuId,map);
         model.addAllAttributes(map);
        return "item";
    }

}

package com.leyou.service;

import com.leyou.client.BrandClient;
import com.leyou.client.CategoryClient;
import com.leyou.client.GoodsClient;
import com.leyou.client.SpecificationClient;
import com.leyou.pojo.Brand;
import com.leyou.pojo.Category;
import com.leyou.pojo.SpecGroup;
import com.leyou.pojo.Spu;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class PageService {

    @Autowired
    private SpecificationClient specificationClient;
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private TemplateEngine templateEngine;
    @Value("${ly.page.destPath}")
    private String destPath;
    //创建一个线程池
    private static final ExecutorService es= Executors.newFixedThreadPool(20);

    public Map<String, Object> findSpu(Long spuId) {
       Spu spu = goodsClient.findSpuBySpuId(spuId);
        Brand brand = brandClient.findBrandByBid(spu.getBrandId());
        List<Category> categories = categoryClient.findCategorysBycid3(spu.getCid3());
        List<SpecGroup> specGroups = specificationClient.findSpecsByCid(spu.getCid3());
        HashMap<String, Object> map = new HashMap<>();
        map.put("skus",spu.getSkus());
        map.put("detail",spu.getSpuDetail());
        map.put("categories",categories);
        map.put("brand",brand);
        map.put("specs",specGroups);
        //防止数据重复
        spu.setSkus(null);
        spu.setSpuDetail(null);
        map.put("spu",spu);
        return map;
    }
    public void createHtml(Long id,Map<String,Object> data)  {
        //创建thymeleaf的数据中心
        Context context = new Context();
        context.setVariables(data);
        File file = getFilePath(id);
        //创建打印流
        try (PrintWriter printWriter = new PrintWriter(file)) {
            templateEngine.process("item",context,printWriter);
        }catch (Exception e){
            log.error("新增或更新商品{}",id,e);
            throw new RuntimeException("新增或更新商品失败");
        }
    }
    public void asynCreateHtml(Long id,Map<String,Object> data){
        //开启新线程
        es.execute(()->{
            try {
                createHtml(id,data);
            } catch (Exception e) {
                log.error("生成静态页面失败",e.getMessage());

            }
        });
    }
    public File getFilePath(Long id){
        File dir = new File(destPath);
        if(!dir.exists()){
            dir.mkdirs();
        }
        File file = new File(dir, id + ".html");
        return file;
    }

    public  void deleteHtml(Long spuId) {
        File file = getFilePath(spuId);
        if(file.exists()){
            file.delete();
        }
    }
}

package com.leyou.test;

import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.pojo.Spu;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.search.service.GoodsService;
import com.leyou.search.service.SearchService;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest()
public class EsTest {
    @Autowired
    private ElasticsearchTemplate estemplate;
    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SearchService searchService;
    @Autowired
    private GoodsService goodsService;

    /**
     * 用于创建索引库,并且创建文档及索引存入索引库
     */
    @Test
    public void creatIndex(){
        estemplate.createIndex(Goods.class);
        estemplate.putMapping(Goods.class);
        //要将spus从数据库中查询出来,调用微服务GoodsFeign中的findspus，但是这个方法要传入page,row,seable,key
        //所以一次查询只能查询部分商品，为了查询出所有商品，就要进行循环
        //先定义好初始参数
        int page=1;
        int rows=100;
        boolean saleable=true;
        String key=null;
        int size=0;
        do {
            //在这里面写查询逻辑
            //1.调用客户端查询数据库的spu数据
            PageResult<Spu> spus = goodsClient.findSpus(page, saleable, rows, key);
            //2.解析spus，将里面的数据封装到Goods对象中
            List<Spu> spuList = spus.getItems();
            if (CollectionUtils.isEmpty(spuList)){
                break;
                //throw new LyException("没有查到商品，检查请求路径", HttpStatus.BAD_REQUEST);
            }
            //ArrayList<Goods> goodslist = new ArrayList<>();
//            for (Spu spu : spuList) {
//                Goods goods=goodsService.DealSpu(spu);//
//                goodslist.add(goods);
//            }
            List<Goods> goodslist = spuList.stream().map(spu -> goodsService.DealSpu(spu)).collect(Collectors.toList());
            goodsRepository.saveAll(goodslist);
            size = spuList.size();
            page++;
        }while (size==100);

    }



}

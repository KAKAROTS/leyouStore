package com.leyou.controller;

import com.leyou.Service.GoodsService;
import com.leyou.common.vo.PageResult;
import com.leyou.pojo.Sku;
import com.leyou.pojo.Spu;
import com.leyou.pojo.SpuDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class GoodsController {
    //http://api.leyou.com/api/item/spu/page?key=&saleable=true&page=1&rows=5
    //要查询分页下spu商品，需要的参数有:
    // 1.当前页page，2.每页显示数量rows，3.该商品是否上架saleable,4.搜索关键字key
    @Autowired
    private GoodsService goodsService;

    /**
     * 分页查询spu，但是没有查询具体的sku及
     * @param page
     * @param saleable
     * @param rows
     * @param key
     * @return
     */
    @GetMapping("spu/page")
    public ResponseEntity<PageResult<Spu>> findSpus(
            @RequestParam(value = "page",defaultValue = "1") Integer page,
            @RequestParam(value = "saleable",required = false)Boolean saleable,
            @RequestParam(value = "rows",defaultValue = "5") Integer rows,
            @RequestParam(value = "key",defaultValue = "")String key){
        PageResult<Spu> pageResult=goodsService.findSpus(page,saleable,rows,key);

        return ResponseEntity.ok(pageResult);
    }
    @PostMapping("goods")
    public ResponseEntity<Void> saveSpu(@RequestBody Spu spu){
        //思路分析，http://api.leyou.com/api/item/goods
        //需要往spu表中添加数据，spu_detail表中添加，sku中添加，sku_detail(stock)中添加数据
        //需要的实体类有spu，spuDeail，sku，stock
        goodsService.saveSpu(spu);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @PutMapping("goods")
    public ResponseEntity<Void> updateSpu(@RequestBody Spu spu){
        //修改spu，直接更新spu表和spudetail表，至于sku与stock表需要将之前的删除然后再添加进去
        goodsService.updateSpu(spu);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    //http://api.leyou.com/api/item/spu/detail/2，根据spu的id去修改spu
    @GetMapping("spu/detail/{spuId}")
    public ResponseEntity<SpuDetail> findSpuDetail(@PathVariable("spuId")Long spuId){
        SpuDetail spuDetail=goodsService.findSpuDetail(spuId);
        return ResponseEntity.ok(spuDetail);
    }
    //http://api.leyou.com/api/item/sku/list?id=2
    //根据spuid查询出skus以及stock
    @GetMapping("sku/list")
    public ResponseEntity<List<Sku>> findSkus(@RequestParam("id")Long spuId){
        List<Sku> skus=goodsService.findSkus(spuId);
        return ResponseEntity.ok(skus);

    }
    @GetMapping("spu/{spuId}")
    public ResponseEntity<Spu> findSpuBySpuId(@PathVariable("spuId") Long spuId){
        Spu spu=goodsService.findSpuBySpuId(spuId);
        return ResponseEntity.ok(spu);

    }
    //Get /sku/list/ids 根据skuids查询sku
    @GetMapping("sku/list/ids")
    public ResponseEntity<List<Sku>> findSkusBySkuIds(@RequestParam("ids")List<Long> ids){
        List<Sku> skus= goodsService.findSkusBySkuId(ids);
        return  ResponseEntity.ok(skus);
    }
    //Put stock/decreased/skuIds
    @PutMapping("stock/decreased")
    public ResponseEntity<Void> decreasedSkuStock(@RequestBody Map<Long,Integer> map){
      goodsService.decreasedSkuStock(map);
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }


}

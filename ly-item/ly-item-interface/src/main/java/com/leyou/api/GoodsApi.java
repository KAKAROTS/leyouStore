package com.leyou.api;

import com.leyou.common.vo.PageResult;
import com.leyou.pojo.Sku;
import com.leyou.pojo.Spu;
import com.leyou.pojo.SpuDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

public interface GoodsApi {
    @GetMapping("spu/page")
    public PageResult<Spu> findSpus(
            @RequestParam(value = "page",defaultValue = "1") Integer page,
            @RequestParam(value = "saleable",required = false)Boolean saleable,
            @RequestParam(value = "rows",defaultValue = "5") Integer rows,
            @RequestParam(value = "key",defaultValue = "")String key);
    @PostMapping("goods")
    public ResponseEntity<Void> saveSpu(@RequestBody Spu spu);
    @PutMapping("goods")
    public ResponseEntity<Void> updateSpu(@RequestBody Spu spu);
    //http://api.leyou.com/api/item/spu/detail/2，根据spu的id去修改spu
    @GetMapping("spu/detail/{spuId}")
    public SpuDetail findSpuDetail(@PathVariable("spuId")Long spuId);
    //http://api.leyou.com/api/item/sku/list?id=2
    //根据spuid查询出skus以及stock
    @GetMapping("sku/list")
    public List<Sku> findSkus(@RequestParam("id")Long spuId);
    @GetMapping("spu/{spuId}")
    public Spu findSpuBySpuId(@PathVariable("spuId") Long spuId);
    @GetMapping("sku/list/ids")
    public List<Sku> findSkusBySkuIds(@RequestParam("ids")List<Long> ids);
    @PutMapping("stock/decreased")
    public ResponseEntity<Void> decreasedSkuStock(@RequestBody Map<Long,Integer> map);

}

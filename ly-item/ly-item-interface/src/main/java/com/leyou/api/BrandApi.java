package com.leyou.api;

import com.leyou.common.vo.PageResult;
import com.leyou.pojo.Brand;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("brand")
public interface BrandApi {
    @GetMapping("page")
    public PageResult<Brand> queryBrandByPage(
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "rows",defaultValue = "5")Integer rows,
            @RequestParam(value = "sortBy",required = false)String sortBy,
            @RequestParam(value = "desc",defaultValue = "false")Boolean desc,
            @RequestParam(value = "key",required = false)String key);
    // /item/brand

    @PostMapping("")
    public ResponseEntity<Void> insertBrand(Brand brand,@RequestParam("cids")List<Long> cids);
    /*
     *
     * 编辑品牌
     * */
    @PutMapping("")
    public ResponseEntity<Void> uploadBrandById(Brand brand,@RequestParam("cids")List<Long> cids);

    /**
     * 根据分类id查询品牌
     */
    @GetMapping("cid/{id}")
    public List<Brand> findBrandsByCid(@PathVariable("id")Long cid);
    /**
     * 根据品牌id查询品牌
     */
    @GetMapping("bid/{id}")
    public Brand findBrandByBid(@PathVariable("id")Long bid);
    @GetMapping("list")
    public List<Brand> fingdBrandsByBids(@RequestParam("ids")List<Long> bids);

}

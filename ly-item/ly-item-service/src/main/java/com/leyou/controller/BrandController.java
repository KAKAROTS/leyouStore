package com.leyou.controller;

import com.leyou.Service.BrandService;
import com.leyou.common.vo.PageResult;
import com.leyou.pojo.Brand;
import com.leyou.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("brand")
public class BrandController {
    //通过前台传来的数据分页查询品牌，需要的条件有
    //1.页码 2.每页的数量大小 3.排序字段 4.排序的方式，升序false或降序true 5.搜索的关键字
    //后台返回给前台的数据有 1.总条数 2.总页数 3.List<brand>集合,所以需要一个vo对象来封装这些数据
    //key// 搜索条件
    //page:// 当前页
    //rows: // 每页大小
    //sortBy: // 排序字段
    //desc: //排序方式
    @Autowired
    private BrandService brandService;
    @GetMapping("page")
    public ResponseEntity<PageResult<Brand>> queryBrandByPage(
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "rows",defaultValue = "5")Integer rows,
            @RequestParam(value = "sortBy",required = false)String sortBy,
            @RequestParam(value = "desc",defaultValue = "false")Boolean desc,
            @RequestParam(value = "key",required = false)String key){
        PageResult<Brand> pageResult=brandService.queryBrandByPage(page,rows,sortBy,desc,key);

        return ResponseEntity.ok(pageResult);
    }
    // /item/brand
    //添加品牌，需要品牌名称，品牌首字母，品牌对应的分类id
    //前台传来的数据是name，letter，image（暂时没有），cids
    @PostMapping("")
    public ResponseEntity<Void> insertBrand(Brand brand,@RequestParam("cids")List<Long> cids){
        brandService.insertBrand(brand,cids);
        return ResponseEntity.status(HttpStatus.CREATED).build();//这个时候返回的状态码是201，被前台接受会显示保存成功，
        //若是有异常就会被通用异常处理从而返回错误的状态码，前台能够接收就会显示保存失败
    }
    /*
    *
    * 编辑品牌
    * */
   @PutMapping("")
    public ResponseEntity<Void> uploadBrandById(Brand brand,@RequestParam("cids")List<Long> cids){
        brandService.updateBrandById(brand,cids);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    //@DeleteMapping("")
    public ResponseEntity<Void> deleteBrandById(@RequestParam("bid")Long bid){
        //怎么增加就怎么删除
        brandService.deleteBrandById(bid);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    /**
     * 根据分类id查询品牌
     * http://api.leyou.com/api/item/brand/cid/76
     */
    @GetMapping("cid/{id}")
    public ResponseEntity<List<Brand>> findBrandsByCid(@PathVariable("id")Long cid){
        List<Brand> list= brandService.findBrandsByCid(cid);
        return ResponseEntity.ok(list);
    }
    /**
     * 根据品牌id查询品牌
     */
    @GetMapping("bid/{id}")
    public ResponseEntity<Brand> findBrandByBid(@PathVariable("id")Long bid){
        Brand brand = brandService.findBrandByBid(bid);
        return ResponseEntity.ok(brand);
    }
    /**
     * 根据品牌ids查询品牌
     */
    @GetMapping("list")
    public ResponseEntity<List<Brand>> fingdBrandsByBids(@RequestParam("ids")List<Long> bids){
        List<Brand> brands=brandService.fingdBrandsByBids(bids);
        return ResponseEntity.ok(brands);

    }
}

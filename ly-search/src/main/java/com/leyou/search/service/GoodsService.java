package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.utils.NumberUtils;
import com.leyou.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.rmi.MarshalledObject;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GoodsService {
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private BrandClient  brandClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SpecificationClient specificationClient;

    /**
     * 将查到的spu进行解析封装至spu
     * @param spu
     * @return
     */
    public Goods DealSpu(Spu spu) {
        Goods goods = new Goods();
        goods.setSupId(spu.getId());
        goods.setBrandId(spu.getBrandId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setSubTitle(spu.getSubTitle());
        //根据品牌id查询出品牌
        Brand brand = brandClient.findBrandByBid(spu.getBrandId());
        //根据商品的分类id查询出分类，并获取商品的分类名
        List<Long> cids = Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3());
        List<Category> categories = categoryClient.findCategorysBycids(cids);
        List<String> cnames = categories.stream().map(category -> category.getName()).collect(Collectors.toList());
        //将title，分类名，品牌名称存入all字段
        String all=spu.getTitle()+" "+brand.getName()+" "+StringUtils.join(cnames," ");
        goods.setAll(all);
        //根据spuId查询出skus
        List<Sku> skus = goodsClient.findSkus(spu.getId());
        Set<Long> prices = skus.stream().map(sku -> sku.getPrice()).collect(Collectors.toSet());
        goods.setPrice(new ArrayList<>(prices));
        //将skus转化为一个json字符串,但是sku中有的属性是不需要的，要剔除掉，所以用一个map装sku中需要的属性
        //也就是将对象sku转化为map
        List<Map<String, Object>> list = new ArrayList<>();
        //遍历sku集合
        for (Sku sku : skus) {
            Map<String, Object> map = new HashMap<>();
            map.put("id",sku.getId());
            map.put("title",sku.getTitle());
            map.put("image",sku.getImages());
            map.put("price",sku.getPrice());
            list.add(map);
        }
        goods.setSkus(JsonUtils.serialize(list));
        //获取分类的规格参数
        //1.先根据分类id查询出有哪些规格参数
        //2.在根据spuid查询出spu中的规格参数的值
        //3.查询出规格参数后要判断该规格参数是特有规格参数还是通用规格参数
        List<SpecParam> specParams = specificationClient.findSpecParamsByGid(null, spu.getCid3(), null, null);
        SpuDetail spuDetail = goodsClient.findSpuDetail(spu.getId());
        String genericSpec = spuDetail.getGenericSpec();//通用规格参数的值，json字符串
        String specialSpec = spuDetail.getSpecialSpec();//特有规格参数的值，json字符串
        //要将json字符串转化为一个map，key是规格参数的id，value是规格参数值
        Map<Long, Object> genericSpecMap = JsonUtils.parseMap(genericSpec, Long.class, Object.class);
        //分析特有规格参数得知，json格式是以key：数组的形式存在，所以转化为map是，map的key是id，value是list
        Map<Long, List<Object>> specialSpecMap = JsonUtils.nativeRead(specialSpec, new TypeReference<Map<Long, List<Object>>>() {
        });
        Map<String, Object> specs = new HashMap<>();//用来装规格参数与值
        for (SpecParam specParam : specParams) {
            //判断该规格参数是特有还是通用
            Long id = specParam.getId();
            String name = specParam.getName();
            Object value=null;
            if(specParam.getGeneric()){
                //是通用的
                value=genericSpecMap.get(id);
                if(specParam.getNumeric()){
                    //为数值类型,规格参数的值作分段处理
                    value = chooseSegment(value.toString(), specParam);
                }
            }else{
                //是特有的
                value=specialSpecMap.get(id);

            }
            if(value==null){
                value="其他";
            }
            specs.put(name,value);
        }
         goods.setSpecs(specs);
        return goods;
    }

    /**
     *处理数值类型的参数，将数值参数进行分段
     * @param value
     * @param p
     * @return
     */
    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }
}

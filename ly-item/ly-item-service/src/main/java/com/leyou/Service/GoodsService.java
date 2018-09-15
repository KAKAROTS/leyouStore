package com.leyou.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.Mapper.SkuMapper;
import com.leyou.Mapper.SpuDetailMapper;
import com.leyou.Mapper.SpuMapper;
import com.leyou.Mapper.StockMapper;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.pojo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GoodsService {
    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private SpuDetailMapper spuDetailMapper;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private StockMapper stockMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;

    public PageResult<Spu> findSpus(Integer page, Boolean saleable, Integer rows, String key) {

        //分页
        PageHelper.startPage(page,rows);
        //过滤
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(key)){
            //key不为空
            criteria.andLike("title","%"+key+"%");
        }
        if (saleable!=null){
            //判断saleable是否为空,不为空也就是要根据上架或下架来查询
            criteria.andEqualTo("saleable",saleable);
        }
        List<Spu> list = spuMapper.selectByExample(example);//查出来的list集合并没有封装cname和bname
        //所以要遍历集合对数据重新封装
        dealList(list);
        //封装结果
        PageInfo<Spu> pageInfo = new PageInfo<>(list);
        return new PageResult<Spu>(pageInfo.getTotal(),pageInfo.getList());

    }
    public void dealList(List<Spu> list){

        if (CollectionUtils.isEmpty(list)){
            throw new LyException("资源路径错误", HttpStatus.NOT_FOUND);
        }
        for (Spu spu:list){
            //将cid1，cid2，cid3封装再一个list中
            List<Long> cids = Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3());
            //拿到spu的cids查询出cname，cname以/分开，然后再赋值给spu的cname
            //调用categoryservice根据ids查询的方法
            List<Category> categorys = categoryService.findCategoryByCids(cids);
            if (CollectionUtils.isEmpty(categorys)){
                throw new LyException("资源路径错误", HttpStatus.NOT_FOUND);
            }
            List<String> cnames = categorys.stream().map(category -> category.getName()).collect(Collectors.toList());
            spu.setCname(StringUtils.join(cnames,"/"));
            //然后再根据brand查出bname，然后再赋值给spu的bname
            Brand brand = brandService.findBrandByBid(spu.getBrandId());
            spu.setBname(brand.getName());
        }

    }

    /**
     * 添加一个spu
     * @param spu
     */
    @Transactional
    public void saveSpu(Spu spu) {
        //先往spu表中添加数据
        if(spu==null){
            throw new LyException("添加失败",HttpStatus.BAD_REQUEST);
        }
        spu.setSaleable(true);
        spu.setValid(true);
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        spuMapper.insert(spu);
        //接着往spu_detail中添加数据，但是要保持从刚保存的spu中获取id然后设置给spu_detail
        SpuDetail spuDetail = spu.getSpuDetail();
        Long spuId = spu.getId();
        spuDetail.setSpuId(spuId);
        spuDetailMapper.insert(spuDetail);
        //至此已经将信息添加至spu两张表了
        //接着要往sku表中添加数据了
        //首先获取skus集合，遍历集合，并添加spu_id
        saveSkuandStock(spu);
        //skuMapper.insertList(skus);//由于是一次添加很多数据，所以不会给sku回显id，一次添加的方法不行
        //向数据库添加数据完成后将spuid发送给rabbitmq
        sendSpuIdToRabbitMq(spu.getId(),"insert");

    }

    /**
     * 将spuid发送至消息队列
     * @param spuId
     */
    private void sendSpuIdToRabbitMq(Long spuId,String type) {
        //发送时指定交换机的名字与消息以及routekey，如果不指定交换机会使用默认的交换机
       try{
           amqpTemplate.convertAndSend("item."+type,spuId);
       } catch (Exception e){
           log.error("{}商品{}发送失败，请重试",type,spuId,e);//{}是占位符
           //由于处理了异常，为了让Spring知道发送产生了异常要重试，所以要再throw一个异常出去
           throw new RuntimeException("发送商品id失败");
       }
    }

    public SpuDetail findSpuDetail(Long spuId) {

           return spuDetailMapper.selectByPrimaryKey(spuId);

    }

    /**
     * 根据spuid查询sku集合
     * @param spuId
     * @return
     */
    public List<Sku> findSkus(Long spuId) {
        //先查询skubiao表
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        //select * from tb_sku s,tb_stock sk where spu_id=? and s.id=sk.sku_id;
       //List<Sku>skus= skuMapper.findSkus(spuId);
        List<Sku> skus = skuMapper.select(sku);
        List<Long> skuIds = skus.stream().map(sku1 -> sku1.getId()).collect(Collectors.toList());
        List<Stock> stocks = stockMapper.selectByIdList(skuIds);
        Map<Long, Integer> maps = new HashMap<>();//将sku_id与stock一一对应
        for (Stock stock:stocks){
            maps.put(stock.getSkuId(),stock.getStock());

        }
        for (Sku sku1:skus){
            sku1.setStock(maps.get(sku1.getId()));
        }
        if(CollectionUtils.isEmpty(skus)){
            throw new LyException("没有skus",HttpStatus.BAD_REQUEST);
        }
        return skus;
    }

    /**
     * 根据spu更新spu
     * @param spu
     */
    @Transactional
    public void updateSpu(Spu spu) {
       //更新spu表
        spu.setLastUpdateTime(new Date());
        spuMapper.updateByPrimaryKeySelective(spu);
       //更新spudetail表
        SpuDetail spuDetail = spu.getSpuDetail();
        spuDetailMapper.updateByPrimaryKey(spuDetail);
        //根据spu_id删除之前的sku及stock
        Long spuId = spu.getId();
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        List<Sku> skus = skuMapper.select(sku);
        if (CollectionUtils.isEmpty(skus)){
            throw new LyException("没有skus",HttpStatus.NOT_FOUND);
        }
        List<Long> skuIds = skus.stream().map(sku1 -> sku1.getId()).collect(Collectors.toList());
        skuMapper.delete(sku);
        stockMapper.deleteByIdList(skuIds);
        saveSkuandStock(spu);
        sendSpuIdToRabbitMq(spu.getId(),"update");

    }

    /**
     * 根据spu保存skus与stock
     * @param spu
     */
    public void saveSkuandStock(Spu spu){
        List<Sku> skus = spu.getSkus();//这个skus中的sku有stock属性
        Long spuId = spu.getId();
        if (CollectionUtils.isEmpty(skus)){
            throw new LyException("没有具体的商品可以添加",HttpStatus.BAD_REQUEST);
        }
        List<Stock> stocks = new ArrayList<>();
        for(Sku sku:skus){
            sku.setId(null);
            sku.setSpuId(spuId);
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            skuMapper.insert(sku);
            //接下来需要将stock集合添加进stock表,获取sku中的stock属性
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            stocks.add(stock);
    }
        stockMapper.insertList(stocks);
    }

    /**
     * 根据spuid查询spu
     * @param spuId
     * @return
     */
    public Spu findSpuBySpuId(Long spuId) {
        //查询spu的同时将spudetail与skus查出来
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(spuId);
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        List<Sku> skus = skuMapper.select(sku);
        spu.setSkus(skus);
        spu.setSpuDetail(spuDetail);
        return spu;
    }

    public List<Sku> findSkusBySkuId(List<Long> ids) {
        //查询sku集合
        List<Sku> skus = skuMapper.selectByIdList(ids);
        //查询库存集合
        List<Stock> stocks = stockMapper.selectByIdList(ids);
        //将stocks变成map
        Map<Long, Integer> map = new HashMap<>();
        for (Stock stock : stocks) {
            map.put(stock.getSkuId(),stock.getStock());
        }
        for (Sku sku : skus) {
            sku.setStock(map.get(sku.getId()));
        }

        return skus;

    }
    @Transactional
    public void decreasedSkuStock(Map<Long, Integer> map) {
         //因为这个时候会有并发的情况，所以要考虑线程安全问题
         //由于到时候要部署集群，所以在方法上加synchronized或lock都不能保证线程安全
        //只能在数据库上加锁，要么使用悲观锁或者乐观锁
        //使用悲观锁会影响读操作，影响性能
        //所以可以选择使用乐观锁，也就不需要先去查库存判断库存数量
        Set<Long> skuIds = map.keySet();
        for (Long skuId : skuIds) {
            int i = stockMapper.updateStock(skuId, map.get(skuId));
            if (i!=1){
                throw new LyException("库存不足",HttpStatus.BAD_REQUEST);
            }

        }

    }
}

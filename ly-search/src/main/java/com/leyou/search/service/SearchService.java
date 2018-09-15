package com.leyou.search.service;

import com.leyou.common.vo.PageResult;
import com.leyou.pojo.Brand;
import com.leyou.pojo.Category;
import com.leyou.pojo.SpecParam;
import com.leyou.pojo.Spu;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.search.repository.SearchRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Service;
import sun.rmi.runtime.Log;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SearchService {
    @Autowired
    private ElasticsearchTemplate estemplate;
    @Autowired
    private SearchRepository searchRepository;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private SpecificationClient specificationClient;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private GoodsRepository goodsRepository;

    /**
     * 根据搜索请求参数去查询商品
     * @param searchRequest
     * @return
     */
    public SearchResult findGoodsByKey(SearchRequest searchRequest) {
        //获取key和page,size
        String key = searchRequest.getKey();
        Integer page = searchRequest.getPage();
        Integer size = searchRequest.getSize();
        //1.创建条件构建器
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        //2.设置搜索方式
        //由于有些字段是不需要被查出来所以要过滤掉
        builder.withSourceFilter(new FetchSourceFilter(new String[]{"supId","subTitle","skus"},null));
        //设置查询方式和条件
        QueryBuilder queryBuilder= buildBasicQueryWithFilter(searchRequest);
        if(StringUtils.isNotBlank(searchRequest.getSortBy())){
            if(searchRequest.getDescending()==true){
        builder.withSort(SortBuilders.fieldSort(searchRequest.getSortBy()).order(SortOrder.DESC));
            }else {

            builder.withSort(SortBuilders.fieldSort(searchRequest.getSortBy()).order(SortOrder.ASC));
            }

        }
        //QueryBuilder queryBuilder = QueryBuilders.matchQuery("all", key);
        builder.withQuery(queryBuilder);
        //添加分页
        builder.withPageable(PageRequest.of(page-1,size));
        //添加聚合方式
        //2.1根据terms聚合，又根据brandid聚合与cid3聚合
        String categoryAgg="category_aggs";
        builder.addAggregation(AggregationBuilders.terms(categoryAgg).field("cid3"));
        String brandAgg="brand_aggs";
        builder.addAggregation(AggregationBuilders.terms(brandAgg).field("brandId"));
        //3.创建综合条件
        NativeSearchQuery query = builder.build();
        //4.搜索并对聚合结果进行解析
        //Page<Goods> goods = searchRepository.search(query);
        AggregatedPage<Goods> goods = estemplate.queryForPage(query, Goods.class);
        Aggregations aggregations = goods.getAggregations();
        List<Category> categories=handlerCategoryAgg(aggregations.get(categoryAgg));
        //判断分类集合的数量是否为一个，为一个就根据其id去数据库中查询相应的规格参数
        List<Map<String, Object>> specs=null;
        if(categories!=null&&categories.size()==1){
            //集合中数量为1，获取该分类并获取分类id
            Long cid = categories.get(0).getId();
            List<SpecParam> specParams = specificationClient.findSpecParamsByGid(null, cid, null, true);
             specs = handlerSpecs(specParams, queryBuilder);
        }
        List<Brand> brands=handlerBrandAgg(aggregations.get(brandAgg));
        List<Goods> goodsList = goods.getContent();
        long total= goods.getTotalElements();
        int totalPages = goods.getTotalPages();
        SearchResult searchResult = new SearchResult(total, (long) totalPages, goodsList, categories, brands,specs);
        return searchResult;
    }

    /**
     * 该方法用于处理品牌聚合
     * @param longTerms
     * @return
     */
    private List<Category> handlerCategoryAgg(LongTerms longTerms){
        List<LongTerms.Bucket> buckets = longTerms.getBuckets();
        List<Long> cids = buckets.stream().map(bucket -> bucket.getKeyAsNumber().longValue()).collect(Collectors.toList());
        List<Category> categories = categoryClient.findCategorysBycids(cids);
         return categories;

    }

    /**
     * 该方法用于处理品牌聚合
     * @param longTerms
     * @return
     */
    private List<Brand> handlerBrandAgg(LongTerms longTerms){
        List<LongTerms.Bucket> buckets = longTerms.getBuckets();
        List<Long> bids = buckets.stream().map(bucket -> bucket.getKeyAsNumber().longValue()).collect(Collectors.toList());
        List<Brand> brands = brandClient.fingdBrandsByBids(bids);
        return brands;
    }

    /**
     * 该方法用于处理规格参数，获取搜索结果的规格参数根据此聚合来封装过滤条件
     * @param specParams
     * @param queryBuilder
     * @return
     */
    private List<Map<String,Object>> handlerSpecs(List<SpecParam> specParams,QueryBuilder queryBuilder){
            //该方法用于再次对搜索结果进行聚合，然后获取结果中的值，再将key与值存储再list中
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        builder.withQuery(queryBuilder);
        builder.withPageable(PageRequest.of(0, 1));
        //一个文档对象也就是商品有很多个规格参数，要根据每一个规格参数设置聚合方式
        for(SpecParam specParam:specParams){
            //将规格参数名作为聚合名,规格参数名字段作为聚合字段，也就是相同的规格参数值会聚合到一个桶内，桶的key就是字段值
            builder.addAggregation(AggregationBuilders.terms(specParam.getName()).field("specs."+specParam.getName()+".keyword"));
        }
        AggregatedPage<Goods> goods = estemplate.queryForPage(builder.build(), Goods.class);
        //解析聚合结果获取规格参数值
        Aggregations aggregations = goods.getAggregations();
        List<Map<String, Object>> list = new ArrayList<>();
        for(SpecParam specParam:specParams){
        Map<String, Object> map = new HashMap<>();//一个map就是一个规格参数对象
            StringTerms stringTerms=aggregations.get(specParam.getName());
            //这个集合就是规格参数的所有值
            List<String> keys = stringTerms.getBuckets().stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());
            map.put("k",specParam.getName());
            map.put("options",keys);
            list.add(map);

        }
        return list;
    }

    /**
     * 根据请求参数创建基本搜索条件
      * @param request
     * @return
     */
    private QueryBuilder buildBasicQueryWithFilter(SearchRequest request){
        //对基本查询条件进行改造
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryBuilder.must(QueryBuilders.matchQuery("all", request.getKey()));
        Map<String, String> filter = request.getFilter();
        if (filter!=null&&filter.size()!=0){
        Set<String> keyset = filter.keySet();
            for (String key : keyset) {
                String value = filter.get(key);
                if(!"cid3".equals(key)&&!"brandId".equals(key)){
                    //key既不等于cid3也不等与brandId就要对key就行修改
                    key="specs."+key+".keyword";
                }
                queryBuilder.filter(QueryBuilders.termQuery(key,value));
            }
        }


        return queryBuilder;


    }


    public void IndexinsertOrUpdate(Long spuId) {
        //根据id调用fegin客户端从数据库中查找出spu
        //将spu进行处理成goods
        //然后将goods添加到索引库中，如果索引库中存在该商品就会执行更新操作，也就是先删除在添加，若果不存在就添加
        Spu spu = goodsClient.findSpuBySpuId(spuId);
        if (spu==null){
            //spu不存在，消费消息失败，抛出异常告诉Spring出一场了，不要进行消费者确认，让消息回滚继续留在队列中
            log.error("商品{}不存在",spuId);
            throw new RuntimeException("商品不存在");
        }
        Goods goods = goodsService.DealSpu(spu);
        goodsRepository.save(goods);

    }
}

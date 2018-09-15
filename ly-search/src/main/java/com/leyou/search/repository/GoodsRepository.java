package com.leyou.search.repository;

import com.leyou.search.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 用于将数据存入索引库
 */
public interface GoodsRepository extends ElasticsearchRepository<Goods,Long> {
}

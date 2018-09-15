package com.leyou.search.repository;

import com.leyou.search.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 用于搜索索引库中的数据
 */
public interface SearchRepository extends ElasticsearchRepository<Goods,Long> {
}

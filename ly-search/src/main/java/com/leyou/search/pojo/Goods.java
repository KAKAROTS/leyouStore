package com.leyou.search.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.List;
import java.util.Map;
@Document(indexName ="goods",type ="docs",shards =1,replicas =0)
@Data
public class Goods {
    @Id
    private Long supId;
    @Field(type = FieldType.Text,analyzer ="ik_max_word")
    private String all;//all字段用于存储商品的名称，标题及分类名
    @Field(type =FieldType.Keyword,index =false)
    private String subTitle;
    private Long brandId;
    private Long cid1;
    private Long cid2;
    private Long cid3;
    private Date createTime;
    private List<Long> price;//集合用于存储sku的价格
    @Field(type =FieldType.Keyword,index =false)
    private String skus;//skus是一个json字符串，为什么是json字符串而不是list集合呢,说是如果字段类型是对象就会用来过滤
    private Map<String,Object> specs;


}

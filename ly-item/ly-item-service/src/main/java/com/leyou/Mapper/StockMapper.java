package com.leyou.Mapper;

import com.leyou.pojo.Stock;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.additional.idlist.IdListMapper;
import tk.mybatis.mapper.additional.insert.InsertListMapper;
import tk.mybatis.mapper.common.Mapper;

public interface StockMapper extends Mapper<Stock>,InsertListMapper<Stock>,IdListMapper<Stock,Long>{
    @Update("update tb_stock set stock=stock-#{num} where sku_id=#{skuId} and stock>=#{num}")
    int updateStock(@Param("skuId")Long skuId,@Param("num")Integer num);
}

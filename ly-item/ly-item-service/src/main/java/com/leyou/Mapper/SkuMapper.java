package com.leyou.Mapper;

import com.leyou.pojo.Sku;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.additional.idlist.IdListMapper;
import tk.mybatis.mapper.additional.insert.InsertListMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SkuMapper extends Mapper<Sku>,InsertListMapper<Sku>,IdListMapper<Sku,Long> {
    @Select("select * from tb_sku s,tb_stock sk where spu_id=#{spuId} and s.id=sk.sku_id")
    List<Sku> findSkus(@Param("spuId") Long spuId);


}

package com.leyou.Mapper;

import com.leyou.pojo.Category;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.additional.idlist.IdListMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface CategoryMapper extends Mapper<Category>,IdListMapper<Category,Long> {
    @Select("select * from tb_category as c,tb_category_brand as cb where cb.brand_id=#{bid} and cb.category_id=c.id")
    List<Category> findCategorysById(@Param("bid") Long bid);

}

package com.leyou.Mapper;

import com.leyou.pojo.Brand;
import com.leyou.pojo.Category;
import org.apache.ibatis.annotations.*;
import tk.mybatis.mapper.additional.idlist.IdListMapper;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BrandMapper extends Mapper<Brand>,IdListMapper<Brand,Long>,SelectByIdListMapper<Brand,Long> {
    @Insert("insert into tb_category_brand values(#{cids},#{bid})")
public void insertCategoryBrand(@Param("cids")Long cids,@Param("bid")Long bid);
    @Delete("delete from tb_category_brand where brand_id=#{bid}")
    public void deleteCategoryBrand(@Param("bid") Long bid);
    @Select("SELECT b.* from tb_category_brand cb ,tb_brand b where cb.category_id=#{cid} and cb.brand_id=b.id")
    List<Brand> findBrandsByCid(@Param("cid")Long cid);
}

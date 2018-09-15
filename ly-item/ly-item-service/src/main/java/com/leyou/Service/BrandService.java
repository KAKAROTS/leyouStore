package com.leyou.Service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.Mapper.BrandMapper;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.pojo.Brand;
import com.leyou.pojo.Category;
import com.leyou.pojo.CategoryBrand;
import com.netflix.discovery.converters.Auto;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandService {
    @Autowired
    private BrandMapper brandMapper;
    public PageResult<Brand> queryBrandByPage(Integer page, Integer rows, String sortBy, Boolean desc, String key){
        //先使用分页助手进行分页设置
        PageHelper.startPage(page, rows);//相当于给sql语句设置了limit m,n
        Example example = new Example(Brand.class);//相当与sql语句中的where条件
        //过滤，判断是否是key关键字
        if(StringUtils.isNotBlank(key)){
            //如果有关键字key就设置条件
            Example.Criteria criteria = example.createCriteria();//设置where中的子条件
            criteria.orLike("name","%"+key+"%");
            criteria.orEqualTo("letter",key.toUpperCase());
        }
        //判断是否有排序字段
        if(StringUtils.isNotBlank(sortBy)){
            //排序字段不为空，则进行排序条件设置
            example.setOrderByClause(desc?sortBy+" desc":sortBy+" asc");//desc为ture，降序,false生序
        }
        //通过条件进行有条件的分页查询
        List<Brand> brands = brandMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(brands)){
            //查询的结果为空，抛出异常
            throw new LyException("没有该品牌数据", HttpStatus.NOT_FOUND);
        }
        //将查询结果传到PageInfo中
        PageInfo<Brand> pageInfo = new PageInfo<>(brands);

        return new PageResult<Brand>(pageInfo.getTotal(),pageInfo.getList());
    }

    public void insertBrand(Brand brand, List<Long> cids) {
        //这个时候的brand对象的id为null，但是位了防止前台不小心传了id所以将其设置为null
        brand.setId(null);
        brandMapper.insert(brand);
        //虽然品牌表中已经添加了数据，但是中间表tb_category_brand没有添加数据，所以需要在中间表中再次添加数据
        //两种方法，1.在mapper中写方法，写注解或mapper.xml文件 2.写中间表的实体类使用通用mapper直接添加
        for (Long cid:cids) {
            brandMapper.insertCategoryBrand(cid,brand.getId());
        }

        //第二种方法
//        CategoryBrand categoryBrand = new CategoryBrand();
//        categoryBrand.setCids(cids);
//        categoryBrand.setBid(brand.getId());
//        categorybrandMapper.insert(categoryBrand); 这个通用mapper还没创建
    }

    /*
    *
    * 根据ID删除品牌
    * */
    public void deleteBrandById(Long bid) {
       brandMapper.deleteByPrimaryKey(bid);
       brandMapper.deleteCategoryBrand(bid);

    }

    public void updateBrandById(Brand brand, List<Long> cids) {
        brandMapper.updateByPrimaryKey(brand);
        brandMapper.deleteCategoryBrand(brand.getId());
        for (Long cid:cids) {
            brandMapper.insertCategoryBrand(cid,brand.getId());
        }


    }
    public Brand findBrandByBid(Long bid){
        return brandMapper.selectByPrimaryKey(bid);
    }

    public List<Brand> findBrandsByCid(Long cid) {
        //brand表中并没有cid字段，所以要去中间表中查，通过中间遍查出的结果再与brand表的id关联
        List<Brand> list=brandMapper.findBrandsByCid(cid);
        if (CollectionUtils.isEmpty(list)){
            throw new LyException("没有这些品牌",HttpStatus.NOT_FOUND);
        }
        return list;
    }

    public List<Brand> fingdBrandsByBids(List<Long> bids) {
        List<Brand> brands = brandMapper.selectByIdList(bids);
        return brands;
    }
}

package com.leyou.Service;

import com.leyou.Mapper.CategoryMapper;
import com.leyou.common.exception.LyException;
import com.leyou.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
@Service
public class CategoryService {
    @Autowired
    private CategoryMapper mapper;
    public List<Category> findCategory(Long pid) {
        Category category = new Category();
        category.setParentId(pid);
        List<Category> list = mapper.select(category);
        if (list==null){
            throw new LyException("没有分类",HttpStatus.NOT_FOUND);
        }
        return list;
    }

    public List<Category> findCategorysById(Long bid) {
        List<Category> list=mapper.findCategorysById(bid);
        if (CollectionUtils.isEmpty(list)){
            throw new LyException("该商品没有分类",HttpStatus.NOT_FOUND);
        }
        return  list;
    }
    public List<Category> findCategoryByCids(List<Long> cids){
        List<Category> list = mapper.selectByIdList(cids);
        return list;
    }


    public List<Category> findCategorysBycid3(Long cid3) {
        Category c3 = mapper.selectByPrimaryKey(cid3);
        Category c2 = mapper.selectByPrimaryKey(c3.getParentId());
        Category c1 = mapper.selectByPrimaryKey(c2.getParentId());
        return Arrays.asList(c1,c2,c3);
    }
}

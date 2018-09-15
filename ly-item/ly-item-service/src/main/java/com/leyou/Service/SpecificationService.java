package com.leyou.Service;

import com.leyou.Mapper.SpecGroupMapper;
import com.leyou.Mapper.SpecParamMapper;
import com.leyou.common.exception.LyException;
import com.leyou.pojo.SpecGroup;
import com.leyou.pojo.SpecParam;
import javafx.scene.chart.NumberAxisBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class SpecificationService {
    @Autowired
    private SpecParamMapper specParamMapper;
    @Autowired
    private SpecGroupMapper specGroupMapper;

    /**
     * 这个方法中返回的specGroup不包含specparams
     * @param cid
     * @return
     */
    public List<SpecGroup> findSpecGroupsByCid(Long cid) {
        SpecGroup specGroup = new SpecGroup();
        specGroup.setCid(cid);
        List<SpecGroup> list = specGroupMapper.select(specGroup);
        if (CollectionUtils.isEmpty(list)){
            throw new LyException("没有规格组",HttpStatus.NOT_FOUND);
        }
        return list;

    }

    /**
     *通过gid或者cid去查规格参数表
     * @param gid
     * @param cid
     * @return
     */
    public List<SpecParam> findSpecParamsByGid(Long gid,Long cid,Boolean generic,Boolean searching) {
        SpecParam specParam = new SpecParam();
        specParam.setGroupId(gid);
        specParam.setCid(cid);
        specParam.setGeneric(generic);
        specParam.setSearching(searching);
        List<SpecParam> list = specParamMapper.select(specParam);
        if (CollectionUtils.isEmpty(list)){
            throw new LyException("该规格组下没有规格参数", HttpStatus.NOT_FOUND);
        }
        return list;

    }

    /**
     * 这个方法中的specgroup中包含specparams
     * @param cid
     * @return
     */
    public List<SpecGroup> findSpecsByCid(Long cid) {
        //1.调用findSpecGroupsByCid查询规格参数组
        //2.调用findSpecParamsByGid查询分类下规格参数
        //3.将规格参数按照规格组id分类至规格组中
        List<SpecGroup> specGroups = findSpecGroupsByCid(cid);
        List<SpecParam> specParams = findSpecParamsByGid(null, cid, null, null);
        //两种方法将参数分类至规格组中
        //1.两个for循环嵌套，不好
        //2.拆成两个for循环，使用一个map来装参数，key是规格组的id，value是具有相同规格组id的规格参数集合
        HashMap<Long, List<SpecParam>> map = new HashMap<>();
        //遍历规格参数集合
        for(SpecParam specParam:specParams){
            if(!map.containsKey(specParam.getGroupId())){
                //map中没有该key
               List<SpecParam> list = new ArrayList<>();
               list.add(specParam);
               map.put(specParam.getGroupId(),list);
            }else{
                //如果有就获得该集合，直接加
                map.get(specParam.getGroupId()).add(specParam);}
        }
        //接着遍历规格组集合
        for (SpecGroup specGroup : specGroups) {
            List<SpecParam> list = map.get(specGroup.getId());
            specGroup.setSpecParams(list);

        }
        return specGroups;
    }
}

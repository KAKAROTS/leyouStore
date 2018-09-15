package com.leyou.api;

import com.leyou.pojo.SpecGroup;
import com.leyou.pojo.SpecParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
@RequestMapping("spec")
public interface SpecificationApi {
    @GetMapping("groups/{cid}")
    public List<SpecGroup> findSpecGroupsByCid(@PathVariable("cid")Long cid);

    /**
     *
     * @param gid 通过规格组的id查找规格参数
     * @param cid 通过分类id查找规格参数
     * @return
     */
    @GetMapping("params")
    public List<SpecParam> findSpecParamsByGid(
            @RequestParam(value = "gid",required =false)Long gid,
            @RequestParam(value = "cid",required =false)Long cid,
            @RequestParam(value = "generic", required = false) Boolean generic,
            @RequestParam(value = "searching", required = false) Boolean searching

    );
    @GetMapping("{cid}")
    public List<SpecGroup> findSpecsByCid(@PathVariable("cid")Long cid);
}

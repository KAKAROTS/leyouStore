package com.leyou.controller;


import com.leyou.Service.SpecificationService;
import com.leyou.pojo.SpecGroup;
import com.leyou.pojo.SpecParam;
import com.leyou.pojo.Spu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("spec")
public class SpecificationController {
    @Autowired
    private SpecificationService specificationService;
    //http://api.leyou.com/api/item/spec/groups/3
    //要查找分类下规格组的信息，根据cid查找
    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> findSpecGroupsByCid(@PathVariable("cid")Long cid){
        List<SpecGroup> list=specificationService.findSpecGroupsByCid(cid);

        return ResponseEntity.ok(list);

    }

    /**
     *
     * @param gid 通过规格组的id查找规格参数
     * @param cid 通过分类id查找规格参数
     * @return
     */
    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> findSpecParamsByGid(
            @RequestParam(value = "gid",required =false)Long gid,
            @RequestParam(value = "cid",required =false)Long cid,
            @RequestParam(value = "generic", required = false) Boolean generic,
            @RequestParam(value = "searching", required = false) Boolean searching
    ){
        List<SpecParam> list=specificationService.findSpecParamsByGid(gid,cid,generic,searching);

        return ResponseEntity.ok(list);
    }
    @GetMapping("{cid}")
    public ResponseEntity<List<SpecGroup>> findSpecsByCid(@PathVariable("cid")Long cid){
        List<SpecGroup> list=specificationService.findSpecsByCid(cid);

        return ResponseEntity.ok(list);

    }



}

package com.leyou.pojo;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "tb_spu_detail")
@Data
public class SpuDetail {
    @Id
    @KeySql(useGeneratedKeys = true)
    private Long spuId;
    private String description;
    private String genericSpec;
    private String specialSpec;
    private String packingList;
    private String afterService;

}

package com.leyou.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;
import java.util.List;

@Data
@Table(name = "tb_spu")
public class Spu {
    @Id
    @KeySql(useGeneratedKeys = true)
private Long id;
private String title;
private String subTitle;
private Long cid1;
private Long cid2;
private Long cid3;
private Long brandId;
private Boolean saleable;
@JsonIgnore
private Boolean valid;
//@JsonIgnore
private Date createTime;
@JsonIgnore
private Date lastUpdateTime;
@Transient//加该注解是为了用该对象去查数据库的时候忽略该属性
private String cname;
@Transient
private String bname;
@Transient
    private List<Sku> skus;
@Transient
    private SpuDetail spuDetail;









}

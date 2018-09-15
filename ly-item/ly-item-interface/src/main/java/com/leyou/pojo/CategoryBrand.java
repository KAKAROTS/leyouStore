package com.leyou.pojo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Table;

@Data
@Table(name = "tb_category_brand")
public class CategoryBrand {
    @Column(name = "category_id")
    private Long cids;
    @Column(name = "brand_id")
    private Long bid;
}

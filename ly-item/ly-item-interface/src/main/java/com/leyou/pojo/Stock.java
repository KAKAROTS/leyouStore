package com.leyou.pojo;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "tb_stock")
@Data
public class Stock {
    @Id
    @KeySql(useGeneratedKeys = true)
    private Long skuId;
    private Integer seckill_stock;
    private Integer seckill_total;
    private Integer stock;
}

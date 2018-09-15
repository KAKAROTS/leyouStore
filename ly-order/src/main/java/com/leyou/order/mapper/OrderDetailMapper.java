package com.leyou.order.mapper;

import com.leyou.order.pojo.OrderDetail;
import tk.mybatis.mapper.additional.idlist.IdListMapper;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.special.InsertListMapper;

public interface OrderDetailMapper extends Mapper<OrderDetail>,IdListMapper<OrderDetail,Long>,InsertListMapper<OrderDetail>{
}

package com.leyou.order.mapper;

import com.leyou.order.pojo.OrderStatus;
import tk.mybatis.mapper.additional.idlist.IdListMapper;
import tk.mybatis.mapper.common.Mapper;

public interface OrderStatusMapper extends Mapper<OrderStatus>,IdListMapper<OrderStatus,Long> {
}

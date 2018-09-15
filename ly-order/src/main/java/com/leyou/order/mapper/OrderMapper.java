package com.leyou.order.mapper;

import com.leyou.order.pojo.Order;
import org.apache.ibatis.annotations.*;
import tk.mybatis.mapper.additional.idlist.IdListMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface OrderMapper extends Mapper<Order>,IdListMapper<Order,Long> {
    @Select("select o.* from tb_order o,tb_order_status os where o.user_id=#{userId} and o.order_id=os.order_id and os.`status`=#{status}")
    List<Order> findWaitOrders(@Param("userId") Long userId, @Param("status") Integer status);

    @Select("select o.* from tb_order o,tb_order_status os where o.user_id=#{userId} and o.order_id=os.order_id and os.`status`=#{status}")
    List<Order> findWaitSendOrders(@Param("userId")Long userId, @Param("status") Integer status);

    @Select("select o.* from tb_order o,tb_order_status os where o.user_id=#{userId} and o.order_id=os.order_id and os.`status`=#{status}")
    List<Order> findWaitReceiveOrders(@Param("userId")Long userId, @Param("status") Integer status);

    @Select("select o.* from tb_order o,tb_order_status os where o.user_id=#{userId} and o.order_id=os.order_id and os.`status`=#{status}")
    List<Order> findWaitEvaluateOrders(@Param("userId")Long userId,@Param("status")  Integer status);
}

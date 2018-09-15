package com.leyou.order.mapper;

import com.leyou.order.pojo.PayLog;
import tk.mybatis.mapper.additional.idlist.IdListMapper;
import tk.mybatis.mapper.common.Mapper;

public interface LogMapper extends Mapper<PayLog>,IdListMapper<PayLog,Long> {
}

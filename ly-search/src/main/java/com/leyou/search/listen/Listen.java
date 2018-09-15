package com.leyou.search.listen;

import com.leyou.search.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 监听类，在该类中编写消费方法，使得search微服务能够获得消息，作出相应的改变
 */
@Component
@Slf4j
public class Listen {
    @Autowired
    private SearchService searchService;
    //编写消费者方法，在消费者方法上设置队列，交换机，routekey
    @RabbitListener(bindings = @QueueBinding(
    value = @Queue(name ="ly.search.insert.queue",durable = "true"),
    exchange = @Exchange(name = "ly.item.exchange",type = ExchangeTypes.TOPIC),
    key={"item.insert","item.update"}
    ))
    public void saveGoodsOrUpdateGoods(Long spuId){
        if(spuId!=null){
            searchService.IndexinsertOrUpdate(spuId);

        }

    }

}

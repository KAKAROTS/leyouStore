package com.leyou.listen;

import com.leyou.service.PageService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class Listen {
    @Autowired
    private PageService pageService;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "ly.page.queue.insert",durable = "true"),
            exchange = @Exchange(name = "ly.item.exchange",type = ExchangeTypes.TOPIC),
            key = {"item.insert","item.update"}
    ))
    public void InsertOrUpdatePage(Long spuId){
        //在消费方法中消费消息，
        //根据id查出商品详细页面的信息
        if(spuId!=null) {
            Map<String, Object> map = pageService.findSpu(spuId);
            //调用创建静态的页面的方法
            pageService.createHtml(spuId, map);
        }
    }
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "ly.page.queue.delete",durable = "true"),
            exchange = @Exchange(name = "ly.item.exchange",type = ExchangeTypes.TOPIC),
            key = {"item.delte"}
    ))
    public void delete(Long spuId){
        if(spuId!=null){
        pageService.deleteHtml(spuId);}
    }
}

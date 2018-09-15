package com.leyou.mq;

import com.leyou.common.exception.LyException;
import com.leyou.config.Smsproperties;
import com.leyou.utils.SmsUtil;
import com.rabbitmq.client.AMQP;
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
    private SmsUtil smsUtil;
    @Autowired
    private Smsproperties smsproperties;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "ly.sms.queue",durable = "true"),
            exchange = @Exchange(name = "smsExchange",type = ExchangeTypes.TOPIC),
            key ={"user.sendmsg"}
    ))
    public void SendMsg(Map<String,String> msg){
           //判断msg是否为空
        if(msg==null){
            throw new LyException("手机号不能为空");
        }
        String phone = msg.get("phone");

        msg.remove("phone");
        smsUtil.sendSms(smsproperties.getSignName(),smsproperties.getVerifyCodeTemplate(),phone,msg);

    }


}

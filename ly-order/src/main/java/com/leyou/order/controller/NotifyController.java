package com.leyou.order.controller;

import com.leyou.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class NotifyController {
    @Autowired
    private OrderService orderService;
    //wxpay/notify
    /**
     * 微信通知支付成功的请求
     * 处理通知，并将订单状态及支付日志修改
     * 然后响应微信告知收到通知
     * 微信的通知请求参数是xml，所以要引入xml的消息转换器包，然后用map接收
     */
    @GetMapping("wxpay/notify")
    public ResponseEntity<String> NotifyHandler(@RequestBody Map<String,String> data){
        //到service层校验微信返回的信息
        orderService.handleNotify(data);
        //返回给微信的消息
        String msg="<xml>\n" +
                "\n" +
                "  <return_code><![CDATA[SUCCESS]]></return_code>\n" +
                "  <return_msg><![CDATA[OK]]></return_msg>\n" +
                "</xml>";
        return ResponseEntity.ok(msg);
    }
}

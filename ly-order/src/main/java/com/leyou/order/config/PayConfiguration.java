package com.leyou.order.config;

import com.leyou.order.utils.PayHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PayConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "ly.pay")
    public WXPayConfigImpl wxPayConfig(){
        return  new WXPayConfigImpl();
    }
    @Bean
    public PayHelper getPayHelper(WXPayConfigImpl wxPayConfig){
        return  new PayHelper(wxPayConfig);

    }
}

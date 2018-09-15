package com.leyou.order.config;

import com.github.wxpay.sdk.WXPayConfig;
import lombok.Data;

import java.io.InputStream;
@Data
public class WXPayConfigImpl implements WXPayConfig {
    private String appID;
    private String mchID;
    private String key;
    private int httpConnectTimeoutMs;
    private int httpReadTimeoutMs;
    private String notifyUrl;


    @Override
    public InputStream getCertStream() {
        return null;
    }


}

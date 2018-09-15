package com.leyou.cart.config;

import com.leyou.auth.utils.JwtUtils;
import com.leyou.auth.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.security.PublicKey;
@Data
@ConfigurationProperties("ly.jwt")
@Slf4j
public class JwtProperties {

    private String pubKeyPath;
    private String cookieName;
    private PublicKey publicKey;

    @PostConstruct
    public void init(){
        try {
             publicKey = RsaUtils.getPublicKey(pubKeyPath);
        } catch (Exception e) {
            log.error("初始化公钥失败！", e);
            throw new RuntimeException();
        }
    }




}

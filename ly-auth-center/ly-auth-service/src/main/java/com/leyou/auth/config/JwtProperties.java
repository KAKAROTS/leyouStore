package com.leyou.auth.config;

import com.leyou.auth.utils.RsaUtils;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

@Data
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties {


    private String secret;
    private String pubKeyPath;
    private String priKeyPath;
    private Integer expire;
    private String cookieName;
    private PublicKey publicKey; // 公钥

    private PrivateKey privateKey; // 私钥
    private static final Logger logger= LoggerFactory.getLogger(JwtProperties.class);
    @PostConstruct //在构造函数后使用该方法
    public void init()  {
        //判断公钥或私钥文件是否存在
        try {
            File prikey = new File(priKeyPath);

        File pubKey = new File(pubKeyPath);
        if(!prikey.exists()||!pubKey.exists()){
            //文件不存，根据该路径创建文件
            RsaUtils.generateKey(pubKeyPath,priKeyPath,secret);
        }
        //获取公钥和私钥
         this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
         this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        }catch (Exception e){
            logger.error("获取公钥或私钥失败",e);
            throw new RuntimeException();
        }

    }
}

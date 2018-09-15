package com.leyou.auth.service;

import com.leyou.auth.client.UserClinet;
import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.exception.LyException;
import com.leyou.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@EnableConfigurationProperties(JwtProperties.class)
public class AuthService {
    @Autowired
    private UserClinet userClinet;
    @Autowired
    private JwtProperties jwtProperties;

    public String login(String username, String password)  {
        //将提交的用户名和密码进行校验
        User user = userClinet.findUser(username, password);
        if(user==null){
            //为空，说明账户密码错误,没有token可以颁发
            return null;
        }
        //不为空，登陆成功，创建token给其授权
        UserInfo userInfo = new UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        String token = null;
        try {
            token = JwtUtils.generateTokenInMinutes(userInfo, jwtProperties.getPrivateKey(), jwtProperties.getExpire());
        } catch (Exception e) {
            //生成token失败，返回null
            return null;
        }
        return token;


    }
}

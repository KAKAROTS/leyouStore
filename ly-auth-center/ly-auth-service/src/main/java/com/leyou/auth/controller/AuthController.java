package com.leyou.auth.controller;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.service.AuthService;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.auth.utils.RsaUtils;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.CookieUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.PublicKey;
@Slf4j
@RestController
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {
    @Autowired
    private AuthService authService;
    @Autowired
    private JwtProperties jwtProperties;
    // post /login username,password
    @PostMapping("login")
    public ResponseEntity<Void> login(
            @RequestParam("username")String username,
            @RequestParam("password")String password,
            HttpServletRequest request, HttpServletResponse response){
        String token = authService.login(username, password);
        if(StringUtils.isBlank(token)){
            throw new LyException("账户或密码错误",HttpStatus.BAD_REQUEST);
        }
        //将token设置在cookie中,httpOnly()设置httponly为true表示js中不能获取cookie，request方法将域名设置在cookie
        CookieUtils.newBuilder(response).httpOnly().request(request).build(jwtProperties.getCookieName(),token);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * http://api.leyou.com/api/auth/verify
     */
    @GetMapping("verify")
    public ResponseEntity<UserInfo> verifyToken(@CookieValue("LY_TOKEN")String token,
            HttpServletRequest request, HttpServletResponse response){

        //  获取cookie中的token然后用公钥进行解密，解密成功说明是正确的token
        UserInfo userInfo = null;
        try {
            PublicKey publicKey = RsaUtils.getPublicKey(jwtProperties.getPubKeyPath());
            userInfo = JwtUtils.getInfoFromToken(token, publicKey);
            //为了防止token过期，所以鉴权完后要刷新token，也就是重新发送一个token,重新生成一个token
            token = JwtUtils.generateTokenInMinutes(userInfo, jwtProperties.getPrivateKey(), jwtProperties.getExpire());
            CookieUtils.newBuilder(response).httpOnly().request(request).build(jwtProperties.getCookieName(),token);

        } catch (Exception e) {
            log.error("token解密失败",HttpStatus.UNAUTHORIZED);
            throw new LyException("登陆失败，账号或密码错误",HttpStatus.UNAUTHORIZED);
        }

        return ResponseEntity.ok(userInfo);

    }

}

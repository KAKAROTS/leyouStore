package com.leyou.order.interceptor;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.order.config.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 创建拦截器对象
 */
@Slf4j
public class LoginInterceptor implements HandlerInterceptor{


    private JwtProperties jwtProperties;
    private static final ThreadLocal<UserInfo> tl=new ThreadLocal<>();
    public LoginInterceptor(JwtProperties jwtProperties){
        this.jwtProperties=jwtProperties;
    }
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        //前置拦截器，为true表示放行
        //获取cookie中的token
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
        //使用公钥对token进行解密
        try {
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
            tl.set(userInfo);
            return true;
        } catch (Exception e) {
            log.error("用户未登陆",e);
            return false;
        }
        //将获取到的user对象放置于request对象中或者threadlocal中，我们选择放在threadlocal


    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        tl.remove();
    }

    public static UserInfo getUserInfo(){
       return tl.get();
    }
}

package com.leyou.cart.config;

import com.fasterxml.jackson.databind.annotation.JsonAppend;
import com.leyou.cart.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class MvcConfig implements WebMvcConfigurer {
    //配置类用于注册登陆拦截器
    @Autowired
     private JwtProperties jwtProperties;
    //是在这里要Spring注入拦截器呢还是自己new呢？当然不能自己注入，因为该配置类都还没被创建，怎么会有拦截器在容器中呢
    //自己去new对象的化，对象中的属性就会为空，spring容器不会帮你注入，所以，在拦截器中不能使用autowired
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor(jwtProperties)).addPathPatterns("/**");
    }
}

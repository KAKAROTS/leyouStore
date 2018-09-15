package com.leyou.filter;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.CookieUtils;
import com.leyou.config.FilterProperties;
import com.leyou.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.util.List;

/**
 * @author xiongzixuan
 */
@Component
@Data
@EnableConfigurationProperties({JwtProperties.class, FilterProperties.class})
public class AuthFilter extends ZuulFilter {
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private FilterProperties filterProperties;
    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        //todo 为什么将该过滤器放于这个过滤器前
        return FilterConstants.PRE_DECORATION_FILTER_ORDER-1;
    }

    @Override
    public boolean shouldFilter() {
        //获取request对象
        RequestContext ctx = RequestContext.getCurrentContext();
        String uri = ctx.getRequest().getRequestURI();
        //获取允许放行的路径列表
        List<String> allowPaths = filterProperties.getAllowPaths();
        for (String allowPath : allowPaths) {
            //判断请求路径是否以允许的路径开头
            if(uri.startsWith(allowPath)){
                //是允许的，放行
                return false;
            }
        }
        //返回true表示拦截
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        //过滤逻辑
        //获取公钥
        PublicKey publicKey = jwtProperties.getPublicKey();
        //获取cookie中的token
        RequestContext ctx = RequestContext.getCurrentContext();
        String token = CookieUtils.getCookieValue(ctx.getRequest(), jwtProperties.getCookieName());
        //解密token
        try {
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, publicKey);
        } catch (Exception e) {
            //设置不发送响应，也就是拦截下来不请求
            ctx.setSendZuulResponse(false);
            //设置响应回去的状态吗
            ctx.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
        }


        return null;
    }
}

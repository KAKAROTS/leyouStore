package com.leyou.cart.service;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.cart.interceptor.LoginInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    private static final String KEY_PREFIX = "ly:cart:uid:";

    public void addCart(Cart cart) {
        //获取用户信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        //判断用户是否为空，如果为空就是未登陆，添加毛线啊
        if(userInfo==null){
            throw new LyException("用户未登陆", HttpStatus.BAD_REQUEST);
        }
        String key=KEY_PREFIX+userInfo.getId().toString();
        String skuId = cart.getSkuId().toString();
        Integer num = cart.getNum();
        //获取用户id，将其作为redis的key，由于一个用户下可以多个购物车，为了查找购物车方便将购物车的id也作为key，cart作为值
        //这样就是redis的value就是一个hash对应java中的map
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
        //需要判断的是添加的购物车是否存在
        if(hashOps.hasKey(skuId)){
            //存在就获取原数据
            String oldcart = hashOps.get(skuId).toString();
            cart = JsonUtils.parse(oldcart, Cart.class);
            cart.setNum(cart.getNum()+num);
        }
        //添加购物车
        hashOps.put(skuId,JsonUtils.serialize(cart));


    }

    public List<Cart> findCartsByUid() {
        //获取用户id
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        if(userInfo==null){
            throw new LyException("用户未登陆", HttpStatus.BAD_REQUEST);
        }
        String key = KEY_PREFIX+userInfo.getId().toString();

        //从redis中获取购物车
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
        List<Object> carts = hashOps.values();
        //判断集合是否为空
        if(CollectionUtils.isEmpty(carts)){
            //为空抛一场
            return null;
        }
        List<Cart> cartss = carts.stream().map(o -> { return JsonUtils.parse(o.toString(), Cart.class); }).collect(Collectors.toList());
        return cartss;


    }


    public void editCart(String skuId,Integer num) {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        //判断用户是否为空，如果为空就是未登陆，添加毛线啊
        if(userInfo==null){
            throw new LyException("用户未登陆", HttpStatus.BAD_REQUEST);
        }
        String key=KEY_PREFIX+userInfo.getId().toString();

        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
        if(hashOps.hasKey(skuId)){
            //存在就获取原数据
            String oldcart = hashOps.get(skuId).toString();
            Cart cart = JsonUtils.parse(oldcart, Cart.class);
            cart.setNum(num);
        hashOps.put(skuId,JsonUtils.serialize(cart));
        return;
        }
        throw new LyException("商品不存在",HttpStatus.BAD_REQUEST);

    }
}

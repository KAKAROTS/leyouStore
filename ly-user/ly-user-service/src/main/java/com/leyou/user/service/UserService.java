package com.leyou.user.service;

import com.leyou.common.exception.LyException;
import com.leyou.common.utils.NumberUtils;
import com.leyou.pojo.User;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.utils.CodecUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private StringRedisTemplate redisTemplate;
    private static final String PREFIX_KEY="user:verfiy:phone";
    /**
     * 校验手机号或者用户名的逻辑
     * @param data
     * @param type
     * @return
     */
    public Boolean CheckData(String data, Integer type) {
        if(StringUtils.isBlank(data)){
            throw new LyException("参数不能为空",HttpStatus.BAD_REQUEST);
        }
        User user = new User();
        switch(type){
            case 1:
                 user.setUsername(data);
                 break;
            case 2:
                 if(!data.matches("1[3456789]\\d{9}")){
                  throw new LyException("参数有误", HttpStatus.BAD_REQUEST);
                 }
                 user.setPhone(data);
                 break;
            default:
                throw new LyException("参数有误", HttpStatus.BAD_REQUEST);

        }
        int i = userMapper.selectCount(user);

         //没有该用户返回true，有该用户返回false
        return i==0;

    }

    public void sendMsg(String phone) {
        //准备好要存入redis中的key，使用前缀加手机号的形式，单用手机号dangzuokey容易冲突
        String key=PREFIX_KEY+phone;
        //判断redis中是否还有该key，如果有则表面已经生成过验证码并且还没过期，所以不能在生成验证码
        if(redisTemplate.hasKey(key)){
            throw new LyException("发送验证码过于频繁");
        }
        //校验手机号的正确性
        if(!phone.matches("1[3456789]\\d{9}")){
            throw new LyException("参数有误", HttpStatus.BAD_REQUEST);
        }
        //生成验证码
        String code = NumberUtils.generateCode(6);
        //将验证码存入redis中
        redisTemplate.opsForValue().set(key,code,2l, TimeUnit.MINUTES);
        //给消息队列发送消息
        Map<String, String> map = new HashMap<>();
        map.put("phone",phone);
        map.put("code",code);
        amqpTemplate.convertAndSend("user.sendmsg",map);


    }

    public void registerUser(User user, String code) {
        user.setId(null);
        //先从redis中获取验证码
        String key=PREFIX_KEY+user.getPhone();
        //将验证码与前台传来的比较，相等就注册，不等就抛异常
        String rediscode = redisTemplate.opsForValue().get(key);
        if (!rediscode.equals(code)){
            throw new LyException("验证码不正确",HttpStatus.BAD_REQUEST);
        }
        //相等，向数据库中注入数据
        user.setCreated(new Date());
        //获取用户输入的密码，对该密码进行加密，使用md5加密，不过其中要添加盐
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);
        String password = CodecUtils.md5Hex(user.getPassword(), salt);
        user.setPassword(password);
        int insert = userMapper.insert(user);
        if(insert!=1){
            //注册失败
            throw new LyException("参数有误",HttpStatus.BAD_REQUEST);
        }
        redisTemplate.delete(key);


    }

    public User login(String username, String password) {
        //根据用户名找用户，能找到在比对密码，找不到就是用户名错误，抛异常说账户名或密码错误
        User user = new User();
        user.setUsername(username);
        User user1 = userMapper.selectOne(user);
        if(user1==null){
            throw new LyException("用户名或密码错误",HttpStatus.BAD_REQUEST);
        }
        //先从用户那获取盐，将盐和密码混合加密后与用户那的密码对比
        String salt = user1.getSalt();
        String passw = CodecUtils.md5Hex(password, salt);
        if(!user1.getPassword().equals(passw)){
            throw new LyException("用户名或密码错误",HttpStatus.BAD_REQUEST);

        }

        return user1;
    }

}

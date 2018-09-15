package com.leyou.common.advice;

import com.leyou.common.exception.LyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/*
*
* 用于统一处理异常，原理是aop，原本是使用@Aspect，@pointcut，（@Before等通知）
* 来对controller层抛出的异常进行处理增强，但是Spring帮我们进行了整理，可以使用@ControllerAdvice注解来标记
* 该通知需要往拿切，往有controller注解的类上切，然后使用ExceptionHandler注解在一个方法上来接收异常并对异常进行处理
*要使用这两个注解就要引入Spring-web的依赖
* */
@ControllerAdvice
public class ExceptionHandler1 {
    //返回值为一个响应实体，也就对响应协议的封装，包含响应行，响应头，响应体
    private Logger log= LoggerFactory.getLogger(ExceptionHandler1.class);
    @ExceptionHandler(Exception.class)//class对象用于接收异常
    public ResponseEntity<String> handler(Exception e){
        log.error(e.getMessage(),e);
        if (e instanceof LyException){
            return ResponseEntity.status(((LyException) e).getHttpStatus()).body(e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("未知错误");
    }
}

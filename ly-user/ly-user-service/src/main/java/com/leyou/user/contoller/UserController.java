package com.leyou.user.contoller;

import com.leyou.pojo.User;
import com.leyou.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
public class UserController {
    @Autowired
    private UserService userService;
    //   Get /check/{data}/{type} 异步校验用户名或者手机号
    @GetMapping("check/{data}/{type}")
    public ResponseEntity<Boolean> CheckData(@PathVariable("data")String data,@PathVariable("type")Integer type){
         Boolean flag=userService.CheckData(data,type);
        return ResponseEntity.ok(flag);
    }

    //   POST /code
    @PostMapping("code")
    public ResponseEntity<Void> sendMsg(@RequestParam("phone")String phone){
        userService.sendMsg(phone);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    //   POST /register
    @PostMapping("register")
    public ResponseEntity<Void> registerUser(@Valid User user, @RequestParam("code")String code){
        userService.registerUser(user,code);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    // GET /query 根据用户名和密码查询用户
    @GetMapping("query")
    public ResponseEntity<User> findUser(@RequestParam("username")String username,@RequestParam("password")String password){
        User user=userService.login(username,password);
      return ResponseEntity.ok(user);
    }

}

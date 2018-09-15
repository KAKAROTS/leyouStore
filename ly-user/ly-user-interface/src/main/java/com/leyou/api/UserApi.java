package com.leyou.api;

import com.leyou.pojo.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

public interface UserApi {
    @GetMapping("query")
    public User findUser(@RequestParam("username")String username, @RequestParam("password")String password);
    @PostMapping("register")
    public Void registerUser(@Valid User user, @RequestParam("code")String code);
    @PostMapping("code")
    public Void sendMsg(@RequestParam("phone")String phone);
    @GetMapping("check/{data}/{type}")
    public Boolean CheckData(@PathVariable("data")String data, @PathVariable("type")Integer type);
}

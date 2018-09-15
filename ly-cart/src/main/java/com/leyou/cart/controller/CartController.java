package com.leyou.cart.controller;

import com.leyou.cart.pojo.Cart;
import com.leyou.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.List;

@Controller
public class CartController {

     @Autowired
    private CartService cartService;
     @PostMapping()
    public ResponseEntity<Void> addCart(@RequestBody Cart cart){
         cartService.addCart(cart);
         return ResponseEntity.status(HttpStatus.CREATED).build();

     }
     @GetMapping("list")
    public ResponseEntity<List<Cart>> findCartsByUid(){
         List<Cart> carts=cartService.findCartsByUid();
         return ResponseEntity.ok(carts);

     }
     //http://api.leyou.com/api/cart
    @PutMapping()
    public ResponseEntity<Void> editCart(@RequestParam("id")String skuid,@RequestParam("num")Integer num){

        cartService.editCart(skuid,num);
         return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }

}

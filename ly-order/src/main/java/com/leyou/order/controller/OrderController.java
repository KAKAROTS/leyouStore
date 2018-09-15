package com.leyou.order.controller;

import com.leyou.common.vo.PageResult;
import com.leyou.order.dto.OrderDTO;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.SearchRequest;
import com.leyou.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("order")
    public ResponseEntity<Long> creatOrder(@RequestBody OrderDTO orderDTO){
       Long orderId= orderService.creatOrder(orderDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(orderId);
    }
    @GetMapping("order/url/{orderId}")
    public ResponseEntity<String> getPayCodeUrl(@PathVariable("orderId")Long OrderId){
       String CodeUrl= orderService.getPayCodeUrl(OrderId);
       return ResponseEntity.ok(CodeUrl);
    }
    @GetMapping("order/{orderId}")
    public ResponseEntity<Order> findOrderByOrderId(@PathVariable("orderId")Long OrderId){
        Order order=orderService.findOrderByOrderId(OrderId);
        return ResponseEntity.ok(order);

    }
    //http://api.leyou.com/api/order-service/order/state/1037679858177675264
    /**
     * 根据订单查询支付状态
     */
    @GetMapping("order/state/{orderId}")
    public ResponseEntity<Integer> findOrderStatus(@PathVariable("orderId")Long orderId){
        Integer status=orderService.findOrderStatus(orderId);
        return ResponseEntity.ok(status);
    }
    /**
     * /order-service/order/
     * 根据用户id及搜索条件查询用户的订单集合
     */
    @PostMapping("orders")
    public ResponseEntity<PageResult<Order>> findOrders(@RequestBody SearchRequest request){
        PageResult<Order> result=orderService.findOrders(request);
        return ResponseEntity.ok(result);

    }
    /**
     * 根据用户id查询处待付款的订单
     * order-service/orders/wait
     */
    @PostMapping("orders/wait")
    public ResponseEntity<PageResult<Order>> findWaitOrders(@RequestBody SearchRequest request){
        PageResult<Order> result=orderService.findWaitOrders(request);
        return ResponseEntity.ok(result);
    }
    @PostMapping("orders/send")
    public ResponseEntity<PageResult<Order>> findWaitSendOrders(@RequestBody SearchRequest request){
        PageResult<Order> result=orderService.findWaitSendOrders(request);
        return ResponseEntity.ok(result);
    }
    @PostMapping("orders/receive")
    public ResponseEntity<PageResult<Order>> findWaitReceiveOrders(@RequestBody SearchRequest request){
        PageResult<Order> result=orderService.findWaitReceiveOrders(request);
        return ResponseEntity.ok(result);
    }
    @PostMapping("orders/evaluate")
    public ResponseEntity<PageResult<Order>> findWaitEvaluateOrders(@RequestBody SearchRequest request){
        PageResult<Order> result=orderService.findWaitEvaluateOrders(request);
        return ResponseEntity.ok(result);
    }





}

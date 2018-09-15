package com.leyou.order.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.auth.pojo.UserInfo;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.IdWorker;
import com.leyou.common.vo.PageResult;
import com.leyou.order.client.AddressClient;
import com.leyou.order.client.GoodsClient;
import com.leyou.order.dto.AddressDTO;
import com.leyou.order.dto.CartDTO;
import com.leyou.order.dto.OrderDTO;
import com.leyou.order.eumn.OrderStatusEnum;
import com.leyou.order.eumn.PayState;
import com.leyou.order.eumn.PayStatusEnum;
import com.leyou.order.interceptor.LoginInterceptor;
import com.leyou.order.mapper.LogMapper;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.*;
import com.leyou.order.utils.PayHelper;
import com.leyou.pojo.Sku;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.chrono.IslamicChronology;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xiongzixuan
 */
@Slf4j
@Service
public class OrderService {
     @Autowired
     private OrderMapper orderMapper;
     @Autowired
     private OrderDetailMapper orderDetailMapper;
     @Autowired
     private OrderStatusMapper orderStatusMapper;
//     @Autowired
//     private AddressClient addressClient;
     @Autowired
     private IdWorker idWorker;
     @Autowired
     private GoodsClient goodsClient;
     @Autowired
     private PayHelper payHelper;
     @Autowired
     private LogMapper logMapper;

    /**
     * 创建订单
     * @param orderDTO
     * @return
     */
    @Transactional
    public Long creatOrder(OrderDTO orderDTO) {
        //通过orderdto中的数据转化成order对象
        Order order = new Order();
        //设置订单id
         order.setOrderId(idWorker.nextId());
        //首先通过addressId查找地址等信息封装到AddressDTO
        AddressDTO addressDTO = AddressClient.findById(orderDTO.getAddressId());
        //给订单设置收件人地址等信息
        order.setReceiver(addressDTO.getName());
        order.setReceiverAddress(addressDTO.getAddress());
        order.setReceiverCity(addressDTO.getCity());
        order.setReceiverDistrict(addressDTO.getDistrict());
        order.setReceiverMobile(addressDTO.getPhone());
        order.setReceiverZip(addressDTO.getZipCode());
        order.setReceiverState(addressDTO.getState());
        //设置订单的付费类型
        order.setPaymentType(orderDTO.getPaymentType());
        //设置订单的来源和发票类型
        order.setSourceType(2);
        order.setInvoiceType(0);
        //获取用户的信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        order.setUserId(userInfo.getId());
        order.setBuyerNick(userInfo.getUsername());
        order.setBuyerRate(false);
        //通过orderdto中的购物车集合，然后根据信息去数据库中查询获取相关商品，然后在减库存
       //将list<CartDto>转化为map
        List<CartDTO> carts = orderDTO.getCarts();
        Map<Long,Integer> map= carts.stream().collect(Collectors.toMap(c -> c.getSkuId(), c -> c.getNum()));
        Set<Long> skuIds = map.keySet();
        List<Sku> skus = goodsClient.findSkusBySkuIds(new ArrayList<>(skuIds));
        if(CollectionUtils.isEmpty(skus)){
            log.error("购物车的商品为空");
            throw new LyException("没有商品可以生成订单", HttpStatus.BAD_REQUEST);
        }
        Long totalPay=0L;
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (Sku sku : skus) {
            //计算总价格
            Integer num = map.get(sku.getId());
            totalPay+=sku.getPrice()*num;
            //一个商品就是一个orderdetail,将sku数据封装至orderdetail
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(order.getOrderId());
            orderDetail.setNum(num);
            orderDetail.setImage(StringUtils.substringBefore(sku.getImages(),","));
            orderDetail.setOwnSpec(sku.getOwnSpec());
            orderDetail.setPrice(sku.getPrice());
            orderDetail.setSkuId(sku.getId());
            orderDetail.setTitle(sku.getTitle());
            orderDetails.add(orderDetail);

        }
        order.setTotalPay(totalPay);
        order.setActualPay(totalPay+order.getPostFee());
        order.setCreateTime(new Date());
        //订单基本信息已组织完
        //将订单插入数据表
        orderMapper.insert(order);
        //将订单详情集合插入数据表
        orderDetailMapper.insertList(orderDetails);
        //组织订单状态信息
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(order.getOrderId());
        orderStatus.setCreateTime(order.getCreateTime());
        orderStatus.setStatus(OrderStatusEnum.INIT.value());
        //将订单状态添加至数据表
        orderStatusMapper.insertSelective(orderStatus);
        //至此订单已经创建完，需要减库存，清除购物车
        //根据skuids减库存
        goodsClient.decreasedSkuStock(map);
        log.info("商品订单已生成，订单号为{}",order.getOrderId());
       return order.getOrderId();

    }

    public String getPayCodeUrl(Long orderId) {

        //先根据订单号查询出订单
        Order order = orderMapper.selectByPrimaryKey(orderId);
        //判断订单状态
        OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(orderId);
        if(!orderStatus.getStatus().equals(OrderStatusEnum.INIT.value())){
            //已付款或其他状态
            log.error("生成微信下单链接失败，获取已下单");
            throw new LyException("生成微信下单链接失败，获取已下单",HttpStatus.BAD_REQUEST);

        }
        //未支付
        //调用支付助手的获取支付链接功能
        //order.getActualPay()
        String codeUrl = payHelper.getPayCodeUrl(orderId, "乐友商城订单", 1L);
        //生成codeUrl之后不要急着返回支付链接，先生成支付日志
        logMapper.deleteByPrimaryKey(orderId);
        PayLog payLog = new PayLog();
        payLog.setOrderId(orderId);
        //order.getActualPay()
        payLog.setTotalFee(1L);
        payLog.setUserId(order.getUserId());
        payLog.setStatus(PayStatusEnum.NOT_PAY.value());
        payLog.setPayType(1);
        payLog.setCreateTime(new Date());
        logMapper.insertSelective(payLog);

        return codeUrl;

    }

    /**
     * @param orderId
     * @return
     * 根据订单id查询订单
     */
    public Order findOrderByOrderId(Long orderId) {
        Order order = orderMapper.selectByPrimaryKey(orderId);

        if(order==null){
            log.error("没有订单");
            throw new LyException("没有订单",HttpStatus.BAD_REQUEST);
        }
        OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(orderId);
        if(orderStatus==null){
            log.error("没有订单");
            throw new LyException("没有订单",HttpStatus.BAD_REQUEST);
        }
        order.setOrderStatus(orderStatus);

        return order;
    }

    /**
     * 校验微信的通知参数
     * @param data
     */
    @Transactional
    public void handleNotify(Map<String, String> data) {
         payHelper.handleNotify(data);
    }

    public Integer findOrderStatus(Long orderId) {
        //先去支付日志处查询是否已经支付成功
        PayLog payLog = logMapper.selectByPrimaryKey(orderId);
        if(payLog==null||payLog.getStatus().equals(PayStatusEnum.NOT_PAY.value())){
         //显示未支付
        //主动去微信处查询订单是否已经成功

        PayState payState = payHelper.queryPayState(orderId);
        return payState.getValue();
        }
        if(payLog.getStatus().equals(PayStatusEnum.SUCCESS.value())){
            return PayState.SUCCESS.getValue();
        }
        //如果处于其他状态，直接表明订单状态是未支付的
        return PayState.FAIL.getValue();


    }

    /**
     *
     * 查询用户所有订单
     * @param request
     * @return
     */
    public PageResult<Order> findOrders(SearchRequest request) {
        //解析请求参数
        Long userId = request.getUserId();
        String key = request.getKey();
        //查询
        if (StringUtils.isBlank(key)){
        //使用分页助手进行分页
            Page<Order> page=PageHelper.startPage(request.getPage(), request.getSize());
            Order order = new Order();
            order.setUserId(userId);
            order.setPostFee(null);
            order.setInvoiceType(null);
            order.setSourceType(null);
            List<Order> orders = orderMapper.select(order);
            for (Order order1 : orders) {
                Long orderId = order1.getOrderId();
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setOrderId(orderId);
                List<OrderDetail> orderDetails = orderDetailMapper.select(orderDetail);
                if(CollectionUtils.isEmpty(orderDetails)){
                    log.error("没有该订单，订单号为{}",orderId);
                    throw new LyException("没有该订单",HttpStatus.BAD_REQUEST);
                }
                order1.setOrderDetails(orderDetails);
                OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(orderId);
                order1.setOrderStatus(orderStatus);
            }
            PageInfo<Order> pageInfo = new PageInfo<>(orders);
            return new PageResult<Order>(pageInfo.getTotal(),Long.valueOf(pageInfo.getPages()),pageInfo.getList());
        }else {


            //根据关键字查询订单详情表
            Example example = new Example(OrderDetail.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andLike("title","%"+key+"%");
            List<OrderDetail> orderDetails = orderDetailMapper.selectByExample(example);
            //用一个map将订单id与具有相同订单id的订单项集合对应
            Map<Long, List<OrderDetail>> map = new HashMap<>();
            for (OrderDetail orderDetail : orderDetails) {
            List<OrderDetail> orderDetails1 = new ArrayList<>();
                //判断map中是否有该key
                if (map.containsKey(orderDetail.getOrderId())) {
                    //有该key
                    //获取值
                    orderDetails1=map.get(orderDetail.getOrderId());

                }
                //往集合中加订单详情
                orderDetails1.add(orderDetail);
                map.put(orderDetail.getOrderId(),orderDetails1);
            }
            List<Long> orderIds = new ArrayList<>(map.keySet());
            //分页
            Page<Order> page = PageHelper.startPage(request.getPage(), request.getSize());
            List<Order> orders = orderMapper.selectByIdList(orderIds);
            for (Order order : orders) {
                Long orderId = order.getOrderId();
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setOrderId(orderId);
                List<OrderDetail> orderDetails2 = orderDetailMapper.select(orderDetail);
                if(CollectionUtils.isEmpty(orderDetails2)){
                    log.error("没有该订单，订单号为{}",orderId);
                    throw new LyException("没有该订单",HttpStatus.BAD_REQUEST);
                }
                order.setOrderDetails(orderDetails2);
               order.setOrderStatus(orderStatusMapper.selectByPrimaryKey(order.getOrderId()));
            }
            PageInfo<Order> pageInfo = new PageInfo<>(orders);
            return new PageResult<Order>(pageInfo.getTotal(),Long.valueOf(pageInfo.getPages()),pageInfo.getList());

        }

    }

    /**
     * 查询处等待付款的订单
     * @param request
     * @return
     */
    public PageResult<Order> findWaitOrders(SearchRequest request) {
        Page<Order> page = PageHelper.startPage(request.getPage(), request.getSize());
        List<Order> waitOrders = orderMapper.findWaitOrders(request.getUserId(), OrderStatusEnum.INIT.value());
        return dealOrders(waitOrders);

    }

    /**
     * 查询等待发货订单
     * @param request
     * @return
     */
    public PageResult<Order> findWaitSendOrders(SearchRequest request) {
        Page<Order> page = PageHelper.startPage(request.getPage(), request.getSize());
        List<Order> waitOrders = orderMapper.findWaitSendOrders(request.getUserId(), OrderStatusEnum.PAY_UP.value());
        return dealOrders(waitOrders);


    }

    /**
     * 查询等待收获订单
     * @param request
     * @return
     */
    public PageResult<Order> findWaitReceiveOrders(SearchRequest request) {
        Page<Order> page = PageHelper.startPage(request.getPage(), request.getSize());
        List<Order> waitOrders = orderMapper.findWaitReceiveOrders(request.getUserId(), OrderStatusEnum.DELIVERED.value());

        return dealOrders(waitOrders);

    }

    /**
     * 查询等待评价订单
     * @param request
     * @return
     */
    public PageResult<Order> findWaitEvaluateOrders(SearchRequest request) {
        Page<Order> page = PageHelper.startPage(request.getPage(), request.getSize());
        List<Order> waitOrders = orderMapper.findWaitEvaluateOrders(request.getUserId(), OrderStatusEnum.CONFIRMED.value());
        return dealOrders(waitOrders);

    }


    public PageResult<Order> dealOrders(List<Order> waitOrders ){
        for (Order order1 : waitOrders) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(order1.getOrderId());
            List<OrderDetail> details = orderDetailMapper.select(orderDetail);
            order1.setOrderDetails(details);
            OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(order1.getOrderId());
            order1.setOrderStatus(orderStatus);
        }
        PageInfo<Order> pageInfo = new PageInfo<>(waitOrders);

        return new PageResult<Order>(pageInfo.getTotal(),Long.valueOf(pageInfo.getPages()),pageInfo.getList());
         
    }
}

package com.leyou.order.utils;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.leyou.common.exception.LyException;
import com.leyou.order.config.WXPayConfigImpl;
import com.leyou.order.eumn.OrderStatusEnum;
import com.leyou.order.eumn.PayState;
import com.leyou.order.eumn.PayStatusEnum;
import com.leyou.order.mapper.LogMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderStatus;
import com.leyou.order.pojo.PayLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class PayHelper {
    private WXPay wxPay;
    private WXPayConfigImpl wxPayConfig;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private  OrderStatusMapper orderStatusMapper;
    @Autowired
    private LogMapper logMapper;

    /**
     * 有参构造函数
     * @param wxPayConfig
     */
    public PayHelper(WXPayConfigImpl wxPayConfig) {
         wxPay = new WXPay(wxPayConfig);
        this.wxPayConfig = wxPayConfig;
    }
    /**
     * 调用支付接口，获得支付链接
     */
     public String getPayCodeUrl(Long orderId, String description, Long totalPay) {
         try {
             //设置请求微信接口的参数
             Map<String, String> data = new HashMap<>();
             // 商品描述
             data.put("body", description);
             // 订单号
             data.put("out_trade_no", orderId.toString());
             //货币
             data.put("fee_type", "CNY");
             //金额，单位是分
             data.put("total_fee", totalPay.toString());
             //调用微信支付的终端IP
             data.put("spbill_create_ip", "127.0.0.1");
             //回调地址
             data.put("notify_url", wxPayConfig.getNotifyUrl());
             // 交易类型为扫码支付
             data.put("trade_type", "NATIVE");
             //发送请求并且传递请求参数，并且返回结果
             Map<String, String> result = wxPay.unifiedOrder(data);
             //校验return_code
             if(WXPayConstants.FAIL.equals(result.get("return_code"))){
                 log.error("微信下单通信失败，生成支付链接失败，错误信息{}",result.get("return_mesg"));
                 throw new LyException("通信失败，生成支付链接失败",HttpStatus.BAD_REQUEST);
             }
             //校验result_code
             if (WXPayConstants.FAIL.equals(result.get("result_code")))
             {
                 log.error("微信下单通信失败，生成支付链接失败，错误码{},错误信息{}",result.get("err_code"),result.get("err_code_des"));
                 throw new LyException("通信失败，生成支付链接失败",HttpStatus.BAD_REQUEST);
             }
             //校验签名
             isSignatureValid(result);
             //校验成功，获得result中的url
             String code_url = result.get("code_url");
             return code_url;


         } catch (Exception e) {
             log.error("生成支付链接失败，订单号{}", orderId,e);
             throw new LyException("生成支付链接失败", HttpStatus.BAD_REQUEST);
         }


     }

    /**
     * 校验签名方法
     * @param result
     */
      private void isSignatureValid(Map<String, String> result){
        try {
            // 下单成功，验证签名
            boolean boo1 = WXPayUtil.isSignatureValid(result, wxPayConfig.getKey(), WXPayConstants.SignType.MD5);
            boolean boo2 = WXPayUtil.isSignatureValid(result, wxPayConfig.getKey(), WXPayConstants.SignType.HMACSHA256);
            if (!boo1 && !boo2) {
                // 验证失败
                log.error("【微信下单】签名验证失败");
                throw new LyException("签名验证失败！",HttpStatus.INTERNAL_SERVER_ERROR );
            }
        } catch (Exception e){
            // 验证失败
            log.error("【微信下单】签名验证失败");
            throw new LyException("签名验证失败！",HttpStatus.INTERNAL_SERVER_ERROR );
        }
    }
    /**
     * 处理微信通知的校验
     */
    public void handleNotify(Map<String, String> msg) {
        //1.校验签名
        isSignatureValid(msg);
        // 2、校验金额
        // 2.1.解析数据
        String totalFee = msg.get("total_fee");
        String outTradeNo = msg.get("out_trade_no");
        String transactionId = msg.get("transaction_id");
        String bankType = msg.get("bank_type");
        if (StringUtils.isBlank(outTradeNo) || StringUtils.isBlank(totalFee)
                || StringUtils.isBlank(transactionId) || StringUtils.isBlank(bankType)) {
            log.error("【微信支付回调】支付回调返回数据不正确");
            throw new LyException("数据不正确",HttpStatus.INTERNAL_SERVER_ERROR);
        }
        // 2.2.查询订单
        Order order = orderMapper.selectByPrimaryKey(Long.valueOf(outTradeNo));
        // 2.3.校验金额，此处因为我们支付的都是1，所以写死了，应该与订单中的对比
        if (1L != Long.valueOf(totalFee)) {
            log.error("【微信支付回调】支付回调返回数据不正确");
            throw new LyException("数据不正确",HttpStatus.INTERNAL_SERVER_ERROR);
        }
        //校验完成以后就判断订单状态是否为未支付状态，如果为已支付状态等其他状态就直接返回
        OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(Long.valueOf(outTradeNo));
        if (!orderStatus.getStatus().equals(OrderStatusEnum.INIT.value())) {
            //不等于未支付状态代表已经支付或者支付关闭
            return;
        }
        //修改订单状态
        orderStatus.setStatus(OrderStatusEnum.PAY_UP.value());
        orderStatus.setPaymentTime(new Date());
        orderStatusMapper.updateByPrimaryKeySelective(orderStatus);
        //修改支付日志
        PayLog payLog = logMapper.selectByPrimaryKey(Long.valueOf(outTradeNo));
        if(payLog.getStatus().equals(PayStatusEnum.NOT_PAY.value())){
        payLog.setStatus(PayStatusEnum.SUCCESS.value());
        payLog.setBankType(bankType);
        payLog.setPayTime(new Date());
        payLog.setTransactionId(transactionId);
        logMapper.updateByPrimaryKeySelective(payLog);
        }



    }

    /**
     * 商家主动查询是否已经付款成功，成功就修改订单状态
     * @param orderId
     * @return
     */
    public PayState queryPayState(Long orderId) {
        Map<String, String> data = new HashMap<>();
        // 订单号
        data.put("out_trade_no", orderId.toString());
        try {
            Map<String, String> result = wxPay.orderQuery(data);
            // 链接失败
            if (result == null || WXPayConstants.FAIL.equals(result.get("return_code"))) {
                // 未查询到结果或链接失败，认为是未付款
                log.info("【支付状态查询】链接微信服务失败，订单编号：{}", orderId);
                return PayState.NOT_PAY;
            }
            // 查询失败
            if (WXPayConstants.FAIL.equals(result.get("result_code"))) {
                log.error("【支付状态查询】查询微信订单支付状态失败，错误码：{}，错误信息：{}",
                        result.get("err_code"), result.get("err_code_des"));
                return PayState.NOT_PAY;
            }

            // 校验签名
            isSignatureValid(result);


            String state = result.get("trade_state");
            if ("SUCCESS".equals(state)) {
                // 修改支付状态等信息
                handleNotify(result);

                // success，则认为付款成功
                return PayState.SUCCESS;
            } else if (StringUtils.equals("USERPAYING", state) || StringUtils.equals("NOTPAY", state)) {
                // 未付款或正在付款，都认为是未付款
                return PayState.NOT_PAY;
            } else {
                // 其它状态认为是付款失败
                return PayState.FAIL;
            }
        } catch (Exception e) {
            log.error("查询订单状态异常", e);
            return PayState.NOT_PAY;
        }
    }




}

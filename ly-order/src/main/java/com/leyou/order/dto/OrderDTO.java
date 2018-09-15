package com.leyou.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    @NotNull(message = "地址不能为空")
    private Long addressId;
    @NotNull(message = "没有商品要形成订单")
    private List<CartDTO> carts;
    @NotNull(message = "请选择付款方式")
    private Integer paymentType;
}

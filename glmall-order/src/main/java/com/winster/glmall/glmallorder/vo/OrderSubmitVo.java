package com.winster.glmall.glmallorder.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderSubmitVo {
    /**
     * 收货地址id
     */
    private Long addrId;

    /**
     * 支付方式
     */
    private Integer payType;

    /**
     * 订单防重令牌
     */
    private String orderToken;

    /**
     * 订单应付价格，验价
     */
    private BigDecimal payPrice;

    /**
     * 相关备注
     */
    private String note;

    // todo 用户相关信息，从登录用户session中获取；
    // todo 商品信息，从用户选中的购物车中获取，无需从页面提交

}

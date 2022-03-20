package com.winster.glmall.glmallorder.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderConfirmVo {

    /**
     * 用户收获地址列表
     */
    private List<MemberAddressVo> addressVoList;

    /**
     * 订单页面购物项
     */
    private List<OrderItemVo> itemVos;

    /**
     * 会员积分
     */
    private Integer integration;

    /**
     * 订单总额
     */
    private BigDecimal totalAmount;

    /**
     * 应付价格
     */
    private BigDecimal payPrice;

    /**
     * 商品总数量
     */
    private Integer totalCount;

    /**
     * 订单防重令牌
     */
    private String orderToken;


    // todo 发票信息暂时不封装了
}

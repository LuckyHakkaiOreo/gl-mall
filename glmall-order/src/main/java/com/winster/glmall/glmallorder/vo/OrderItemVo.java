package com.winster.glmall.glmallorder.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单购物项，其实就是购物车页面传递来的购物项
 */
@Data
public class OrderItemVo {
    private Long skuId;
    private String title;
    private String image;
    private List<String> skuAttr;
    private BigDecimal price;
    private Integer count;
    private BigDecimal totalPrice;
    // todo
    private boolean hasStock;
    private BigDecimal weight;
}

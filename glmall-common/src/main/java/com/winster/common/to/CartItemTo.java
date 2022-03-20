package com.winster.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartItemTo {
    private Long skuId;
    private Boolean check;
    private String title;
    private String image;
    private List<String> skuAttr;
    private BigDecimal price;
    private Integer count;
    private BigDecimal totalPrice;
}

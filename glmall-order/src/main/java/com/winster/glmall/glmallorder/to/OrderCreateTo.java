package com.winster.glmall.glmallorder.to;

import com.winster.glmall.glmallorder.entity.OrderEntity;
import com.winster.glmall.glmallorder.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCreateTo {

    private OrderEntity order;

    private List<OrderItemEntity> orderItems;

    private BigDecimal payPrice;

    private BigDecimal fare;
}

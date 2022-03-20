package com.winster.common.to;

import lombok.Data;

import java.util.List;

@Data
public class WareSkuLockTo {
    /**
     * 订单序列号
     */
    private String orderSn;

    /**
     * 需要锁定的库存信息
     */
    private List<OrderItemTo> locks;
}

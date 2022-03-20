package com.winster.glmall.glmallorder.vo;

import com.winster.glmall.glmallorder.entity.OrderEntity;
import lombok.Data;

@Data
public class OrderSubmitResponseVo {

    private OrderEntity orderEntity;

    /**
     * 下单状态：0成功，其他失败
     */
    private Integer code;
}

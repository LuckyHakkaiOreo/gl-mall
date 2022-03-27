package com.winster.glmall.glmallware.feign;

import com.winster.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("glmall-order")
public interface OrderFeign {
    /**
     * 根据订单号查询订单信息
     */
    @RequestMapping("/glmallOrder/order/getOne/{orderSn}")
    R getOneByOrderSn(@PathVariable("orderSn") String orderSn);
}

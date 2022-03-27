package com.winster.glmall.glmallseckill.feign;

import com.winster.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("glmall-coupon")
public interface CouponFeign {
    @GetMapping("/glmallcoupon/seckillsession/getLatest3DaySeckillSession")
    R getLatest3DaySeckillSession();
}

package com.winster.glmall.glmallorder.feign;

import com.winster.common.to.WareSkuLockTo;
import com.winster.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("glmall-ware")
public interface WareFeign {

    @PostMapping("/glmallWare/waresku/lock/order")
    public R orderLockStock(@RequestBody WareSkuLockTo to);
}

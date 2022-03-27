package com.winster.glmall.glmallseckill.feign;

import com.winster.common.to.SkuInfoTo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("glmall-product")
public interface ProductFeign {
    @RequestMapping("glmallproduct/skuinfo/getSkuInfoListByIds")
    List<SkuInfoTo> getSkuInfoListByIds(@RequestParam List<Long> skuIds);
}

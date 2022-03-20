package com.winster.glmall.glmallorder.feign;

import com.winster.common.to.SpuInfoWithSkuIdTo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@FeignClient("glmall-product")
public interface ProductFeign {
    @PostMapping("/glmallproduct/spuinfo/spuListBySkuIds")
    @ResponseBody
    public List<SpuInfoWithSkuIdTo> getSpuListBySkuIds(@RequestParam("skuIds") List<Long> skuIds);
}

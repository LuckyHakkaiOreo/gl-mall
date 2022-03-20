package com.winster.glmall.cart.feign;

import com.winster.common.to.SkuInfoTo;
import com.winster.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("glmall-product")
public interface ProductFeign {

    @RequestMapping("/glmallproduct/skuinfo/info/{skuId}")
    R info(@PathVariable("skuId") Long skuId);

    @RequestMapping("/glmallproduct/skusaleattrvalue/strList/{skuId}")
    List<String> getSkuSaleAttrValues(@PathVariable("skuId") Long skuId);

    @RequestMapping("/glmallproduct/skuinfo/getSkuInfoListByIds")
    List<SkuInfoTo> getSkuInfoListByIds(@RequestParam List<Long> skuIds);
}

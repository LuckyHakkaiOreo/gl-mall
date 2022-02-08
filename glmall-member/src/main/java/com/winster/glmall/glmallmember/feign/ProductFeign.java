package com.winster.glmall.glmallmember.feign;

import com.winster.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;


// 指定远端服务名

@FeignClient("glmall-product")
public interface ProductFeign {
    // 远端服务暴露的接口：注意映射路径必须是该接口的【全部】访问路径
    @RequestMapping("/glmallproduct/category/feign/test")
    public R test();
}

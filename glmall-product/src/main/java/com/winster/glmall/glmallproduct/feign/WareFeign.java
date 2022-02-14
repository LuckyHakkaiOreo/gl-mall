package com.winster.glmall.glmallproduct.feign;

import com.winster.common.to.WareSkuTo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


// 指定远端服务名

@FeignClient("glmall-ware")
public interface WareFeign {
    // 远端服务暴露的接口：注意映射路径必须是该接口的【全部】访问路径

    /**
     *  查询多个指定sku的库存信息
     * @return
     */
    @PostMapping("/glmallWare/waresku/wareskubyids")
    List<WareSkuTo> getWareSkuByskuIds(@RequestBody List<Long> params);
}

package com.winster.glmall.glmallproduct.feign;

import com.winster.common.to.es.SkuESTo;
import com.winster.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.util.List;


// 指定远端服务名

@FeignClient("third-party-server")
public interface ThirdPartyFeign {
    // 远端服务暴露的接口：注意映射路径必须是该接口的【全部】访问路径

    /**
     *  往es中保存sku信息
     * @return
     */
    @PostMapping("/es/opt/save/skuinfos")
    public R saveSkuESTos(@RequestBody List<SkuESTo> list) throws IOException;
}

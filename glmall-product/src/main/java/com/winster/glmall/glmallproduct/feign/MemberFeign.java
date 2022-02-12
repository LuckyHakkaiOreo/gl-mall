package com.winster.glmall.glmallproduct.feign;

import com.winster.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;


// 指定远端服务名

@FeignClient("glmall-member")
public interface MemberFeign {
    // 远端服务暴露的接口：注意映射路径必须是该接口的【全部】访问路径

    /**
     * 获取所有的会员层级信息
     * @return
     */
    @RequestMapping("/glmallmember/memberlevel/allList")
    public R allList();
}

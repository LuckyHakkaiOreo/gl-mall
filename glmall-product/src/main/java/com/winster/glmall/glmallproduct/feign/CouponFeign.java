package com.winster.glmall.glmallproduct.feign;

import com.winster.common.to.MemberPriceTo;
import com.winster.common.to.SkuFullReductionTo;
import com.winster.common.to.SkuLadderTo;
import com.winster.common.to.SpuBoundsTo;
import com.winster.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;


// 指定远端服务名

@FeignClient("glmall-coupon")
public interface CouponFeign {
    // 远端服务暴露的接口：注意映射路径必须是该接口的【全部】访问路径

    /**
     * 保存sku的积分数据
     * @param spuBounds
     * @return
     */
    @PostMapping("/glmallcoupon/spubounds/save")
    public R save(@RequestBody SpuBoundsTo spuBounds);

    /**
     * 保存sku打折数据
     * @param skuLadder
     * @return
     */
    @PostMapping("/glmallcoupon/skuladder/save")
    public R save(@RequestBody SkuLadderTo skuLadder);

    /**
     * 保存sku满减数据
     */
    @RequestMapping("/glmallcoupon/skufullreduction/save")
    public R save(@RequestBody SkuFullReductionTo skuFullReduction);

    /**
     * 保存某sku的会员价格
     * @param memberPrices
     * @return
     */
    @PostMapping("/glmallcoupon/memberprice/savebatch")
    public R save(@RequestBody List<MemberPriceTo> memberPrices);

}

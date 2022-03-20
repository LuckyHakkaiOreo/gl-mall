package com.winster.glmall.glmallorder.feign;

import com.winster.glmall.glmallorder.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@FeignClient("glmall-cart")
public interface CartFeign {
    @GetMapping("/currentUserCartItemList")
    @ResponseBody
    List<OrderItemVo> getCurrentLoginUserCheckedCartItemList();
}

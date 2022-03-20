package com.winster.glmall.glmallorder.feign;

import com.winster.common.utils.R;
import com.winster.glmall.glmallorder.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient("glmall-member")
public interface MemberFeign {

    @GetMapping("/glmallmember/memberreceiveaddress/{userId}/addresslist")
    List<MemberAddressVo> getCurrentLoginUserAddressList(@PathVariable("userId") Long userId);

    @RequestMapping("/glmallmember/memberreceiveaddress/info/{id}")
    public R info(@PathVariable("id") Long id);
}

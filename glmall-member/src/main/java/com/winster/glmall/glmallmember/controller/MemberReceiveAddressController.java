package com.winster.glmall.glmallmember.controller;

import com.winster.common.utils.PageUtils;
import com.winster.common.utils.R;
import com.winster.glmall.glmallmember.entity.MemberReceiveAddressEntity;
import com.winster.glmall.glmallmember.service.MemberReceiveAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;



/**
 * 会员收货地址
 *
 * @author winster
 * @email winsterhandsome@gmail.com
 * @date 2022-02-04 07:34:31
 */
@RestController
@RequestMapping("glmallmember/memberreceiveaddress")
public class MemberReceiveAddressController {
    @Autowired
    private MemberReceiveAddressService memberReceiveAddressService;

    @GetMapping("/{userId}/addresslist")
    public List<MemberReceiveAddressEntity> getCurrentLoginUserAddressList(@PathVariable("userId") Long userId){
        return memberReceiveAddressService.getCurrentLoginUserAddressList(userId);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("glmallmember:memberreceiveaddress:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberReceiveAddressService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("glmallmember:memberreceiveaddress:info")
    public R info(@PathVariable("id") Long id){
		MemberReceiveAddressEntity memberReceiveAddress = memberReceiveAddressService.getById(id);

        return R.ok().put("memberReceiveAddress", memberReceiveAddress);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("glmallmember:memberreceiveaddress:save")
    public R save(@RequestBody MemberReceiveAddressEntity memberReceiveAddress){
		memberReceiveAddressService.save(memberReceiveAddress);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("glmallmember:memberreceiveaddress:update")
    public R update(@RequestBody MemberReceiveAddressEntity memberReceiveAddress){
		memberReceiveAddressService.updateById(memberReceiveAddress);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("glmallmember:memberreceiveaddress:delete")
    public R delete(@RequestBody Long[] ids){
		memberReceiveAddressService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}

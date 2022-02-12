package com.winster.glmall.glmallcoupon.controller;

import com.winster.common.utils.PageUtils;
import com.winster.common.utils.R;
import com.winster.glmall.glmallcoupon.entity.MemberPriceEntity;
import com.winster.glmall.glmallcoupon.service.MemberPriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 商品会员价格
 *
 * @author winster
 * @email winsterhandsome@gmail.com
 * @date 2022-02-04 08:04:23
 */
@RestController
@RequestMapping("glmallcoupon/memberprice")
public class MemberPriceController {
    @Autowired
    private MemberPriceService memberPriceService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("glmallcoupon:memberprice:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = memberPriceService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("glmallcoupon:memberprice:info")
    public R info(@PathVariable("id") Long id) {
        MemberPriceEntity memberPrice = memberPriceService.getById(id);

        return R.ok().put("memberPrice", memberPrice);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("glmallcoupon:memberprice:save")
    public R save(@RequestBody MemberPriceEntity memberPrice) {
        memberPriceService.save(memberPrice);

        return R.ok();
    }

    /**
     * 批量保存
     */
    @PostMapping("/savebatch")
    // @RequiresPermissions("glmallcoupon:memberprice:save")
    public R save(@RequestBody List<MemberPriceEntity> memberPrices) {

        memberPriceService.saveBatch(memberPrices);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("glmallcoupon:memberprice:update")
    public R update(@RequestBody MemberPriceEntity memberPrice) {
        memberPriceService.updateById(memberPrice);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("glmallcoupon:memberprice:delete")
    public R delete(@RequestBody Long[] ids) {
        memberPriceService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}

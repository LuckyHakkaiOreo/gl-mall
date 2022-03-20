package com.winster.glmall.glmallware.controller;

import com.winster.common.to.WareSkuLockTo;
import com.winster.common.utils.PageUtils;
import com.winster.common.utils.R;
import com.winster.glmall.glmallware.entity.WareSkuEntity;
import com.winster.glmall.glmallware.service.WareSkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 商品库存
 *
 * @author winster
 * @email winsterhandsome@gmail.com
 * @date 2022-02-04 07:55:41
 */
@RestController
@RequestMapping("glmallWare/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;

    @PostMapping("/lock/order")
    public R orderLockStock(@RequestBody WareSkuLockTo to) {
        Boolean b = wareSkuService.orderLockStock(to);
        if (b)
            return R.ok("库存锁定成功！");
        else
            return R.error(-1, "订单" + to.getOrderSn() + "商品锁定失败");
    }

    /**
     * 查询多个指定sku的库存信息
     *
     * @return
     */
    @PostMapping("/wareskubyids")
    public List<WareSkuEntity> getWareSkuByskuIds(@RequestBody List<Long> params) {
//        List<Long> ids = params.stream().map(SkuInfoTo::getSkuId).collect(Collectors.toList());
        List<WareSkuEntity> list = wareSkuService.getWareSkuByskuIds(params);
        return list;
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("glmallWare:waresku:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("glmallWare:waresku:info")
    public R info(@PathVariable("id") Long id) {
        WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("glmallWare:waresku:save")
    public R save(@RequestBody WareSkuEntity wareSku) {
        wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("glmallWare:waresku:update")
    public R update(@RequestBody WareSkuEntity wareSku) {
        wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("glmallWare:waresku:delete")
    public R delete(@RequestBody Long[] ids) {
        wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}

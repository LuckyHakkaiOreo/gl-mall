package com.winster.glmall.glmallorder.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.winster.common.to.OrderTo;
import com.winster.common.utils.PageUtils;
import com.winster.common.utils.R;
import com.winster.glmall.glmallorder.entity.OrderEntity;
import com.winster.glmall.glmallorder.service.OrderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;



/**
 * 订单
 *
 * @author winster
 * @email winsterhandsome@gmail.com
 * @date 2022-02-04 07:45:50
 */
@RestController
@RequestMapping("glmallOrder/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 根据订单号查询订单信息
     */
    @RequestMapping("/getOne/{orderSn}")
    // @RequiresPermissions("glmallOrder:order:list")
    public R getOneByOrderSn(@PathVariable("orderSn") String orderSn){
        OrderEntity orderEntity = orderService.getOne(new QueryWrapper<OrderEntity>()
                .eq("order_sn", orderSn));
        OrderTo to = new OrderTo();
        BeanUtils.copyProperties(orderEntity, to);
        return R.ok().put("order", to);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("glmallOrder:order:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = orderService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("glmallOrder:order:info")
    public R info(@PathVariable("id") Long id){
		OrderEntity order = orderService.getById(id);

        return R.ok().put("order", order);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("glmallOrder:order:save")
    public R save(@RequestBody OrderEntity order){
		orderService.save(order);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("glmallOrder:order:update")
    public R update(@RequestBody OrderEntity order){
		orderService.updateById(order);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("glmallOrder:order:delete")
    public R delete(@RequestBody Long[] ids){
		orderService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}

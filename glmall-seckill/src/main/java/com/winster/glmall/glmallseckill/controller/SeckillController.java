package com.winster.glmall.glmallseckill.controller;

import com.winster.common.utils.R;
import com.winster.glmall.glmallseckill.service.SeckillSkuService;
import com.winster.glmall.glmallseckill.to.SeckillSkuRedisTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class SeckillController {

    @Resource
    private SeckillSkuService seckillSkuService;

    /**
     * 返回当前时间可以参与的秒杀商品信息
     * @return
     */
    @GetMapping("/currentSeckillSkus")
    public R getCurrentSeckillSkus(){
        List<SeckillSkuRedisTo> data = seckillSkuService.getCurrentSeckillSkus();

        return R.ok().put("data", data);
    }
}

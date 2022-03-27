package com.winster.glmall.glmallseckill.service;

import com.winster.glmall.glmallseckill.to.SeckillSkuRedisTo;

import java.util.List;

public interface SeckillSkuService {
    void uploadSeckillSkuLast3Days();

    List<SeckillSkuRedisTo> getCurrentSeckillSkus();
}

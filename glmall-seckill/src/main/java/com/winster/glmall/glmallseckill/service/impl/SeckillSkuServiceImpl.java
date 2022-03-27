package com.winster.glmall.glmallseckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.winster.common.to.SkuInfoTo;
import com.winster.common.to.coupon.SeckillSessionTo;
import com.winster.common.to.coupon.SeckillSkuRelationTo;
import com.winster.common.utils.R;
import com.winster.glmall.glmallseckill.common.contant.SeckillConstant;
import com.winster.glmall.glmallseckill.feign.CouponFeign;
import com.winster.glmall.glmallseckill.feign.ProductFeign;
import com.winster.glmall.glmallseckill.service.SeckillSkuService;
import com.winster.glmall.glmallseckill.to.SeckillSkuRedisTo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SeckillSkuServiceImpl implements SeckillSkuService {

    @Resource
    private CouponFeign couponFeign;

    @Resource
    private ProductFeign productFeign;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

    @Override
    public void uploadSeckillSkuLast3Days() {
        // 远程获取最近三天需要参与秒杀的活动和商品
        R r = couponFeign.getLatest3DaySeckillSession();
        List<SeckillSessionTo> data = JSON.parseArray(JSON.toJSONString(r.get("data")), SeckillSessionTo.class);

        if ((Integer) r.get("code") != 0
                || CollectionUtils.isEmpty(data)) {
            log.info("最近三天没有需要上秒杀的活动！");
            return;
        }

        // 将活动及其商品缓存到redis中
        data.forEach(session -> {
            long start = session.getStartTime().getTime();
            long end = session.getEndTime().getTime();
            String k = start + "_" + end;
            String key = SeckillConstant.SECKILL_SESSION_CACHE_PREFIX + k;
            List<SeckillSkuRelationTo> skuRelationEntities = session.getSkuRelationEntities();
            List<Long> skuIds = skuRelationEntities.stream().map(SeckillSkuRelationTo::getSkuId).collect(Collectors.toList());
            skuIds.forEach(skuId -> {
                // must check 幂等性处理：list中，如果活动中存在对应的值则不缓存
                Long size = stringRedisTemplate.opsForList().size(key);
                List<String> range = stringRedisTemplate.opsForList().range(key, 0, size);
                if (CollectionUtils.isEmpty(range) || !range.contains(session.getId() + "_" + skuId)) {
                    log.info("新品上架了！！！新上架活动{}，场次{}，商品{}，", key, session.getId(), skuId);
                    stringRedisTemplate.opsForList().leftPush(key, session.getId() + "_" + skuId);
                } else {
                    log.info("活动{}，场次{}，商品{}，已经上过架！", key, session.getId(), skuId);
                }
            });
        });
        // 将活动中关联的商品缓存到redis中
        data.forEach(session -> {
            BoundHashOperations<String, Object, Object> hashOps = stringRedisTemplate.boundHashOps(SeckillConstant.SECKILL_SKU_CACHE_HASH_KEY);
            // 1.sku的基本信息（去远程服务查）
            List<Long> skuIds = session.getSkuRelationEntities().stream().map(SeckillSkuRelationTo::getSkuId).collect(Collectors.toList());
            List<SkuInfoTo> skuInfoTos = productFeign.getSkuInfoListByIds(skuIds);

            session.getSkuRelationEntities().forEach(seckillSkuRelationTo -> {
                if (hashOps.hasKey(seckillSkuRelationTo.getPromotionSessionId() + "_" + seckillSkuRelationTo.getSkuId().toString())) {
                    log.info("场次：{}，商品：{}，已经上架过了...", seckillSkuRelationTo.getPromotionSessionId(), seckillSkuRelationTo.getSkuId().toString());
                    return;
                }
                SeckillSkuRedisTo redisTo = new SeckillSkuRedisTo();
                // 设置to的商品基本信息
                skuInfoTos.stream().filter(t1 -> seckillSkuRelationTo.getSkuId().equals(t1.getSkuId())).findAny().ifPresent(redisTo::setSkuInfoTo);
                // 设置秒杀的开始时间和结束时间
                redisTo.setStartTime(session.getStartTime().getTime());
                redisTo.setEndTime(session.getEndTime().getTime());
                // 为秒杀设置随机码
                String randomCode = UUID.randomUUID().toString().replace("-", "");
                redisTo.setRandomCode(randomCode);

                // must check 获取商品信号量：主要是用于限流，每个商品对应一个随机码和一个信号量
                RSemaphore semaphore = redissonClient.getSemaphore(SeckillConstant.SKU_STOCK_SEMAPHORE + randomCode);
                // 使用秒杀数作为当前商品信号量的总数
                semaphore.trySetPermits(seckillSkuRelationTo.getSeckillCount().intValue());
                // 2.sku的秒杀信息
                BeanUtils.copyProperties(seckillSkuRelationTo, redisTo);
                // must check 【场次id_skuId】，作为key才对，因为不同秒杀活动可能会绑定同一个商品
                hashOps.put(seckillSkuRelationTo.getPromotionSessionId() + "_" + seckillSkuRelationTo.getSkuId().toString(), JSON.toJSONString(redisTo));
            });
        });

    }

    /**
     * 当前可以参与秒杀的商品信息
     *
     * @return
     */
    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {
        // 1、确定当前时间属于哪个秒杀场次
        long now = new Date().getTime();

        List<SeckillSkuRedisTo> result = new ArrayList<>();
        // 获取所有秒杀活动key
        Set<String> keys = stringRedisTemplate.keys(SeckillConstant.SECKILL_SESSION_CACHE_PREFIX + "*");
        keys.forEach(k -> {
            String replace = k.replace(SeckillConstant.SECKILL_SESSION_CACHE_PREFIX, "");
            String[] kTime = replace.split("_");
            Long start = Long.valueOf(kTime[0]);
            Long end = Long.valueOf(kTime[1]);
            // 当前时刻命中的活动
            if (now >= start && now <= end) {
                // 2、命中了这次活动，获取这个秒杀场次需要的所有商品信息
                Long size = stringRedisTemplate.opsForList().size(k);

                if (size <= 0) {
                    return;
                }
                List<String> range = stringRedisTemplate.opsForList().range(k, 0, size);
                // 获取本场活动关联的所有的商品信息
                BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SeckillConstant.SECKILL_SKU_CACHE_HASH_KEY);
                List<String> skuDatas = hashOps.multiGet(range);

                if (!CollectionUtils.isEmpty(skuDatas)) {
                    List<SeckillSkuRedisTo> collect = skuDatas.stream().map(data -> JSON.parseObject(data, SeckillSkuRedisTo.class)).collect(Collectors.toList());
                    result.addAll(collect);
                }
            }
        });
        return result;
    }
}

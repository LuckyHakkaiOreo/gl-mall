package com.winster.glmall.glmallseckill.common.contant;

public class SeckillConstant {
    /**
     * 秒杀场次缓存key前缀
     */
    public static final String SECKILL_SESSION_CACHE_PREFIX = "seckill:session:";
    /**
     * 秒杀商品缓存hash key
     */
    public static final String SECKILL_SKU_CACHE_HASH_KEY = "seckill:skus";
    /**
     * 秒杀商品信号量库存数量的key前缀
     */
    public static final String SKU_STOCK_SEMAPHORE = "seckill:semaphore:stock:";
    /**
     * 秒杀上架的分布式锁
     */
    public static final String SECKILL_UPLOAD_LOCK = "seckill:upload:lock";
}

package com.winster.glmall.glmallware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.winster.common.utils.PageUtils;
import com.winster.glmall.glmallware.entity.PurchaseDetailEntity;
import com.winster.glmall.glmallware.entity.WareSkuEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author winster
 * @email winsterhandsome@gmail.com
 * @date 2022-02-04 07:55:41
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(List<PurchaseDetailEntity> purchaseDetailEntities);

    List<WareSkuEntity> getWareSkuByskuIds(List<Long> skuIds);
}


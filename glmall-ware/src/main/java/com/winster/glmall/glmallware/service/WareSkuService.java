package com.winster.glmall.glmallware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rabbitmq.client.Channel;
import com.winster.common.to.WareSkuLockTo;
import com.winster.common.to.mq.StockLockedTo;
import com.winster.common.utils.PageUtils;
import com.winster.glmall.glmallware.entity.PurchaseDetailEntity;
import com.winster.glmall.glmallware.entity.WareSkuEntity;
import org.springframework.amqp.core.Message;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * εεεΊε­
 *
 * @author winster
 * @email winsterhandsome@gmail.com
 * @date 2022-02-04 07:55:41
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(List<PurchaseDetailEntity> purchaseDetailEntities);

    List<WareSkuEntity> getWareSkuByskuIds(List<Long> skuIds);

    Boolean orderLockStock(WareSkuLockTo to);

    void unlockStock(StockLockedTo to, Message message, Channel channel) throws IOException;

}


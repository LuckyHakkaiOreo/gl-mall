package com.winster.glmall.glmallware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.winster.common.to.OrderItemTo;
import com.winster.common.to.WareSkuLockTo;
import com.winster.common.utils.PageUtils;
import com.winster.common.utils.Query;
import com.winster.glmall.glmallware.dao.WareSkuDao;
import com.winster.glmall.glmallware.entity.PurchaseDetailEntity;
import com.winster.glmall.glmallware.entity.WareSkuEntity;
import com.winster.glmall.glmallware.service.WareSkuService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();

        Integer skuId = Integer.valueOf(StringUtils.isBlank((String) params.get("skuId")) ? "0" : (String) params.get("skuId"));
        Integer wareId = Integer.valueOf(StringUtils.isBlank((String) params.get("wareId")) ? "0" : (String) params.get("wareId"));

        if (skuId != 0) {
            wrapper.eq("sku_id", skuId);
        }

        if (wareId != 0) {
            wrapper.eq("ware_id", wareId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(List<PurchaseDetailEntity> purchaseDetailEntities) {

        // 1.根据仓库id和sku_id，查询是否已经有创建过这这个商品的库存信息
        // 2.如果有，则累加数量，如果没有，则新增库存信息再累加
        // 创建没有的库存
        purchaseDetailEntities.forEach(e -> {
            QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("sku_id", e.getSkuId()).eq("ware_id", e.getWareId());
            WareSkuEntity target = null;
            List<WareSkuEntity> wareSkuEntities = this.baseMapper.selectList(wrapper);
            if (CollectionUtils.isEmpty(wareSkuEntities)) {
                // 需要新建该库存
                WareSkuEntity entity = new WareSkuEntity();
                entity.setSkuId(e.getSkuId());
                entity.setWareId(e.getWareId());
                entity.setStock(0);
                entity.setStockLocked(0);
                this.save(entity);
                target = entity;
            } else {
                target = wareSkuEntities.get(0);
            }
            target.setStock(e.getSkuNum() + target.getStock());
            UpdateWrapper<WareSkuEntity> w = new UpdateWrapper<>();
            w.eq("sku_id", e.getSkuId()).eq("ware_id", e.getWareId());
            this.update(target, w);
        });

    }

    @Override
    public List<WareSkuEntity> getWareSkuByskuIds(List<Long> skuIds) {
        QueryWrapper<WareSkuEntity> w = new QueryWrapper<>();
        w.in("sku_id", skuIds);
        List<WareSkuEntity> list = this.list(w);
        return list;
    }

    /**
     * 为某个订单锁定库存
     *
     * @param to
     * @return
     */
    @Override
    public Boolean orderLockStock(WareSkuLockTo to) {
        // 为所有商品锁定库存，有一个商品失败，本次锁定都失败

        // todo 按照下单的地址，找到就近的仓库，锁定商品库存，这里不实现

        // 1. 找到商品在库存中的信息
        List<OrderItemTo> itemList = to.getLocks();
        List<Long> skuIds = itemList.stream().map(OrderItemTo::getSkuId).collect(Collectors.toList());
        QueryWrapper<WareSkuEntity> w = new QueryWrapper<>();
        w.in("sku_id", skuIds);
        List<WareSkuEntity> wareSkuEntityList = this.list(w);
        // 针对商品对库存进行分组
        Map<Long, List<WareSkuEntity>> wsMap = wareSkuEntityList.stream().collect(Collectors.groupingBy(WareSkuEntity::getSkuId));

        // 锁定指定商品的库存
        for (Map.Entry<Long, List<WareSkuEntity>> entry : wsMap.entrySet()) {
            Long skuId = entry.getKey();
            List<WareSkuEntity> wareSkuEntities = entry.getValue();

            if (CollectionUtils.isEmpty(wareSkuEntities)) {
                throw new RuntimeException(skuId + "商品没有库存了");
            }

            AtomicReference<Boolean> allFail = new AtomicReference<>(true);
            wareSkuEntities.forEach(wareSkuEntity -> {
                OrderItemTo orderItemTo = itemList.stream().filter(item -> item.getSkuId().equals(skuId)).findAny().get();
                // 需要锁定的库存
                Integer lockNum = orderItemTo.getCount();
                if (lockNum + wareSkuEntity.getStockLocked() > wareSkuEntity.getStock()) {
                    // 当前仓库数量不够，需要换下一个仓库来尝试锁定
                    return;
                }
                // 只要有一个仓库能够锁定，就不算失败
                wareSkuEntity.setStockLocked(lockNum + wareSkuEntity.getStockLocked());
                allFail.set(false);
            });

            if (allFail.get()) {
                throw new RuntimeException(skuId + "商品锁定库存失败");
            }
        }
        // 保存库存数据
        List<WareSkuEntity> entityList = wsMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        this.updateBatchById(entityList);

        return true;
    }

}
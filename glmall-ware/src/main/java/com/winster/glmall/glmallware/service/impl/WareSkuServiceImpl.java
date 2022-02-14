package com.winster.glmall.glmallware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.winster.common.utils.PageUtils;
import com.winster.common.utils.Query;
import com.winster.glmall.glmallware.dao.WareSkuDao;
import com.winster.glmall.glmallware.entity.PurchaseDetailEntity;
import com.winster.glmall.glmallware.entity.WareSkuEntity;
import com.winster.glmall.glmallware.service.WareSkuService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;


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
            this.update(target,w);
        });

    }

    @Override
    public List<WareSkuEntity> getWareSkuByskuIds(List<Long> skuIds) {
        QueryWrapper<WareSkuEntity> w = new QueryWrapper<>();
        w.in("sku_id", skuIds);
        List<WareSkuEntity> list = this.list(w);
        return list;
    }

}
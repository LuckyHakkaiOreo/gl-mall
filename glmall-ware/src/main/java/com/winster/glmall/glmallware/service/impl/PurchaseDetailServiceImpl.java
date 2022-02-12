package com.winster.glmall.glmallware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.winster.common.utils.PageUtils;
import com.winster.common.utils.Query;
import com.winster.glmall.glmallware.dao.PurchaseDetailDao;
import com.winster.glmall.glmallware.entity.PurchaseDetailEntity;
import com.winster.glmall.glmallware.service.PurchaseDetailService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<PurchaseDetailEntity> wrapper = new QueryWrapper<>();
        String wareId = params.get("wareId") == null ? "" : String.valueOf(params.get("wareId"));
        String status = params.get("status") == null ? "" : String.valueOf(params.get("status"));
        String key = params.get("key") == null ? "" : String.valueOf(params.get("key"));
        if (StringUtils.isNotBlank(wareId)) {
            wrapper.eq("ware_id", wareId);
        }
        // 状态[0新建，1已分配，2正在采购，3已完成，4采购失败
        if (StringUtils.isNotBlank(wareId)) {
            wrapper.eq("status", status);
        }

        if (StringUtils.isNotBlank(key)) {
            wrapper.and(w -> w.eq("sku_num", key).or().eq("purchase_id", key).or().eq("sku_id", key));
        }

        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

}
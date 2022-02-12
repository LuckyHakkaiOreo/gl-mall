package com.winster.glmall.glmallproduct.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.winster.common.utils.PageUtils;
import com.winster.common.utils.Query;
import com.winster.glmall.glmallproduct.dao.SkuInfoDao;
import com.winster.glmall.glmallproduct.entity.SkuInfoEntity;
import com.winster.glmall.glmallproduct.service.SkuInfoService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Integer brandId = Integer.valueOf(params.get("brandId") == null ? "0" : params.get("brandId").toString());
        Integer catelogId = Integer.valueOf(params.get("catelogId") == null ? "0" : params.get("catelogId").toString());
        BigDecimal min = new BigDecimal(params.get("min").toString());
        BigDecimal max = new BigDecimal(params.get("max").toString());
        String key = (String) params.get("key");
        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();

        if (brandId != null && brandId != 0) {
            wrapper.eq("brand_id", brandId);
        }

        if (catelogId != null && catelogId != 0) {
            wrapper.eq("catalog_id", catelogId);
        }

        if (min != null && min.doubleValue() >= 0) {
            wrapper.ge("price", min);
        }

        if (max != null && max.doubleValue() >= 0) {
            wrapper.le("price", max);
        }

        if (StringUtils.isNotBlank(key)) {
            wrapper.and(w -> w.eq("sku_id", key).or().like("sku_name", key));
        }

        /*wrapper.between("price", min, max).eq("brand_id", brandId)
                .eq("catalog_id", catelogId).and(w -> w.eq("sku_id", key).or().like("sku_name", key));*/

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

}
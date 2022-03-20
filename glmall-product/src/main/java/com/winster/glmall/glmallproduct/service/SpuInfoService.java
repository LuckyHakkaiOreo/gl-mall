package com.winster.glmall.glmallproduct.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.winster.common.to.SpuInfoWithSkuIdTo;
import com.winster.common.utils.PageUtils;
import com.winster.glmall.glmallproduct.entity.SpuInfoEntity;
import com.winster.glmall.glmallproduct.vo.SpuInfoVo;

import java.util.List;
import java.util.Map;

/**
 * spu信息
 *
 * @author winster
 * @email winsterhandsome@gmail.com
 * @date 2022-02-04 08:08:19
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfo(SpuInfoVo spuInfo);

    List<SpuInfoWithSkuIdTo> getSpuListBySkuIds(List<Long> skuIds);
}


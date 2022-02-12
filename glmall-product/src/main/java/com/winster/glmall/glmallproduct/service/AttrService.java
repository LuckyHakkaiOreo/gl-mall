package com.winster.glmall.glmallproduct.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.winster.common.utils.PageUtils;
import com.winster.glmall.glmallproduct.entity.AttrEntity;
import com.winster.glmall.glmallproduct.vo.AttrVo;

import java.util.Map;

/**
 * 商品属性
 *
 * @author winster
 * @email winsterhandsome@gmail.com
 * @date 2022-02-04 08:08:19
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long catId, String attrType);

    void save(AttrVo attr);

    AttrVo getInfo(Long attrId);

    void removeAttr(Long[] attrIds);

    void updateAttr(AttrVo attr);
}


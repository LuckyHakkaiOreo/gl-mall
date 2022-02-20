package com.winster.glmall.glmallproduct.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.winster.common.utils.PageUtils;
import com.winster.common.utils.R;
import com.winster.glmall.glmallproduct.entity.AttrGroupEntity;
import com.winster.glmall.glmallproduct.vo.AttrGroupWithAttrVo;
import com.winster.glmall.glmallproduct.vo.AttrNoRelationVo;
import com.winster.glmall.glmallproduct.vo.AttrVo;
import com.winster.glmall.glmallproduct.vo.SkuItemVo;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author winster
 * @email winsterhandsome@gmail.com
 * @date 2022-02-04 08:08:19
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long catelogId);

    List<AttrVo> getAttrByAttrGroupId(Long attrGroupId);

    void removeAttrRelation(List<Map<String, Object>> params);

    PageUtils findAttrNoRelation(AttrNoRelationVo vo, Long attrGroupId);

    R saveAttrNoRelation(List<Map<String, Object>> params);

    List<AttrGroupWithAttrVo> getAttrGroupWithAttrByCatId(Long catelogId);

    List<SkuItemVo.SpuItemAttrGroupVo> getAttrByAttrGroupWithAttrsBySpuId(Long catalogId, Long spuId);
}


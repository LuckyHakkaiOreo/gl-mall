package com.winster.glmall.glmallproduct.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.winster.common.utils.PageUtils;
import com.winster.glmall.glmallproduct.entity.CategoryBrandRelationEntity;

import java.util.List;
import java.util.Map;

/**
 * 品牌分类关联
 *
 * @author winster
 * @email winsterhandsome@gmail.com
 * @date 2022-02-04 08:08:19
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveCategoryBrandRelation(CategoryBrandRelationEntity categoryBrandRelation);

    List<CategoryBrandRelationEntity> getBrandsListByCatId(Long catId);
}


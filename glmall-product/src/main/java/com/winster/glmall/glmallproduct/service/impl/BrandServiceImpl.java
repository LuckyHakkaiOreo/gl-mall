package com.winster.glmall.glmallproduct.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.winster.common.utils.PageUtils;
import com.winster.common.utils.Query;
import com.winster.glmall.glmallproduct.dao.BrandDao;
import com.winster.glmall.glmallproduct.dao.CategoryBrandRelationDao;
import com.winster.glmall.glmallproduct.entity.BrandEntity;
import com.winster.glmall.glmallproduct.entity.CategoryBrandRelationEntity;
import com.winster.glmall.glmallproduct.service.BrandService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Map;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Resource
    private CategoryBrandRelationDao categoryBrandRelationDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String key = (String) params.get("key");
        QueryWrapper<BrandEntity> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(key)) {
            wrapper.eq("brand_id",key).or().like("name",key);
        }
        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void updateCascade(BrandEntity brand) {
        baseMapper.updateById(brand);
        // 如果品牌名字不为空则级联修改品牌分类关系表的品牌名称
        if (StringUtils.isNotBlank(brand.getName())) {
            // 级联删除其他关联了brand表中brandName字段的表
            QueryWrapper<CategoryBrandRelationEntity> cbWrapper = new QueryWrapper<>();
            cbWrapper.eq("brand_id", brand.getBrandId());
            CategoryBrandRelationEntity entity = new CategoryBrandRelationEntity();
            entity.setBrandName(brand.getName());
            categoryBrandRelationDao.update(entity, cbWrapper);
        }


    }

}
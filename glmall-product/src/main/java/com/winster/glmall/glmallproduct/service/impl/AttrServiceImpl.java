package com.winster.glmall.glmallproduct.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.winster.common.constant.ProductConstant;
import com.winster.common.utils.PageUtils;
import com.winster.common.utils.Query;
import com.winster.glmall.glmallproduct.dao.AttrAttrgroupRelationDao;
import com.winster.glmall.glmallproduct.dao.AttrDao;
import com.winster.glmall.glmallproduct.dao.AttrGroupDao;
import com.winster.glmall.glmallproduct.dao.CategoryDao;
import com.winster.glmall.glmallproduct.entity.AttrAttrgroupRelationEntity;
import com.winster.glmall.glmallproduct.entity.AttrEntity;
import com.winster.glmall.glmallproduct.entity.AttrGroupEntity;
import com.winster.glmall.glmallproduct.entity.CategoryEntity;
import com.winster.glmall.glmallproduct.service.AttrService;
import com.winster.glmall.glmallproduct.vo.AttrVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Resource
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Resource
    private AttrGroupDao attrGroupDao;

    @Resource
    private CategoryDao categoryDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catId, String attrType) {
        String key = (String) params.get("key");
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();
        // 分组类型也是筛选条件
        wrapper.eq("attr_type",
                attrType.equalsIgnoreCase(
                        ProductConstant.ProductAttrEnum.TYPE_ATTR_SALE.getFlag()) ?
                        ProductConstant.ProductAttrEnum.TYPE_ATTR_SALE.getCode() :
                        ProductConstant.ProductAttrEnum.TYPE_ATTR_BASE.getCode());
        if (StringUtils.isNotBlank(key)) {
            wrapper.and(w -> w.eq("attr_id", key).or().like("attr_name", key));
        }

        IPage<AttrEntity> page = null;
        if (catId != 0) {
            wrapper.eq("catelog_id", catId);
        }

        page = this.page(
                new Query<AttrEntity>().getPage(params),
                wrapper
        );
        List<AttrEntity> records = page.getRecords();

        List<AttrVo> list = records.stream().map(r -> {
            AttrVo attrVo = new AttrVo();
            BeanUtils.copyProperties(r, attrVo);
            // 查询分类信息
            if (r.getCatelogId() != null) {
                CategoryEntity categoryEntity = categoryDao.selectById(r.getCatelogId());
                if (categoryEntity != null) {
                    attrVo.setCatelogName(categoryEntity.getName());
                }
            }

            // 销售属性没有进行 分组
            if (attrType.equalsIgnoreCase(ProductConstant.ProductAttrEnum.TYPE_ATTR_SALE.getFlag())) {
                return attrVo;
            }

            // 查询分组信息
            Long attrId = r.getAttrId();
            QueryWrapper<AttrAttrgroupRelationEntity> attrWrapper = new QueryWrapper<>();
            attrWrapper.eq("attr_id", attrId);
            List<AttrAttrgroupRelationEntity> relationEntities = attrAttrgroupRelationDao.selectList(attrWrapper);
            List<Long> gIds = relationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrGroupId).collect(Collectors.toList());

            if (!CollectionUtils.isEmpty(gIds)) {
                AttrGroupEntity groupEntity = attrGroupDao.selectById(gIds.get(0));
                if (groupEntity != null) {
                    attrVo.setGroupId(groupEntity.getAttrGroupId());
                    attrVo.setGroupName(groupEntity.getAttrGroupName());
                }
            }
            return attrVo;
        }).collect(Collectors.toList());
        PageUtils pageUtils = new PageUtils(page);

        pageUtils.setList(list);
        return pageUtils;
    }

    @Transactional
    @Override
    public void save(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.save(attrEntity);

        // 保存 属性-分组 关系
        if (attr.getGroupId() != null && attr.getGroupId() != 0
                && attr.getAttrType().equals(ProductConstant.ProductAttrEnum.TYPE_ATTR_BASE.getCode())) {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrId(attrEntity.getAttrId());
            relationEntity.setAttrGroupId(attr.getGroupId());
            attrAttrgroupRelationDao.insert(relationEntity);
        }

    }

    @Override
    public AttrVo getInfo(Long attrId) {
        AttrVo attrVo = new AttrVo();
        AttrEntity attr = this.getById(attrId);
        BeanUtils.copyProperties(attr, attrVo);

        QueryWrapper<AttrAttrgroupRelationEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("attr_id", attr.getAttrId());
        List<AttrAttrgroupRelationEntity> list = attrAttrgroupRelationDao.selectList(wrapper);

        if (!CollectionUtils.isEmpty(list)) {
            AttrAttrgroupRelationEntity relationEntity = list.get(0);
            AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(relationEntity.getAttrGroupId());
            attrVo.setGroupId(attrGroupEntity.getAttrGroupId());
            attrVo.setGroupName(attrGroupEntity.getAttrGroupName());
        }

        return attrVo;
    }

    @Transactional
    @Override
    public void removeAttr(Long[] attrIds) {
        this.removeByIds(Arrays.asList(attrIds));

        QueryWrapper<AttrAttrgroupRelationEntity> wrapper = new QueryWrapper<>();
        wrapper.in("attr_id", Arrays.asList(attrIds));
        attrAttrgroupRelationDao.delete(wrapper);
    }

    @Transactional
    @Override
    public void updateAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.updateById(attrEntity);

        QueryWrapper<AttrAttrgroupRelationEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("attr_id", attrEntity.getAttrId());
        AttrAttrgroupRelationEntity entity = new AttrAttrgroupRelationEntity();
        entity.setAttrId(attr.getAttrId());
        entity.setAttrGroupId(attr.getGroupId());
        entity.setAttrSort(0);
        int update = attrAttrgroupRelationDao.update(entity, wrapper);
        // 没有则新增
        if (update<=0) {
            attrAttrgroupRelationDao.insert(entity);
        }

    }

}
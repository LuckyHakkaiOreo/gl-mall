package com.winster.glmall.glmallproduct.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.winster.common.constant.ProductConstant;
import com.winster.common.exception.ExceptionEnum;
import com.winster.common.utils.PageUtils;
import com.winster.common.utils.Query;
import com.winster.common.utils.R;
import com.winster.glmall.glmallproduct.dao.AttrAttrgroupRelationDao;
import com.winster.glmall.glmallproduct.dao.AttrDao;
import com.winster.glmall.glmallproduct.dao.AttrGroupDao;
import com.winster.glmall.glmallproduct.dao.CategoryDao;
import com.winster.glmall.glmallproduct.entity.AttrAttrgroupRelationEntity;
import com.winster.glmall.glmallproduct.entity.AttrEntity;
import com.winster.glmall.glmallproduct.entity.AttrGroupEntity;
import com.winster.glmall.glmallproduct.entity.CategoryEntity;
import com.winster.glmall.glmallproduct.service.AttrGroupService;
import com.winster.glmall.glmallproduct.vo.AttrGroupWithAttrVo;
import com.winster.glmall.glmallproduct.vo.AttrNoRelationVo;
import com.winster.glmall.glmallproduct.vo.AttrVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Resource
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Resource
    private AttrDao attrDao;

    @Resource
    private AttrGroupDao attrGroupDao;

    @Resource
    private CategoryDao categoryDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        String key = (String) params.get("key");
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(key)) {
            wrapper.and(w -> w.eq("attr_group_id", key).or().like("attr_group_name", key));
        }

        if (catelogId == 0) {
            IPage<AttrGroupEntity> page = this.page(
                    new Query<AttrGroupEntity>().getPage(params),
                    wrapper
            );
            return new PageUtils(page);
        } else {
            // select * from pms_attr_group where catelog_id = ?
            // and (attr_group_id = ? or attr_group_name like "%"+?+"%")
            wrapper.eq("catelog_id", catelogId);
            IPage<AttrGroupEntity> page = this.page(
                    new Query<AttrGroupEntity>().getPage(params),
                    wrapper
            );
            return new PageUtils(page);
        }

    }

    @Override
    public List<AttrVo> getAttrByAttrGroupId(Long attrGroupId) {
        QueryWrapper<AttrAttrgroupRelationEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("attr_group_id", attrGroupId);
        List<AttrAttrgroupRelationEntity> list = attrAttrgroupRelationDao.selectList(wrapper);

        List<AttrVo> collect = list.stream().map(a -> {
            Long attrId = a.getAttrId();
            AttrEntity attrEntity = attrDao.selectById(attrId);
            AttrVo attrVo = new AttrVo();
            if (attrEntity != null) {
                BeanUtils.copyProperties(attrEntity, attrVo);
                // 查询分组信息
                AttrGroupEntity groupEntity = attrGroupDao.selectById(a.getAttrGroupId());
                if (groupEntity != null) {
                    attrVo.setGroupId(a.getAttrGroupId());
                    attrVo.setGroupName(groupEntity.getAttrGroupName());
                }
                CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
                if (categoryEntity != null) {
                    attrVo.setCatelogId(categoryEntity.getCatId());
                    attrVo.setCatelogName(categoryEntity.getName());
                }
            }
            return attrVo;
        }).collect(Collectors.toList());

        return collect;
    }

    @Transactional
    @Override
    public void removeAttrRelation(List<Map<String, Object>> params) {
        // Long attrId, Long attrGroupId

        attrAttrgroupRelationDao.deleteBatchRelation(params);
        /*params.forEach(m -> {
            Integer attrId = (Integer) m.get("attrId");
            Integer attrGroupId = (Integer) m.get("attrGroupId");

            QueryWrapper<AttrAttrgroupRelationEntity> wrapper= new QueryWrapper<>();
            wrapper.eq("attr_id", attrId);
            wrapper.eq("attr_group_id", attrGroupId);
            attrAttrgroupRelationDao.delete(wrapper);
        });*/

    }

    /**
     * 分页 获取当前组所属分类下，尚未被关联（包括未被当前分组关联）的所有属性
     *
     * @param vo
     * @param attrGroupId
     * @return
     */
    @Override
    public PageUtils findAttrNoRelation(AttrNoRelationVo vo, Long attrGroupId) {
        // 1. 获取当前组对应的分类
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupId);

        if (attrGroupEntity == null) {
            return new PageUtils(null);
        }

        // 2. 获取当前组对应的分类下，所有的属性
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("catelog_id", attrGroupEntity.getCatelogId())
                .ne("attr_type", ProductConstant.ProductAttrEnum.TYPE_ATTR_SALE.getCode());
        String key = vo.getKey();
        wrapper.and(w -> w.eq("attr_id", key).or().like("attr_name", key));
        List<AttrEntity> attrEntities = attrDao.selectList(wrapper);

        if (CollectionUtils.isEmpty(attrEntities)) {
            return new PageUtils(null);
        }

        // 3.获取当前分类下所有的分组
        QueryWrapper<AttrGroupEntity> gWrapper = new QueryWrapper<>();
        gWrapper.eq("catelog_id", attrGroupEntity.getCatelogId());
        List<AttrGroupEntity> groupEntityList = attrGroupDao.selectList(gWrapper);

        // 4. 获取当前分类下，已经被所有分组关联过的属性
        List<Long> gIds = groupEntityList.stream().map(AttrGroupEntity::getAttrGroupId).collect(Collectors.toList());
        QueryWrapper<AttrAttrgroupRelationEntity> reWrapper = new QueryWrapper<>();
        reWrapper.in("attr_group_id", gIds);
        List<AttrAttrgroupRelationEntity> relationEntityList = attrAttrgroupRelationDao.selectList(reWrapper);

        //5.从当前组对应的分类下所有的属性中，排除已经被关联的其他属性
        List<Long> aIds = relationEntityList.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
        List<AttrEntity> resultList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(aIds)) {
            resultList = attrEntities.stream().filter(a -> !aIds.contains(a.getAttrId())).collect(Collectors.toList());
        } else {
            resultList = attrEntities;
        }

        //6. 构建分页返回
        Integer pageIndex = vo.getPage();
        Integer limit = vo.getLimit();
        PageUtils pageUtils = new PageUtils(resultList, resultList.size(), limit, pageIndex);
        pageUtils.setList(resultList);
        return pageUtils;
    }

    /**
     * 保存分组与属性关联关系
     *
     * @param params
     * @return
     */
    @Override
    public R saveAttrNoRelation(List<Map<String, Object>> params) {
        //      * [{attrId: 14, attrGroupId: 4}]
        if (CollectionUtils.isEmpty(params)
                || !(params.get(0).containsKey("attrId") || params.get(0).containsKey("attrGroupId"))) {
            return R.error(ExceptionEnum.VALID_PARAMS_EXCEPTION.getCode(), "未包含指定参数，格式：[{attrId: 14, attrGroupId: 4}]");
        }

        // 批量新增
        attrAttrgroupRelationDao.insertBatchRelation(params);

        return R.ok();
    }

    @Override
    public List<AttrGroupWithAttrVo> getAttrGroupWithAttrByCatId(Long catelogId) {
        // 获取到所有的分组信息
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("catelog_id", catelogId);
        List<AttrGroupEntity> attrGroupEntities = attrGroupDao.selectList(wrapper);

        if (CollectionUtils.isEmpty(attrGroupEntities)) {
            return new ArrayList<>();
        }

        // 查询关联表，将当前分组所关联的属性id都查出来
        QueryWrapper<AttrAttrgroupRelationEntity> rWrapper = new QueryWrapper<>();
        rWrapper.in("attr_group_id", attrGroupEntities.stream().map(AttrGroupEntity::getAttrGroupId)
                .collect(Collectors.toList()));
        List<AttrAttrgroupRelationEntity> relationEntityList = attrAttrgroupRelationDao.selectList(rWrapper);

        if (CollectionUtils.isEmpty(relationEntityList)) {
            return new ArrayList<>();
        }

        // 将数据按照组别分组
        Map<Long, List<AttrAttrgroupRelationEntity>> map = relationEntityList.stream()
                .collect(Collectors.groupingBy(AttrAttrgroupRelationEntity::getAttrGroupId));

        // 获取所有属性
        List<Long> aIds = relationEntityList.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
        List<AttrEntity> attrEntityList = attrDao.selectBatchIds(aIds);

        // 查询出所有属性消息，将结果转换为分组最后要返回的vo
        List<AttrGroupWithAttrVo> vos = attrGroupEntities.stream().map(g -> {
            AttrGroupWithAttrVo vo = new AttrGroupWithAttrVo();
            BeanUtils.copyProperties(g, vo);
            List<AttrAttrgroupRelationEntity> list = map.get(g.getAttrGroupId());
            if (CollectionUtils.isEmpty(list)) {
                return vo;
            }
            list.forEach(l -> {
                attrEntityList.stream().filter(a -> a.getAttrId().equals(l.getAttrId()))
                        .findAny().ifPresent(attrEntity -> vo.getAttrs().add(attrEntity));
            });

            return vo;
        }).collect(Collectors.toList());

        return vos;
    }

}
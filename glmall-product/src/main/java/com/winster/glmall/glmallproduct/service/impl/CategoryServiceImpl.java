package com.winster.glmall.glmallproduct.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.winster.common.utils.PageUtils;
import com.winster.common.utils.Query;
import com.winster.glmall.glmallproduct.dao.CategoryBrandRelationDao;
import com.winster.glmall.glmallproduct.dao.CategoryDao;
import com.winster.glmall.glmallproduct.entity.CategoryBrandRelationEntity;
import com.winster.glmall.glmallproduct.entity.CategoryEntity;
import com.winster.glmall.glmallproduct.service.CategoryService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Resource
    private CategoryBrandRelationDao categoryBrandRelationDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 算法分析，其实这里分类的算法思路有两种：
     * 一、写递归函数，从上到下遍历寻找子分类；（这种写法比较复杂）
     * 二、使用java8 stream函数对【所有分类的列表】按照【parentId】进行分类，
     * 然后再为每一个分类从hashMap中找出并设置其子分类；（这种写法思路比较简单）
     */
    @Override
    public List<CategoryEntity> listWithTree() {
        // 1.查询所有分类
        List<CategoryEntity> all = baseMapper.selectList(null);
        // 2.将所有分类按照parentId分组
        Map<Long, List<CategoryEntity>> map = all.stream().collect(Collectors.groupingBy(CategoryEntity::getParentCid));

        // 3.为每一个分类设置其子分类列表并且排序
        all = all.stream()
                // 没有下级分类的不需要设置子分类，直接过滤掉
                .filter(categoryEntity -> map.get(categoryEntity.getCatId()) != null)
                // 在分类map中为所有分类设置子分类（子分类也需要排序）
                .map(categoryEntity -> {
                    categoryEntity.setChildren(map.get(categoryEntity.getCatId()).stream()
                            .sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort())))
                            .collect(Collectors.toList()));
                    return categoryEntity;
                })
                // 仅返回一级分类的数据
                .filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                // 对所有一级分类进行排序
                .sorted(Comparator.comparingInt(categoryEntity -> (categoryEntity.getSort() == null ? 0 : categoryEntity.getSort())))
                .collect(Collectors.toList());

        return all;
    }

    @Override
    public void removeCategories(List<Long> asList) {
        // TODO 1. 检查当前删除的菜单是否有被引用

        // 逻辑删除
        /*
        1.配置全局的逻辑删除（可省略）
        2.低版本需要配置逻辑删除的组件（可省略）
        3.在需要作为逻辑删除的实体类字段上加注解：@TableLogic(value = "1", delval = "0")
        value表示不删除的值，delval表示删除的值
        * */
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> list = new ArrayList<>();
        findCatePath(catelogId, list);

        Collections.reverse(list);

        return list.toArray(new Long[list.size()] );
    }

    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        baseMapper.updateById(category);

        // 如果分类名字不为空，则级联修改品牌分类关系表的分类名称
        if (StringUtils.isNotBlank(category.getName())) {
            // 级联删除其他关联了brand表中brandName字段的表
            QueryWrapper<CategoryBrandRelationEntity> cbWrapper = new QueryWrapper<>();
            cbWrapper.eq("catelog_id", category.getCatId());
            CategoryBrandRelationEntity entity = new CategoryBrandRelationEntity();
            entity.setCatelogName(category.getName());
            categoryBrandRelationDao.update(entity, cbWrapper);
        }
    }

    private void findCatePath(Long catelogId, List<Long> list) {
        list.add(catelogId);
        CategoryEntity entity = baseMapper.selectById(catelogId);
        if (entity.getParentCid() != null && entity.getParentCid() != 0) {
            findCatePath(entity.getParentCid(), list);
        }
    }

}
package com.winster.glmall.glmallproduct.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.winster.common.utils.PageUtils;
import com.winster.common.utils.Query;
import com.winster.glmall.glmallproduct.dao.SkuSaleAttrValueDao;
import com.winster.glmall.glmallproduct.entity.SkuInfoEntity;
import com.winster.glmall.glmallproduct.entity.SkuSaleAttrValueEntity;
import com.winster.glmall.glmallproduct.service.SkuInfoService;
import com.winster.glmall.glmallproduct.service.SkuSaleAttrValueService;
import com.winster.glmall.glmallproduct.vo.SkuItemVo;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Resource
    private SkuInfoService skuInfoService;

    @Resource
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuItemVo.ItemSaleAttrVo> getSaleAttrsBySpuId(Long spuId) {
        List<SkuItemVo.ItemSaleAttrVo> result = new ArrayList<>();
        // 查询出spu下所有skuId
        QueryWrapper<SkuInfoEntity> w1 = new QueryWrapper<>();
        w1.eq("spu_id", spuId);
        List<SkuInfoEntity> skuInfoEntities = skuInfoService.list(w1);
        List<Long> skuIds = skuInfoEntities.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(skuIds)) {
            return result;
        }

        // 查询出所有sku的销售属性
        QueryWrapper<SkuSaleAttrValueEntity> w2 = new QueryWrapper<>();
        w2.in("sku_id", skuIds);
        List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = skuSaleAttrValueService.list(w2);

        // 构建返回数据

        Map<Long, List<SkuSaleAttrValueEntity>> rMap = skuSaleAttrValueEntities.stream().collect(Collectors.groupingBy(SkuSaleAttrValueEntity::getAttrId));
        result = rMap.entrySet().stream().map(item -> {

            SkuItemVo.ItemSaleAttrVo vo = new SkuItemVo.ItemSaleAttrVo();
            vo.setAttrId(item.getKey());

            List<SkuSaleAttrValueEntity> entities = rMap.get(item.getKey());
            if (!CollectionUtils.isEmpty(entities)) {
                vo.setAttrName(entities.get(0).getAttrName());
                // 每个当前attrId下，同一个 attrValue 下，对应sku的销售属性list，从这些list中获取出skuId以逗号分隔
                Map<String, List<SkuSaleAttrValueEntity>> m1 = entities.stream().collect(Collectors.groupingBy(SkuSaleAttrValueEntity::getAttrValue));
                // attrValue,skuIdsStr
                List<SkuItemVo.SaleAttrValueWithSkuIdVo> vList1 = m1.entrySet().stream().map(entry -> {
                    String attrValue = entry.getKey();
                    // 将skuId，以逗号进行分隔
                    String skuIdsStr = entry.getValue().stream().map(e -> e.getSkuId() + "").collect(Collectors.joining(","));
                    SkuItemVo.SaleAttrValueWithSkuIdVo vvo = new SkuItemVo.SaleAttrValueWithSkuIdVo();
                    vvo.setAttrValue(attrValue);
                    vvo.setSkuIds(skuIdsStr);
                    return vvo;
                }).collect(Collectors.toList());

                vo.setAttrValues(vList1);
            }
            return vo;
        }).collect(Collectors.toList());

        return result;
    }

    @Override
    public List<String> getSkuSaleAttrValues(Long skuId) {
        QueryWrapper<SkuSaleAttrValueEntity> w = new QueryWrapper<>();
        w.eq("sku_id", skuId);
        List<SkuSaleAttrValueEntity> list = this.list(w);
        List<String> r = new ArrayList<>();
        if (!CollectionUtils.isEmpty(list)) {
            r = list.stream().map(i -> i.getAttrValue()).collect(Collectors.toList());
        }
        return r;
    }

}
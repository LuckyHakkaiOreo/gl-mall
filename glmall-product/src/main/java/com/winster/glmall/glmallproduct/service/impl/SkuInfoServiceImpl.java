package com.winster.glmall.glmallproduct.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.winster.common.to.WareSkuTo;
import com.winster.common.to.es.AttrESTo;
import com.winster.common.to.es.SkuESTo;
import com.winster.common.utils.PageUtils;
import com.winster.common.utils.Query;
import com.winster.common.utils.R;
import com.winster.glmall.glmallproduct.dao.SkuInfoDao;
import com.winster.glmall.glmallproduct.entity.*;
import com.winster.glmall.glmallproduct.feign.ThirdPartyFeign;
import com.winster.glmall.glmallproduct.feign.WareFeign;
import com.winster.glmall.glmallproduct.service.*;
import com.winster.glmall.glmallproduct.vo.SkuItemVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Resource
    private BrandService brandService;

    @Resource
    private CategoryService categoryService;

    @Resource
    private ProductAttrValueService productAttrValueService;

    @Resource
    private AttrService attrService;

    @Resource
    private SpuInfoService spuInfoService;

    @Resource
    private WareFeign wareFeign;

    @Resource
    private ThirdPartyFeign thirdPartyFeign;


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

    @Override
    public void skuUp(Long spuId) {
        // 1.根据spuId查询出所有的sku信息
        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("spu_id", spuId);
        List<SkuInfoEntity> list = this.list(wrapper);
        // 获取sku的id集合
        List<Long> skuIds = list.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());

        // 2.获取所有的品牌数据
        Set<Long> brandIds = list.stream().map(SkuInfoEntity::getBrandId).collect(Collectors.toSet());
        List<BrandEntity> brandEntities = brandService.listByIds(brandIds);
        Map<Long, BrandEntity> m1 = new HashMap<>();
        if (!CollectionUtils.isEmpty(brandEntities)) {
            m1 = brandEntities.stream().collect(Collectors.toMap(BrandEntity::getBrandId, item -> item));
        }
        // 3.获取所有的分类数据
        Set<Long> catalogIds = list.stream().map(SkuInfoEntity::getCatalogId).collect(Collectors.toSet());
        List<CategoryEntity> categoryEntities = categoryService.listByIds(catalogIds);
        Map<Long, CategoryEntity> m2 = new HashMap<>();
        if (!CollectionUtils.isEmpty(categoryEntities)) {
            m2 = categoryEntities.stream().collect(Collectors.toMap(CategoryEntity::getCatId, item -> item));
        }

        // 4.去属性表和spu属性关联表查：attrs:attrId,attrName,attrValue，
        List<AttrESTo> validAttrs = findValidAttrs(spuId);

        // 5.从gulimall_wms.wms_ware_sku中查出所有sku对应的库存信息
        List<WareSkuTo> wareSkuTos = wareFeign.getWareSkuByskuIds(skuIds);
        Map<Long, Boolean> m3 = wareSkuTos.stream().collect(Collectors.toMap(WareSkuTo::getSkuId, item -> item.getStock() > 0));

        // 6.封装sku的数据到SkuESTo
        Map<Long, BrandEntity> finalM = m1;
        Map<Long, CategoryEntity> finalM1 = m2;
        List<SkuESTo> skuESToList = list.stream().map(skuInfoEntity -> {
            // 最终上架的数据模型
            SkuESTo skuESTo = new SkuESTo();
            // 拷贝属性
            BeanUtils.copyProperties(skuInfoEntity, skuESTo);
            // price->skuPrice,skuDefaultImg->skuImg
            skuESTo.setSkuPrice(skuInfoEntity.getPrice());
            skuESTo.setSkuImg(skuInfoEntity.getSkuDefaultImg());

            // hasStock,从glmall-ware系统查出来
            if (m3.get(skuESTo.getSkuId()) != null) {
                skuESTo.setHasStock(true);
            } else {
                skuESTo.setHasStock(false);
            }

            // TODO hotScore,暂时给0
            skuESTo.setHotScore(0L);
            // 去brand表查 brandName,brandImg
            BrandEntity brandEntity = finalM.get(skuInfoEntity.getBrandId());
            skuESTo.setBrandName(brandEntity.getName());
            skuESTo.setBrandImg(brandEntity.getLogo());
            // catelogId,去分类表查 catalogName
            CategoryEntity categoryEntity = finalM1.get(skuInfoEntity.getCatalogId());
            skuESTo.setCatelogId(categoryEntity.getCatId());
            skuESTo.setCatalogName(categoryEntity.getName());

            // 设置sku的基础属性列表，每一个sku的spu规格参数都是冗余存储的，这块查询可以放到迭代外边
            skuESTo.setAttrs(validAttrs);

            return skuESTo;
        }).collect(Collectors.toList());

        // 6.将上架sku商品保存到es
        Boolean esResult = false;
        R r = null;
        try {
            r = thirdPartyFeign.saveSkuESTos(skuESToList);
        } catch (IOException e) {
            // TODO 7.如果发生io异常，我们需要启用重试机制
        }
        if (r.get("code").equals(0)) {
            esResult = true;
        }

        // 8.修改spu的发布状态为上架
        if (esResult) {
            SpuInfoEntity spuEntity = new SpuInfoEntity();
            spuEntity.setId(spuId);
            spuEntity.setPublishStatus(1);
            spuEntity.setUpdateTime(new Date());
            spuInfoService.updateById(spuEntity);
        }

    }

    @Resource
    private SkuImagesService skuImagesService;

    @Resource
    private SpuInfoDescService spuInfoDescService;

    @Resource
    private AttrGroupService attrGroupService;

    @Resource
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = new SkuItemVo();

        // 1.获取sku的基本信息 pms_sku_info
        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfoEntity info = getById(skuId);
            skuItemVo.setInfo(info);
            return info;
        }, threadPoolExecutor);

        // 3.获取sku的销售属性组合 pms_sku_sale_attr_value
        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync(result -> {
            Long spuId = result.getSpuId();
            List<SkuItemVo.ItemSaleAttrVo> saleAttrs = skuSaleAttrValueService.getSaleAttrsBySpuId(spuId);
            skuItemVo.setSaleAttrs(saleAttrs);
        }, threadPoolExecutor);

        // 4.获取spu的介绍 pms_spu_info_desc
        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync(result -> {
            Long spuId = result.getSpuId();
            QueryWrapper<SpuInfoDescEntity> spuInfoDescWrapper = new QueryWrapper<>();
            spuInfoDescWrapper.eq("spu_id", spuId);
            List<SpuInfoDescEntity> list = spuInfoDescService.list(spuInfoDescWrapper);
            skuItemVo.setDesp(CollectionUtils.isEmpty(list) ? null : list.get(0));
        }, threadPoolExecutor);

        // 5.获取规格参数信息
        CompletableFuture<Void> baseAttrFuture = infoFuture.thenAcceptAsync(result -> {
            Long spuId = result.getSpuId();
            Long catalogId = result.getCatalogId();
            // pms_product_attr_value,pms_attr_attrgroup_relation, pms_attr_group
            List<SkuItemVo.SpuItemAttrGroupVo> groupAttrs = attrGroupService.getAttrByAttrGroupWithAttrsBySpuId(catalogId, spuId);
            skuItemVo.setGroupAttrs(groupAttrs);
        }, threadPoolExecutor);

        // 2.获取sku的图片信息 pms_sku_images
        CompletableFuture<Void> imgFuture = CompletableFuture.runAsync(() -> {
            QueryWrapper<SkuImagesEntity> imgWrapper = new QueryWrapper<>();
            imgWrapper.eq("sku_id", skuId);
            List<SkuImagesEntity> images = skuImagesService.list(imgWrapper);
            skuItemVo.setImages(images);
        }, threadPoolExecutor);

        // 等待以上异步任务都完成才返回
        CompletableFuture.allOf(infoFuture, baseAttrFuture, saleAttrFuture, descFuture, imgFuture)
                .get();

        return skuItemVo;
    }

    @Override
    public List<SkuInfoEntity> getSkuInfoListByIds(List<Long> skuIds) {
        return this.list(new QueryWrapper<SkuInfoEntity>().in("sku_id", skuIds));
    }

    private List<AttrESTo> findValidAttrs(Long spuId) {
        QueryWrapper<ProductAttrValueEntity> w1 = new QueryWrapper<>();
        w1.eq("spu_id", spuId);
        List<ProductAttrValueEntity> productAttrValueEntities = productAttrValueService.list(w1);
        List<AttrESTo> attrs = new ArrayList<>();
        if (!CollectionUtils.isEmpty(productAttrValueEntities)) {
            List<Long> aIds = productAttrValueEntities.stream().map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());
            Map<Long, List<ProductAttrValueEntity>> map = productAttrValueEntities.stream().collect(Collectors.groupingBy(ProductAttrValueEntity::getAttrId));

            QueryWrapper<AttrEntity> w2 = new QueryWrapper<>();
            w2.in("attr_id", aIds).eq("enable", 1).eq("show_desc", 1);
            List<AttrEntity> list1 = attrService.list(w2);
            if (!CollectionUtils.isEmpty(list1)) {
                attrs = list1.stream().map(item -> {
                    AttrESTo attrESTo = new AttrESTo();
                    BeanUtils.copyProperties(item, attrESTo);
                    List<ProductAttrValueEntity> entities = map.get(item.getAttrId());
                    attrESTo.setAttrValue(entities != null ? entities.get(0).getAttrValue() : "");
                    return attrESTo;
                }).collect(Collectors.toList());
            }
        }
        return attrs;
    }

}
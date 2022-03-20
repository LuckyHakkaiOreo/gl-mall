package com.winster.glmall.glmallproduct.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.winster.common.to.*;
import com.winster.common.utils.PageUtils;
import com.winster.common.utils.Query;
import com.winster.common.utils.R;
import com.winster.glmall.glmallproduct.dao.SpuInfoDao;
import com.winster.glmall.glmallproduct.entity.*;
import com.winster.glmall.glmallproduct.feign.CouponFeign;
import com.winster.glmall.glmallproduct.feign.MemberFeign;
import com.winster.glmall.glmallproduct.service.*;
import com.winster.glmall.glmallproduct.vo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {
    @Resource
    private SpuInfoDescService spuInfoDescService;

    @Resource
    private SpuImagesService spuImagesService;

    @Resource
    private ProductAttrValueService productAttrValueService;

    @Resource
    private SkuInfoService skuInfoService;

    @Resource
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Resource
    private SkuImagesService skuImagesService;

    @Resource
    private AttrService attrService;

    @Resource
    private CouponFeign couponFeign;

    @Resource
    private MemberFeign memberFeign;

    @Resource
    private BrandService brandService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        String key = (String) params.get("key");

        Integer status = Integer.valueOf(StringUtils.isBlank((String) params.get("status")) ? "0" : (String) params.get("status"));
        Integer brandId = Integer.valueOf(StringUtils.isBlank((String) params.get("brandId")) ? "0" : (String) params.get("brandId"));
        Integer catelogId = Integer.valueOf(StringUtils.isBlank((String) params.get("catelogId")) ? "0" : (String) params.get("catelogId"));

        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        if (status != 0) {
            wrapper.eq("publish_status", status);
        }

        if (brandId != 0) {
            wrapper.eq("brand_id", brandId);
        }

        if (catelogId != 0) {
            wrapper.eq("catalog_id", catelogId);
        }

        if (StringUtils.isNotBlank(key)) {
            wrapper.and(w -> w.eq("id", key).or().like("spu_name", key));
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveSpuInfo(SpuInfoVo spuInfo) {
        // 1.保存spu的基本信息：pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        BeanUtils.copyProperties(spuInfo, spuInfoEntity);
        this.save(spuInfoEntity);
        Long spuId = spuInfoEntity.getId();

        // 2.保存spu的描述信息：pms_spu_info_desc
        List<String> decript = spuInfo.getDecript();
        List<SpuInfoDescEntity> spuInfoDescEntities = decript.stream().map(d -> {
            SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
            spuInfoDescEntity.setSpuId(spuId);
            spuInfoDescEntity.setDecript(d);
            return spuInfoDescEntity;
        }).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(spuInfoDescEntities)) {
            spuInfoDescService.saveBatch(spuInfoDescEntities);
        }

        // 3.保存spu的图片信息：pms_spu_images
        List<String> images = spuInfo.getImages();
        List<SpuImagesEntity> spuImagesEntities = images.stream().map(i -> {
            SpuImagesEntity spuImagesEntity = new SpuImagesEntity();
            spuImagesEntity.setSpuId(spuId);
            spuImagesEntity.setImgUrl(i);
            spuImagesEntity.setImgSort(0);
            return spuImagesEntity;
        }).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(spuImagesEntities)) {
            spuImagesService.saveBatch(spuImagesEntities);
        }

        // 4.保存spu的积分信息：glmall-coupon.sms_spu_bounds
        Bounds bounds = spuInfo.getBounds();
        SpuBoundsTo spuBoundsTo = new SpuBoundsTo();
        BeanUtils.copyProperties(bounds, spuBoundsTo);
        spuBoundsTo.setWork(1111);
        spuBoundsTo.setSpuId(spuId);
        couponFeign.save(spuBoundsTo);

        // 5.保存spu的基本属性（参数规格）信息：pms_product_attr_value
        List<BaseAttrs> baseAttrs = spuInfo.getBaseAttrs();
        List<ProductAttrValueEntity> productAttrValueEntities = baseAttrs.stream().map(b -> {
            ProductAttrValueEntity productAttrValueEntiry = new ProductAttrValueEntity();
            productAttrValueEntiry.setAttrId(b.getAttrId());
            productAttrValueEntiry.setSpuId(spuId);
            productAttrValueEntiry.setAttrValue(b.getAttrValues());
            productAttrValueEntiry.setAttrSort(0);
            productAttrValueEntiry.setQuickShow(b.getShowDesc());
            AttrEntity attrEntity = attrService.getById(b.getAttrId());
            productAttrValueEntiry.setAttrName(attrEntity.getAttrName());
            return productAttrValueEntiry;
        }).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(productAttrValueEntities)) {
            productAttrValueService.saveBatch(productAttrValueEntities);
        }
        // 6.保存spu的sku信息：
        //      6.1 保存sku的基本信息：pms_sku_info
        List<Skus> skus = spuInfo.getSkus();
        skus.forEach(sku -> {
            SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
            skuInfoEntity.setSpuId(spuId);
            skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
            skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
            Optional<Images> any = sku.getImages().stream().filter(image -> image.getDefaultImg() == 1).findAny();
            if (any.isPresent()) {
                skuInfoEntity.setSkuDefaultImg(any.get().getImgUrl());
            }
            skuInfoEntity.setSkuName(sku.getSkuName());
            skuInfoEntity.setPrice(sku.getPrice());
            skuInfoEntity.setSkuTitle(sku.getSkuTitle());
            skuInfoEntity.setSkuSubtitle(sku.getSkuSubtitle());
            // 保存sku的基本信息
            skuInfoService.save(skuInfoEntity);

            Long skuId = skuInfoEntity.getSkuId();
            //  6.2 保存sku的销售属性信息：pms_sku_sale_attr_value
            List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = sku.getAttr().stream().map(attr -> {
                SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                skuSaleAttrValueEntity.setSkuId(skuId);
                skuSaleAttrValueEntity.setAttrId(Long.valueOf(attr.getAttrId()));
                skuSaleAttrValueEntity.setAttrName(attr.getAttrName());
                skuSaleAttrValueEntity.setAttrValue(attr.getAttrValue());
                skuSaleAttrValueEntity.setAttrSort(0);
                return skuSaleAttrValueEntity;
            }).collect(Collectors.toList());

            if (!CollectionUtils.isEmpty(skuSaleAttrValueEntities)) {
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);
            }

            //      6.3 保存sku的图片信息：pms_sku_images
            List<SkuImagesEntity> skuImagesEntities = sku.getImages().stream().map(image -> {
                SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                skuImagesEntity.setSkuId(skuId);
                skuImagesEntity.setImgSort(0);
                skuImagesEntity.setImgUrl(image.getImgUrl());
                skuImagesEntity.setDefaultImg(image.getDefaultImg());
                return skuImagesEntity;
            }).filter(i -> StringUtils.isNotBlank(i.getImgUrl())).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(skuImagesEntities)) {
                skuImagesService.saveBatch(skuImagesEntities);
            }

            //      6.4 保存sku的打折信息：glmall-coupon.sms_sku_ladder
            /*"fullCount": 2,
                    "discount": 0.95,
                    "countStatus": 1,
                    */
            if (sku.getFullCount() != 0 && sku.getDiscount().doubleValue() != 0) {
                SkuLadderTo skuLadderTo = new SkuLadderTo();
                skuLadderTo.setSkuId(skuId);
                skuLadderTo.setFullCount(sku.getFullCount());
                skuLadderTo.setDiscount(sku.getDiscount());
                skuLadderTo.setPrice(sku.getPrice().multiply(new BigDecimal(sku.getFullCount())).multiply(sku.getDiscount()));
                skuLadderTo.setAddOther(sku.getCountStatus());
                couponFeign.save(skuLadderTo);
            }

            //      6.5 保存sku的满减信息：glmall-coupon.sms_sku_full_reduction
            if (sku.getFullPrice().doubleValue() != 0 && sku.getReducePrice().doubleValue() != 0) {
                SkuFullReductionTo skuFullReductionTo = new SkuFullReductionTo();
                skuFullReductionTo.setSkuId(skuId);
                skuFullReductionTo.setAddOther(sku.getPriceStatus());
                skuFullReductionTo.setFullPrice(sku.getFullPrice());
                skuFullReductionTo.setReducePrice(sku.getReducePrice());
                couponFeign.save(skuFullReductionTo);
            }

            //      6.6 保存sku的会员价格信息：glmall-coupon.sms_member_price
            R r = memberFeign.allList();
            List<MemberLevelTo> memberLevelToList = JSON
                    .parseArray(JSON.toJSONString(r.get("list")), MemberLevelTo.class);

            List<MemberPrice> memberPrice = sku.getMemberPrice();
            List<MemberPriceTo> memberPriceToList = memberPrice.stream().map(m -> {
                MemberPriceTo memberPriceTo = new MemberPriceTo();
                memberPriceTo.setSkuId(skuId);
                memberPriceTo.setMemberLevelName(m.getName());
                memberPriceTo.setMemberPrice(m.getPrice());
                memberPriceTo.setAddOther(1);
                Optional<MemberLevelTo> any1 = memberLevelToList.stream().filter(l -> l.getName().equals(m.getName())).findAny();
                if (any1.isPresent()) {
                    memberPriceTo.setMemberLevelId(any1.get().getId());
                }
                return memberPriceTo;
            }).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(memberPriceToList)) {
                couponFeign.save(memberPriceToList);
            }

        });
    }

    @Override
    public List<SpuInfoWithSkuIdTo> getSpuListBySkuIds(List<Long> skuIds) {

        List<SkuInfoEntity> skuList = skuInfoService.getSkuInfoListByIds(skuIds);
        if (CollectionUtils.isEmpty(skuList)) {
            return null;
        }

        List<Long> spuIds = skuList.stream().map(SkuInfoEntity::getSpuId).collect(Collectors.toList());
        List<Long> brandIds = skuList.stream().map(SkuInfoEntity::getBrandId).collect(Collectors.toList());
        List<SpuInfoEntity> spuList = this.listByIds(spuIds);

        List<BrandEntity> brandList = brandService.listByIds(brandIds);

        if (CollectionUtils.isEmpty(spuList)) {
            return null;
        }

        List<SpuInfoWithSkuIdTo> result = spuList.stream().map(item -> {
            SpuInfoWithSkuIdTo spuInfoWithSkuId = new SpuInfoWithSkuIdTo();
            List<Long> ids = skuList.stream().filter(i -> i.getSpuId().equals(item.getId())).map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
            SpuInfoTo spuInfoTo = new SpuInfoTo();
            BeanUtils.copyProperties(item, spuInfoTo);
            String brandName = brandList.stream().filter(b -> b.getBrandId().equals(spuInfoTo.getBrandId())).map(BrandEntity::getName).findAny().get();
            spuInfoTo.setBrandName(brandName);
            spuInfoWithSkuId.setSpuInfo(spuInfoTo);
            spuInfoWithSkuId.setSkuIds(ids);
            return spuInfoWithSkuId;
        }).collect(Collectors.toList());

        return result;
    }

}
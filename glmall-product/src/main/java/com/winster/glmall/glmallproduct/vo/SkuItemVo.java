package com.winster.glmall.glmallproduct.vo;

import com.winster.glmall.glmallproduct.entity.SkuImagesEntity;
import com.winster.glmall.glmallproduct.entity.SkuInfoEntity;
import com.winster.glmall.glmallproduct.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

@Data
public class SkuItemVo {
    private Long skuId;
    // 1.获取sku的基本信息 pms_sku_info
    private  SkuInfoEntity info;

    private boolean hasStock = true;

    // 2.获取sku的图片信息 pms_sku_images
    private List<SkuImagesEntity> images;

    // 3.获取sku的销售属性组合 pms_sku_sale_attr_value
    private List<ItemSaleAttrVo> saleAttrs;

    // 4.获取spu的介绍 pms_spu_info_desc
    private SpuInfoDescEntity desp;

    // 5.获取spu规格参数信息
    private List<SpuItemAttrGroupVo> groupAttrs;

    @Data
    public static class ItemSaleAttrVo {
        /**
         * 属性id
         */
        private Long attrId;
        /**
         * 属性名
         */
        private String attrName;
        private List<SaleAttrValueWithSkuIdVo> attrValues;
    }

    @Data
    public static class SaleAttrValueWithSkuIdVo {
        private String skuIds;
        private String attrValue;
    }

    @Data
    public static class SpuItemAttrGroupVo {
        private String groupName;
        private List<SpuBaseAttrVo> attrs;

    }

    @Data
    public static class SpuBaseAttrVo {
        private String attrName;
        private String attrValue;
    }
}

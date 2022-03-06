package com.winster.glmall.glmallsearch.vo;

import com.winster.common.to.es.SkuESTo;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SearchResp implements Serializable {

    private static final long serialVersionUID = 4165959903778816022L;
    // 查询到的商品信息
    private List<SkuESTo> products;
    private Long totals;
    private Integer pageNum;
    private Integer totalPages;
    private List<Integer> pageNavs;
    // 当前查询到的所有品牌
    private List<BrandVo> brands;
    // 当前查询到的结果所有的规格参数（基本属性）
    private List<AttrVo> attrs;
    // 当前查询到的结果所有分类信息
    private List<CatalogVo> catalogs;

    // 面包屑导航数据
    private List<NavVo> navs;

    @Data
    public static class NavVo {
        private String navName;
        private String navValue;
        private String link;
    }

    @Data
    public static class BrandVo {
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class AttrVo {
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }

    @Data
    public static class CatalogVo {
        private String catalogId;
        private String catalogName;
    }
}

package com.winster.glmall.glmallsearch.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 封装页面可能传递的所有查询条件
 * catalog3Id=225&keyword=小米&sort=saleCount_asc
 * &hasStock=1&skuPrice=1_500
 * &brandId=1&brandId=2
 * &attrs=1_其他:安卓attrs=2_8G:16G
 */
@Data
public class SearchParam implements Serializable {
    private static final long serialVersionUID = 8333222162990640835L;

    // 搜索框输入的关键字，用于全文匹配
    private String keyword;
    // 分类id
    private Long catalog3Id;
    /**
     * sort=saleCount_asc/desc
     * sort=skuPrice_asc/desc
     * sort=hotScore_asc/desc
     */
    private String sort;
    /**
     * 好多过滤条件
     * hasStock（是否有货）、skuPrice区间、brandId、attrs
     * hasStock=0/1
     * skuPrice=1_500/_500/500_
     * brandId=1
     * attrs=1_其他:安卓
     */
    private Integer hasStock;
    private String skuPrice;// 价格区间
    private List<Long> brandId;// 品牌id
    private List<String> attrs;// 其他属性
    private Integer pageNum;// 页码

    private String _aueryString;// 原生的查询条件
}

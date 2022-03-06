package com.winster.glmall.glmallsearch.service.impl;

import com.alibaba.fastjson.JSON;
import com.winster.common.constant.ThirdPartyServerConstant;
import com.winster.common.to.es.SkuESTo;
import com.winster.glmall.glmallsearch.service.MallSearchService;
import com.winster.glmall.glmallsearch.vo.SearchParam;
import com.winster.glmall.glmallsearch.vo.SearchResp;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Override
    public SearchResp search(SearchParam param) throws IOException {
        SearchResp result = new SearchResp();
        // 构建查询选项
        RequestOptions reqOps = RequestOptions.DEFAULT;

        // 1.配置查询条件
        SearchRequest searchReq = configSearchRequest(param);

        // 2.执行es查询请求
        SearchResponse response = restHighLevelClient.search(searchReq, reqOps);

        // 3.构建返回结果
        result = parseSearchResponse(param, response);
        return result;
    }

    /**
     * 配置查询条件
     *
     * @param param
     * @return
     */
    private SearchRequest configSearchRequest(SearchParam param) {
        /**
         * 1.添加查询条件
         */
        SearchRequest searchReq = new SearchRequest();

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // 1.0 query:{bool:{...}}
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // 1.1 bool:{match:{...}} => skuTitle
        String keyword = param.getKeyword();
        if (StringUtils.isNotBlank(keyword)) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", keyword));
        }
        // 1.2 bool:{filter:{term:{...}}} => catalog
        if (param.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catelogId", param.getCatalog3Id()));
        }
        // 1.3 bool:{filter:{terms:{...}}} => brandId
        if (!CollectionUtils.isEmpty(param.getBrandId())) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }
        // 1.4 bool:{filter:{term:{...}}} => hasStock
        if (param.getHasStock() != null) {
            boolQuery.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        }

        // 1.5 bool:{filter:{range:{...}}} => skuPrice
        String skuPrice = param.getSkuPrice();
        if (StringUtils.isNotBlank(skuPrice)) {
            RangeQueryBuilder q = QueryBuilders.rangeQuery("skuPrice");
            String[] r = param.getSkuPrice().split("_");
            if (skuPrice.startsWith("_")) {
                boolQuery.filter(q.lte(r[1]));
            } else if (skuPrice.endsWith("_")) {
                boolQuery.filter(q.gte(r[0]));
            } else if (skuPrice.contains("_")) {
                boolQuery.filter(q.gte(r[0]).lte(r[1]));
            }
        }

        // 1.6 bool:{filter:{nested}} => attrs
        if (!CollectionUtils.isEmpty(param.getAttrs())) {
            param.getAttrs().forEach(attr -> {
                String[] s = attr.split("_");
                String attrId = s[0];// 属性id
                String[] attrValue = s[1].split(":");// 属性值
                BoolQueryBuilder bq = QueryBuilders.boolQuery();
                bq.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                bq.must(QueryBuilders.matchQuery("attrs.attrValue", s[1]));
                NestedQueryBuilder n = QueryBuilders.nestedQuery("attrs", bq, ScoreMode.None);
                boolQuery.filter(n);
            });
        }

        sourceBuilder.query(boolQuery);

        /**
         *  2.排序，分页，高亮
         */
        String sort = param.getSort();
        if (StringUtils.isNotBlank(sort)) {
            String[] s = sort.split("_");
            SortOrder s1 = SortOrder.ASC.toString().equals(s[1]) ? SortOrder.ASC : SortOrder.DESC;
            sourceBuilder.sort(s[0], s1);
        }
        Integer pageNum = param.getPageNum();
        if (pageNum == null || pageNum <= 0) {
            pageNum = 1;
        }
        sourceBuilder.from((pageNum - 1) * ThirdPartyServerConstant.PRODUCT_PAGESIZE);
        sourceBuilder.size(ThirdPartyServerConstant.PRODUCT_PAGESIZE);

        if (StringUtils.isNotBlank(keyword)) {
            HighlightBuilder builder = new HighlightBuilder();
            builder.field("skuTitle")
                    .preTags("<b style='color:red'>")
                    .postTags("</b>");
            sourceBuilder.highlighter(builder);
        }

        /**
         * 3.添加聚合
         */
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brand_agg");
        brandAgg.field("brandId").size(50);
        TermsAggregationBuilder subAgg1 = AggregationBuilders.terms("brandName_agg");
        subAgg1.field("brandName").size(50);
        TermsAggregationBuilder subAgg2 = AggregationBuilders.terms("brandImg_agg");
        subAgg2.field("brandImg").size(50);

        brandAgg.subAggregation(subAgg1);
        brandAgg.subAggregation(subAgg2);
        sourceBuilder.aggregation(brandAgg);

        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalog_agg");
        catalogAgg.field("catelogId").size(20);
        TermsAggregationBuilder subAgg3 = AggregationBuilders.terms("catalogName_agg");
        subAgg3.field("catalogName").size(1);

        catalogAgg.subAggregation(subAgg3);
        sourceBuilder.aggregation(catalogAgg);

        // 属性聚合
        NestedAggregationBuilder attrAgg = AggregationBuilders.nested("nAttr", "attrs");
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attr_id_agg");
        attrIdAgg.field("attrs.attrId").size(10);
        TermsAggregationBuilder attrNameSubAgg = AggregationBuilders.terms("attr_name_sub_agg");
        attrNameSubAgg.field("attrs.attrName").size(1);

        TermsAggregationBuilder attrValueSubAgg = AggregationBuilders.terms("attr_value_sub_agg");
        attrValueSubAgg.field("attrs.attrValue.keyword").size(10);

        attrIdAgg.subAggregation(attrNameSubAgg);
        attrIdAgg.subAggregation(attrValueSubAgg);
        attrAgg.subAggregation(attrIdAgg);
        sourceBuilder.aggregation(attrAgg);

        // 打印dsl
        String dsl = sourceBuilder.toString();
        log.info(dsl);

        searchReq.indices(ThirdPartyServerConstant.PRODUCT_SKU_INFO_INDEX)
                .source(sourceBuilder);

        return searchReq;
    }

    /**
     * 解析返回结果
     *
     * @param response
     * @return
     */
    private SearchResp parseSearchResponse(SearchParam param, SearchResponse response) {
        SearchResp searchResp = new SearchResp();
        // 1. 返回所有查询到的商品
        SearchHits hits = response.getHits();
        SearchHit[] hits1 = hits.getHits();
        List<SkuESTo> skuESTos = new ArrayList<>();
        if (hits != null && hits1.length > 0) {
            skuESTos = Stream.of(hits1).map(hit -> {
                String sourceAsString = hit.getSourceAsString();
                SkuESTo skuESTo = JSON.parseObject(sourceAsString, SkuESTo.class);
                if (StringUtils.isNotBlank(param.getKeyword())) {
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String title = skuTitle.getFragments()[0].string();
                    skuESTo.setSkuTitle(title);
                }
                return skuESTo;
            }).collect(Collectors.toList());
        }
        searchResp.setProducts(skuESTos);

        // 品牌、分类、属性都是从es中获取出来的聚合数据
        Aggregations aggregations = response.getAggregations();
        if (aggregations != null) {
            // 2.当前所有商品涉及到的品牌信息
            List<SearchResp.BrandVo> brands = new ArrayList<>();
            ParsedLongTerms brand_agg = aggregations.get("brand_agg");
            if (brand_agg != null) {
                List<? extends Terms.Bucket> b1 = brand_agg.getBuckets();
                if (!CollectionUtils.isEmpty(b1)) {
                    b1.forEach(b -> {
                        SearchResp.BrandVo vo = new SearchResp.BrandVo();
                        vo.setBrandId(Long.valueOf(b.getKeyAsString()));
                        // 获取子聚合
                        Aggregations subAgg = b.getAggregations();
                        ParsedStringTerms brandImg_agg = subAgg.get("brandImg_agg");
                        List<? extends Terms.Bucket> bs1 = brandImg_agg.getBuckets();
                        if (!CollectionUtils.isEmpty(bs1)) {
                            String img = bs1.get(0).getKeyAsString();
                            vo.setBrandImg(img);
                        }
                        ParsedStringTerms brandName_agg = subAgg.get("brandName_agg");
                        List<? extends Terms.Bucket> bs2 = brandName_agg.getBuckets();
                        if (!CollectionUtils.isEmpty(bs2)) {
                            String name = bs2.get(0).getKeyAsString();
                            vo.setBrandName(name);
                        }
                        brands.add(vo);
                    });
                }
            }
            searchResp.setBrands(brands);
            // 3.当前所有商品的分类信息
            List<SearchResp.CatalogVo> catalogs = new ArrayList<>();
            ParsedLongTerms catalog_agg = aggregations.get("catalog_agg");
            if (catalog_agg != null) {
                List<? extends Terms.Bucket> buckets = catalog_agg.getBuckets();
                if (!CollectionUtils.isEmpty(buckets)) {
                    buckets.forEach(b -> {
                        SearchResp.CatalogVo vo = new SearchResp.CatalogVo();
                        vo.setCatalogId(b.getKeyAsString());
                        // 获取子聚合
                        Aggregations aggs = b.getAggregations();
                        ParsedStringTerms catalogName_agg = aggs.get("catalogName_agg");
                        List<? extends Terms.Bucket> bs1 = catalogName_agg.getBuckets();
                        if (!CollectionUtils.isEmpty(bs1)) {
                            String name = bs1.get(0).getKeyAsString();
                            vo.setCatalogName(name);
                        }
                        catalogs.add(vo);
                    });
                }
            }
            searchResp.setCatalogs(catalogs);
            // 4.当前所有商品所涉及到的属性信息
            List<SearchResp.AttrVo> attrs = new ArrayList<>();
            ParsedNested nAttr = aggregations.get("nAttr");
            if (nAttr != null) {
                Aggregations agg1 = nAttr.getAggregations();
                ParsedLongTerms attr_id_agg = agg1.get("attr_id_agg");
                if (attr_id_agg != null) {
                    List<? extends Terms.Bucket> buckets = attr_id_agg.getBuckets();
                    if (!CollectionUtils.isEmpty(buckets)) {
                        buckets.forEach(b -> {
                            SearchResp.AttrVo vo = new SearchResp.AttrVo();
                            vo.setAttrId(Long.valueOf(b.getKeyAsString()));
                            // 获取子聚合
                            Aggregations subAggs = b.getAggregations();
                            ParsedStringTerms attr_name_sub_agg = subAggs.get("attr_name_sub_agg");
                            List<? extends Terms.Bucket> bs1 = attr_name_sub_agg.getBuckets();
                            if (!CollectionUtils.isEmpty(bs1)) {
                                vo.setAttrName(bs1.get(0).getKeyAsString());
                            }

                            ParsedStringTerms attr_value_sub_agg = subAggs.get("attr_value_sub_agg");
                            List<? extends Terms.Bucket> bs2 = attr_value_sub_agg.getBuckets();
                            if (!CollectionUtils.isEmpty(bs2)) {
                                List<String> vals = bs2.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
                                vo.setAttrValue(vals);
                            }
                            attrs.add(vo);
                        });
                    }
                }
            }
            searchResp.setAttrs(attrs);
        }

        // 5.分页信息--页码、总记录数、总页码
        long total = hits.getTotalHits().value;
        Integer pageNum = param.getPageNum();
        // 总页码=(total+pageSize-1)/pageSize
        Long totalPages = (total + ThirdPartyServerConstant.PRODUCT_PAGESIZE - 1)
                / ThirdPartyServerConstant.PRODUCT_PAGESIZE;
        searchResp.setTotals(total);
        searchResp.setPageNum(pageNum == null ? 0 : pageNum);
        searchResp.setTotalPages(totalPages.intValue());
        List<Integer> pn = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pn.add(i);
        }
        searchResp.setPageNavs(pn);

        // 6. 构建面包屑导航数据
        /*searchResp.getAttrs().stream().map(attrVo -> {
            SearchResp.NavVo navVo = new SearchResp.NavVo();
            navVo.setNavValue(attrVo.getAttrValue().stream().collect(Collectors.joining(":")));
            navVo.setNavName(attrVo.getAttrName());

            return navVo;
        }).collect(Collectors.toList());*/
        if (!CollectionUtils.isEmpty(param.getAttrs())) {
            List<SearchResp.NavVo> navs =
                    param.getAttrs().stream().map(attr -> {
                        SearchResp.NavVo navVo = new SearchResp.NavVo();
                        String[] s = attr.split("_");
                        String id = s[0];
                        String v = s[1];
                        Optional<SearchResp.AttrVo> any = searchResp.getAttrs().stream().filter(av -> av.getAttrId().toString().equals(id)).findAny();
                        if (any.isPresent()) {
                            navVo.setNavValue(v);
                            navVo.setNavName(any.get().getAttrName());
                        }

                        // 2.  取消了这个面包屑后，我们要跳转到那个地方，将请求地址的url的当前置空
                        String aueryString = param.get_aueryString();
                        String encode = null;
                        try {
                            encode = URLEncoder.encode(attr, "UTF-8");
                            encode.replace(encode,"%20");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        String link = aueryString.replace("&attrs=" + encode, "");
                        navVo.setLink("http://search.glmall.com/list.html?" + link);
                        return navVo;
                    }).collect(Collectors.toList());
            searchResp.setNavs(navs);
        }

        return searchResp;
    }
}

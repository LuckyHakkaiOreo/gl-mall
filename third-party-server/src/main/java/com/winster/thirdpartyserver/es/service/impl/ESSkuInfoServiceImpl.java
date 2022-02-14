package com.winster.thirdpartyserver.es.service.impl;

import com.alibaba.fastjson.JSON;
import com.winster.common.constant.ThirdPartyServerConstant;
import com.winster.common.to.es.SkuESTo;
import com.winster.thirdpartyserver.es.service.ESSkuInfoService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ESSkuInfoServiceImpl implements ESSkuInfoService {

    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Override
    public Boolean saveSkuESTos(List<SkuESTo> list) throws IOException {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
        BulkRequest bulkReq = new BulkRequest(ThirdPartyServerConstant.PRODUCT_SKU_INFO_INDEX);
        list.forEach(item -> {
            IndexRequest indexReq = new IndexRequest();
            indexReq.id(item.getSkuId().toString());
            indexReq.source(JSON.toJSONString(item), XContentType.JSON);
            bulkReq.add(indexReq);
        });
        BulkResponse bulk = restHighLevelClient.bulk(bulkReq, builder.build());
        boolean b = bulk.hasFailures();

        if (b) {
            // 保存发生错误，需要记录日志，便于及时排查
            BulkItemResponse[] items = bulk.getItems();
            List<BulkItemResponse> errors = Arrays.asList(items).stream().filter(i -> i.isFailed()).collect(Collectors.toList());

            log.error("存在保存错误，错误数据：{}", errors);
            return false;
        }
        return true;
    }
}

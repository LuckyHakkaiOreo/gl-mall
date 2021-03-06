package com.winster.thirdpartyserver;

import com.alibaba.fastjson.JSON;
import com.winster.common.constant.ThirdPartyServerConstant;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.MinioException;
import lombok.Data;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;

import javax.annotation.Resource;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@SpringBootTest
class ThirdPartyServerApplicationTests {

    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Test
    void esBulkTest() throws IOException {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
        BulkRequest bulkReq = new BulkRequest(ThirdPartyServerConstant.PRODUCT_SKU_INFO_INDEX);
        IndexRequest indexReq = new IndexRequest();
        indexReq.id("i");
        indexReq.source("", XContentType.JSON);
        bulkReq.add(indexReq);
        restHighLevelClient.bulk(bulkReq, builder.build());
    }

    @Test
    void esTest() throws IOException {
        System.out.println(restHighLevelClient);
        System.out.println(elasticsearchRestTemplate);
        // ?????? ??????restHighLevelClient
        IndexRequest indexReq = new IndexRequest("users");
        indexReq.id("1");
        User user = new User();
        user.setName("??????");
        user.setAge(18);
        user.setGender("???");
        indexReq.source(JSON.toJSONString(user), XContentType.JSON);
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
        // builder.addHeader("Authorization", "Bearer"+ TOKEN);
        // builder.setHttpAsyncResponseConsumerFactory(
        //        new HttpAsyncResponseConsumerFactory
        //                .HeapBufferedResponseConsumerFactory(30*1024*1024*1024));
        IndexResponse index = restHighLevelClient.index(indexReq, builder.build());
        System.out.println(index);

        System.out.println("=============================================================");
        // ?????? ??????restHighLevelClient
        // ??????????????????
        SearchRequest searchReq = new SearchRequest();
        // ????????????
        searchReq.indices("bank");
        // ??????????????????
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
//        sourceBuilder.query();
//        sourceBuilder.from();
//        sourceBuilder.size();
//        sourceBuilder.aggregation()
        // ????????????
        QueryBuilder query = QueryBuilders.matchQuery("address", "mill");
        // ????????????????????????????????????
        TermsAggregationBuilder termsAggBuilder = AggregationBuilders.terms("ageAgg");
        termsAggBuilder.field("age").size(10);
        // ???????????????????????????
        //  termsAggBuilder.subAggregation(AggregationBuilder aggregation)
        // ???????????????????????????????????????????????????????????????
        AvgAggregationBuilder avgAggBuilder = AggregationBuilders.avg("balanceAvg");
        avgAggBuilder.field("balance");

        sourceBuilder.query(query);
        sourceBuilder.aggregation(termsAggBuilder);
        sourceBuilder.aggregation(avgAggBuilder);
        // ???????????????????????????????????????
        searchReq.source(sourceBuilder);

        SearchResponse searchResponse = restHighLevelClient.search(searchReq, builder.build());
        System.out.println(searchResponse);
        // ???????????????
        SearchHits hits = searchResponse.getHits();
        SearchHit[] hits1 = hits.getHits();
        for (SearchHit h : hits1) {
            // ??????????????????????????????
            /*
            * ??????hit????????????
            * {"_index": "bank",
			"_type": "account",
			"_id": "970",
			"_score": 5.4032025,
			"_source": {...}
            }
            * */
            // source???json???????????????
            String sourceAsString = h.getSourceAsString();
            System.out.println("source===>" + sourceAsString);
        }
        // ????????????????????????
        Aggregations aggregations = searchResponse.getAggregations();
        // ????????????????????????
        Terms ageAgg = aggregations.get("ageAgg");
        List<? extends Terms.Bucket> list = ageAgg.getBuckets();
        for (Terms.Bucket bucket : list) {
            String keyAsString = bucket.getKeyAsString();
            long docCount = bucket.getDocCount();
            System.out.println("???????????????key:" + keyAsString + ";docCount:" + docCount);
        }
        // ??????????????????????????????
        Avg balanceAvg = aggregations.get("balanceAvg");
        double value = balanceAvg.getValue();
        System.out.println("????????????????????????:" + value);


        // ???????????????9300???????????????es?????????es????????????????????????8.0??????es??????????????????9300???????????????????????????
        // The ElasticsearchTemplate is an implementation of the ElasticsearchOperations interface using the Transport Client.
    }


    @Test
    void contextLoads() {
    }

    @Data
    class User {
        private String name;
        private Integer age;
        private String gender;
    }

    @Test
    void uploadTest() {
        try {
            // Create a minioClient with the MinIO server playground, its access key and secret key.
            MinioClient minioClient =
                    MinioClient.builder()
                            .endpoint("http://192.168.0.196:9000/")
                            .credentials("51B6P21DHJFLAZPLHGUD", "iaZQUdMsSojTlZZqNNAslXiBzIQr1pr2S9YptR7w")
                            .build();

            // Make 'asiatrip' bucket if not exist.
            boolean found =
                    minioClient.bucketExists(BucketExistsArgs.builder().bucket("glmall").build());
            if (!found) {
                // Make a new bucket called 'asiatrip'.
                minioClient.makeBucket(MakeBucketArgs.builder().bucket("glmall").build());
            } else {
                System.out.println("Bucket 'glmall' already exists.");
            }

            // Upload '/home/user/Photos/asiaphotos.zip' as object name 'asiaphotos-2015.zip' to bucket
            // 'asiatrip'.
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket("glmall")
                    .object("2022-02-06/????????????.doc")
                    .filename("C:\\Users\\winst\\Desktop\\????????????.doc")
                    .build();
            minioClient.uploadObject(uploadObjectArgs);

            // ?????????endpoint??????????????????/?????????????????????bucket????????????
            System.out.println("http://192.168.0.196" + ":9001" + "/" + "glmall" + "/" + "2022-02-06/????????????.doc");

            System.out.println(
                    "'C:\\Users\\winst\\Desktop\\????????????.doc' is successfully uploaded as "
                            + "object '????????????.doc' to bucket 'asiatrip'.");
        } catch (MinioException e) {
            System.out.println("Error occurred: " + e);
            System.out.println("HTTP trace: " + e.httpTrace());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }

}

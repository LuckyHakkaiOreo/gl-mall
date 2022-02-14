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
        // 新增 使用restHighLevelClient
        IndexRequest indexReq = new IndexRequest("users");
        indexReq.id("1");
        User user = new User();
        user.setName("张三");
        user.setAge(18);
        user.setGender("男");
        indexReq.source(JSON.toJSONString(user), XContentType.JSON);
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
        // builder.addHeader("Authorization", "Bearer"+ TOKEN);
        // builder.setHttpAsyncResponseConsumerFactory(
        //        new HttpAsyncResponseConsumerFactory
        //                .HeapBufferedResponseConsumerFactory(30*1024*1024*1024));
        IndexResponse index = restHighLevelClient.index(indexReq, builder.build());
        System.out.println(index);

        System.out.println("=============================================================");
        // 检索 使用restHighLevelClient
        // 创建检索请求
        SearchRequest searchReq = new SearchRequest();
        // 指定索引
        searchReq.indices("bank");
        // 指定检索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
//        sourceBuilder.query();
//        sourceBuilder.from();
//        sourceBuilder.size();
//        sourceBuilder.aggregation()
        // 查询条件
        QueryBuilder query = QueryBuilders.matchQuery("address", "mill");
        // 数据聚合操作，求年龄分布
        TermsAggregationBuilder termsAggBuilder = AggregationBuilders.terms("ageAgg");
        termsAggBuilder.field("age").size(10);
        // 子聚合的话，这样写
        //  termsAggBuilder.subAggregation(AggregationBuilder aggregation)
        // 针对年龄分布进行子聚合，求所有人的平均薪资
        AvgAggregationBuilder avgAggBuilder = AggregationBuilders.avg("balanceAvg");
        avgAggBuilder.field("balance");

        sourceBuilder.query(query);
        sourceBuilder.aggregation(termsAggBuilder);
        sourceBuilder.aggregation(avgAggBuilder);
        // 设置查询条件，聚合运算条件
        searchReq.source(sourceBuilder);

        SearchResponse searchResponse = restHighLevelClient.search(searchReq, builder.build());
        System.out.println(searchResponse);
        // 解析返回值
        SearchHits hits = searchResponse.getHits();
        SearchHit[] hits1 = hits.getHits();
        for (SearchHit h : hits1) {
            // 解析每一个命中的数据
            /*
            * 每个hit中的数据
            * {"_index": "bank",
			"_type": "account",
			"_id": "970",
			"_score": 5.4032025,
			"_source": {...}
            }
            * */
            // source的json格式字符串
            String sourceAsString = h.getSourceAsString();
            System.out.println("source===>" + sourceAsString);
        }
        // 获取聚合分析数据
        Aggregations aggregations = searchResponse.getAggregations();
        // 获取年龄分布数据
        Terms ageAgg = aggregations.get("ageAgg");
        List<? extends Terms.Bucket> list = ageAgg.getBuckets();
        for (Terms.Bucket bucket : list) {
            String keyAsString = bucket.getKeyAsString();
            long docCount = bucket.getDocCount();
            System.out.println("年龄分布：key:" + keyAsString + ";docCount:" + docCount);
        }
        // 获取所有人的平均存款
        Avg balanceAvg = aggregations.get("balanceAvg");
        double value = balanceAvg.getValue();
        System.out.println("所有人的平均存款:" + value);


        // 这个是通过9300端口连接的es，不被es官方推荐的使用，8.0后的es不再支持使用9300的端口，所以废弃了
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
                    .object("2022-02-06/设计模式.doc")
                    .filename("C:\\Users\\winst\\Desktop\\设计模式.doc")
                    .build();
            minioClient.uploadObject(uploadObjectArgs);

            // 服务器endpoint地址，控制台/下载文件端口，bucket，文件名
            System.out.println("http://192.168.0.196" + ":9001" + "/" + "glmall" + "/" + "2022-02-06/设计模式.doc");

            System.out.println(
                    "'C:\\Users\\winst\\Desktop\\设计模式.doc' is successfully uploaded as "
                            + "object '设计模式.doc' to bucket 'asiatrip'.");
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

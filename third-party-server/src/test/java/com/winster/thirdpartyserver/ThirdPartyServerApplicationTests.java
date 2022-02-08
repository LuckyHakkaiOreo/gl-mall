package com.winster.thirdpartyserver;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.MinioException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@SpringBootTest
class ThirdPartyServerApplicationTests {

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
            System.out.println( "http://192.168.0.196"+":9001"+ "/" + "glmall" + "/" + "2022-02-06/设计模式.doc");

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

    @Test
    void contextLoads() {
    }

}

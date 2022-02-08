package com.winster.thirdpartyserver.minio.config;

import com.winster.thirdpartyserver.minio.properties.MinioProp;
import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
public class MinioConfiguration {

    @Resource
    private MinioProp minioProp;

    /**
     * 获取 MinioClient
     *
     * @return
     */
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioProp.getEndpoint() +":"+ minioProp.getUploadPort())
                .credentials(minioProp.getAccessKey(), minioProp.getSecretKey())
                .build();
    }
}

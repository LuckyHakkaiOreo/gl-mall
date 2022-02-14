package com.winster.glmall.glmallsearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * es交互的功能交给third-party-server来完成
 */
@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@Deprecated
public class GlmallSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(GlmallSearchApplication.class, args);
    }

}

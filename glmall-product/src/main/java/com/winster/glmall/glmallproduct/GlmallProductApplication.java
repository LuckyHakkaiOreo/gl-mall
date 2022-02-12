package com.winster.glmall.glmallproduct;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;

@EnableDiscoveryClient
@EnableFeignClients
// 扫描mapper对应的接口
@MapperScan("com.winster.glmall.glmallproduct.dao")
@SpringBootApplication
public class GlmallProductApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(GlmallProductApplication.class, args);
//        System.out.println(run.getEnvironment().getProperty("custom.mysql.name"));

    }

}

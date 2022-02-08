package com.winster.glmall.glmallorder;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;

@EnableFeignClients
@EnableDiscoveryClient
@MapperScan("com.winster.glmall.glmallorder.dao")
@SpringBootApplication
public class GlmallOrderApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(GlmallOrderApplication.class, args);
//        System.out.println(run.getEnvironment().getProperty("custom.mysql.name"));
    }

}

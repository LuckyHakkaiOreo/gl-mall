package com.winster.glmall.glmallware;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;

@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.winster.glmall.glmallware.dao")
@SpringBootApplication
public class GlmallWareApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(GlmallWareApplication.class, args);
//        System.out.println(run.getEnvironment().getProperty("custom.mysql.name"));

    }

}

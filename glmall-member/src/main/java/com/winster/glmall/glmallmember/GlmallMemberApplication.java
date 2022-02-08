package com.winster.glmall.glmallmember;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;

@EnableFeignClients
@EnableDiscoveryClient
@MapperScan("com.winster.glmall.glmallmember.dao")
@SpringBootApplication
public class GlmallMemberApplication {

    @Value("${server.port:9000}")
    private String port;

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(GlmallMemberApplication.class, args);
        /*while (true) {
            System.out.println(applicationContext.getEnvironment().getProperty("server.kkk"));
            System.out.println(applicationContext.getEnvironment().getProperty("test.str"));
        }*/
    }

}

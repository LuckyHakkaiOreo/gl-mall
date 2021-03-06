package com.winster.glmall.glmallware;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;

@Slf4j
@EnableRabbit
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.winster.glmall.glmallware.dao")
@SpringBootApplication
public class GlmallWareApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(GlmallWareApplication.class, args);

        log.info("----------------库存服务启动成功---------------");


        /*new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(run.getEnvironment().getProperty("spring.jackson.date-format"));
            }
        }).start();*/


    }

}

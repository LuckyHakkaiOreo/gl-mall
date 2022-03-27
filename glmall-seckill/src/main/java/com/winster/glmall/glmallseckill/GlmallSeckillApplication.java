package com.winster.glmall.glmallseckill;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;


@EnableAsync // 开启异步任务功能
@EnableScheduling // 启动定时任务
@EnableRedisHttpSession// 启用 httpSession
@EnableRabbit// 启用rabbitmq
@EnableFeignClients// 启用FeignClients
@EnableDiscoveryClient// 启用服务发现
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class GlmallSeckillApplication {

    public static void main(String[] args) {
        SpringApplication.run(GlmallSeckillApplication.class, args);
    }

}

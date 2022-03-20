package com.winster.glmall.glmallorder;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;


@EnableAspectJAutoProxy(exposeProxy = true) // 使用aspectJ的动态代理，实现本地事务（使用字节码代理，即使不实现接口也能使用动态代理）
@EnableRedisHttpSession// 启用 httpSession
@EnableRabbit// 启用rabbitmq
@EnableFeignClients// 启用FeignClients
@EnableDiscoveryClient// 启用服务发现
@MapperScan("com.winster.glmall.glmallorder.dao")
@SpringBootApplication
public class GlmallOrderApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(GlmallOrderApplication.class, args);
//        System.out.println(run.getEnvironment().getProperty("custom.mysql.name"));
    }

}

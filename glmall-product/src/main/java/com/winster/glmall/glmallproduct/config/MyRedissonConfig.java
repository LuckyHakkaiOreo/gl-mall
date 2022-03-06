package com.winster.glmall.glmallproduct.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class MyRedissonConfig {

    @Value("${spring.redis.host}")
    private String addr;

    @Value("${spring.redis.port}")
    private String port;

    @Value("${spring.redis.password}")
    private String password;

    @Bean(destroyMethod = "shutdown")
    RedissonClient redissonClient() throws IOException {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://"+addr+":"+port)
                .setPassword(password);


        return Redisson.create(config);
    }
}

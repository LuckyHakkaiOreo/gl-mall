package com.winster.glmall.glmallorder;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.winster.glmall.glmallorder.entity.OrderEntity;
import com.winster.glmall.glmallorder.service.OrderService;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;


@SpringBootTest
class GlmallOrderApplicationTests {

    @Resource
    private OrderService orderService;

    @Test
    void contextLoads() {

    }

}

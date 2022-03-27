package com.winster.glmall.glmallorder.web;

import com.winster.glmall.glmallorder.entity.OrderEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Date;
import java.util.UUID;

@Controller
public class HelloController {

    @Resource
    public RabbitTemplate rabbitTemplate;

    @GetMapping("/test/createOrder")
    @ResponseBody
    public String createOrderTest(){
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(UUID.randomUUID().toString());
        orderEntity.setModifyTime(new Date());

        rabbitTemplate.convertAndSend("order-event-exchange","order.create.order", orderEntity);

        return "success";
    }

    @RequestMapping("/{page}.html")
    public String toString(@PathVariable("page") String page) {
        return page;
    }
}

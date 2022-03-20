package com.winster.glmall.glmallorder;

import com.winster.glmall.glmallorder.entity.OrderReturnReasonEntity;
import com.winster.glmall.glmallorder.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;


@Slf4j
@SpringBootTest
class GlmallOrderApplicationTests {

    @Resource
    private OrderService orderService;

    @Resource
    private AmqpAdmin amqpAdmin;

    /**
     * 1.如何创建exchange、queue、Binding
     *  使用AmqpAdmin进行创建
     * 2.如何发送消息
     *
     */
    @Test
    void createExchange() {
        /**
         * DirectExchange(String, boolean, boolean, Map<String, Object>)
         */
        DirectExchange directExchange = new DirectExchange(
                "hello-java-exchange",
                true,
                false,
                null);
        amqpAdmin.declareExchange(directExchange);
        log.info("创建直连交换机：hello-java-exchange");
    }

    @Test
    void createQueue() {
        /**
         *     public Queue(String name, boolean durable, boolean exclusive, boolean autoDelete, @Nullable Map<String, Object> arguments) {
         */
        Queue queue = new Queue(
                "hello-java-queue",
                true,
                false, // 排他队列，仅允许一个消费者连接的队列
                false,
                null);
        amqpAdmin.declareQueue(queue);
        log.info("创建直连交换机：hello-java-queue");
    }

    @Test
    void createBinding() {
        /**
         *     public Binding(String destination, Binding.DestinationType destinationType, String exchange, String routingKey, @Nullable Map<String, Object> arguments)
         *
         *     将exchange指定的交换机和destination目的地进行绑定
         *     并且指定绑定的类型，使用routingkey指定路由
         */
        Binding binding = new Binding(
                "hello-java-queue",
                Binding.DestinationType.QUEUE,
                "hello-java-exchange",
                "hello.java",
                null);
        amqpAdmin.declareBinding(binding);
    }

    @Resource
    RabbitTemplate rabbitTemplate;

    @Test
    void testSendMessage() {
        /**
         * convertAndSend(String, String, Object, MessagePostProcessor, CorrelationData)
         */
        OrderReturnReasonEntity entity = new OrderReturnReasonEntity();
        //1. 发送消息
        rabbitTemplate.convertAndSend(
                "hello-java-exchange",
                "hello.java",
                entity,
                null,
                null
        );
    }



}

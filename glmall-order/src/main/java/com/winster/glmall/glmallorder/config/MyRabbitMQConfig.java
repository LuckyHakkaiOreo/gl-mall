package com.winster.glmall.glmallorder.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class MyRabbitMQConfig {

    @Resource
    private AmqpAdmin amqpAdmin;

    /**
     * 注入容器中的exchange、Queue、Binding都会自动创建
     * 一旦创建好队列，即使属性发生变化，也不会覆盖，
     * 需要删除queue后再次创建
     *
     * @return
     */
    @Bean
    public Queue orderDelayQueue() {
        Map<String, Object> arguments = new HashMap<>();
        // 死信队列，信死之时投递死信到哪个交换机
        arguments.put("x-dead-letter-exchange", "order-event-exchange");
        // 投递死信的时候，传递的routing-key是什么
        arguments.put("x-dead-letter-routing-key", "order.release.order");
        // 消息超时时间
        arguments.put("x-message-ttl", 60000);

        Queue queue = new Queue(
                "order.delay.queue",
                true,
                false, // 排他队列，仅允许一个消费者连接的队列
                false,
                arguments);
        return queue;
    }

    @Bean
    public Queue orderReleaseQueue() {
        Queue queue = new Queue(
                "order.release.queue",
                true,
                false, // 排他队列，仅允许一个消费者连接的队列
                false,
                null);
        return queue;
    }

    @Bean
    public Exchange orderEventExchange() {
        TopicExchange topicExchange = new TopicExchange(
                "order-event-exchange",
                true,
                false,
                null);

        return topicExchange;
    }

    @Bean
    public Binding orderCreateBinding() {
        Binding binding = new Binding(
                "order.delay.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.create.order",
                null);

        return binding;
    }

    @Bean
    public Binding orderReleaseBinding() {
        Binding binding = new Binding(
                "order.release.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.order",
                null);

        return binding;
    }

    /**
     * 得额外注入这么个方法，否则不会自动创建
     * 和视频内容描述的不一样
     */
    @Bean
    public void createRabbitMQObjects() {
        amqpAdmin.declareQueue(orderDelayQueue());
        amqpAdmin.declareQueue(orderReleaseQueue());
        amqpAdmin.declareBinding(orderCreateBinding());
        amqpAdmin.declareBinding(orderReleaseBinding());
        amqpAdmin.declareExchange(orderEventExchange());
    }

    /**
     * 判断订单是否超时关单
     */
    /*@RabbitListener(queues = "order.release.queue")
    public void releaseOrderListener(Message message,
                                     OrderEntity entity,
                                     Channel channel) throws IOException {
        System.out.println("收到超时订单：" + entity.getOrderSn());
        // 消息头
        MessageProperties properties = message.getMessageProperties();
        // channel内自增的整数
        long deliveryTag = properties.getDeliveryTag();
        channel.basicAck(deliveryTag, false);
    }*/

}

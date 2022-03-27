package com.winster.glmall.glmallware.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class MyRabbitConfig {

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Exchange stockEventExchange() {
        return new TopicExchange(
                "stock-event-exchange",
                true,
                false,
                null);
    }

    @Bean
    public Queue stockReleaseStockQueue() {
        return new Queue(
                "stock.release.queue",
                true,
                false,
                false,
                null);
    }

    @Bean
    public Queue stockDelayQueue() {
        Map<String, Object> arguments = new HashMap<>();
        // 死信队列，信死之时投递死信到哪个交换机
        arguments.put("x-dead-letter-exchange", "stock-event-exchange");
        // 投递死信的时候，传递的routing-key是什么
        arguments.put("x-dead-letter-routing-key", "stock.release");
        // 消息队列的超时时间
        arguments.put("x-message-ttl", 2 * 60 * 1000);

        return new Queue(
                "stock.delay.queue",
                true,
                false,
                false,
                arguments);
    }

    @Bean
    public Binding stockLockedBinding() {
        Binding binding = new Binding(
                "stock.delay.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.locked",
                null);

        return binding;
    }

    @Bean
    public Binding stockReleaseBinding() {
        Binding binding = new Binding(
                "stock.release.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.release.#",
                null);

        return binding;
    }

    /**
     * 如果不配置消费者，上边注入的对象是没有在rabbitmq中创建的
     * 监听库存释放队列
     */
    /*@RabbitListener(queues = "stock.release.queue")
    public void stockLockedListener(Message message,
                                     Channel channel) throws IOException {
        System.out.println("收到超时订单：" + JSON.toJSONString(message));
        // 消息头
        MessageProperties properties = message.getMessageProperties();
        // channel内自增的整数
        long deliveryTag = properties.getDeliveryTag();
        channel.basicAck(deliveryTag, false);
    }*/

}

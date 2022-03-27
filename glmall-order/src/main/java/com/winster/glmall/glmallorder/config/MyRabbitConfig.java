package com.winster.glmall.glmallorder.config;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Slf4j
@Configuration
public class MyRabbitConfig {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @PostConstruct
    public void initRabbitTemplate() {
        /*
         must check 只要消息投递到rabbitmq服务器便会触发这个方法

         todo
          如果消息的处理的量很大的话，我们可能需要将消息的处理单独抽成一个微服务
          1、需要对消息发送和接收的两端都进行消息的确认
          2、每一个发送的消息都在数据库做好记录。定期将失败的消息进行重新投递
         */
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(
                    CorrelationData correlationData,
                    boolean ack,
                    String cause) {
                // todo 消息已经抵达broker，修改消息的状态
                log.info("confirm...{},{},{}", correlationData, ack, cause);
            }
        });

        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            /**
             * must check 只要消息没有投递到指定的队列，便触发这个回调
             *  来到这个方法也就是消息发送到队列的时候，发生错误了
             *
             *  todo 在这里将消息设置为错误
             * @param message 投递失败消息的详细信息
             * @param replyCode 回复的状态码
             * @param replyText 回复的文本内容
             * @param exchange 当前消息发给哪个交换机
             * @param routingKey 当前消息使用的是哪个路由键
             */
            @Override
            public void returnedMessage(
                    Message message,
                    int replyCode,
                    String replyText,
                    String exchange,
                    String routingKey) {

                // todo 消息发送给队列的途中失败了，修改消息的状态
                log.info("returnedMessage...{}", JSON.toJSONString(message));
                log.info("returnedMessage2...{},{},{},{}", replyCode, replyText, exchange, routingKey);
            }
        });
    }
}

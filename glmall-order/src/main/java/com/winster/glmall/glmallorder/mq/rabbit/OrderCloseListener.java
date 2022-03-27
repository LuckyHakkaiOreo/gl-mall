package com.winster.glmall.glmallorder.mq.rabbit;

import com.rabbitmq.client.Channel;
import com.winster.glmall.glmallorder.entity.OrderEntity;
import com.winster.glmall.glmallorder.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * 订单关闭消息监听器
 */
@Slf4j
@Component
@RabbitListener(queues = "order.release.queue")
public class OrderCloseListener {

    @Resource
    private OrderService orderService;

    @RabbitHandler
    public void handleOrderClose(OrderEntity orderEntity, Message message, Channel channel) {
        try {
            log.info("收到订单关闭的消息:{},{},{}", orderEntity, message, channel);
            orderService.closeOrder(orderEntity);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("收到订单关闭消息消费异常", e);
            try {
                // 消息重入队
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}

package com.winster.glmall.glmallware.mq.rabbit;

import com.rabbitmq.client.Channel;
import com.winster.common.to.mq.StockLockedTo;
import com.winster.glmall.glmallware.service.WareSkuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * 库存锁定失败消息监听器
 */
@Slf4j
@Component
@RabbitListener(queues = "stock.release.queue")
public class StockReleaseListener {

    @Resource
    private WareSkuService wareSkuService;

    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo to, Message message, Channel channel) {
        /*log.info("收到库存解锁消息:{},{},{}",  message, to, channel);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);*/

        try {
            log.info("收到库存解锁消息:{},{},{}",  message, to, channel);
            wareSkuService.unlockStock(to, message, channel);
        } catch (IOException e) {
            log.error("库存解锁消息消费异常", e);
            try {
                // 消息重入队
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    /*
        todo 存在一种问题场景：
         考虑另外一种特殊场合，当由于各种原因导致订单关闭的时间太久；
         而最终导致库存解锁消息先被消费，由于订单状态是待付款状态，所以，库存不会被回滚。
         简而言之，就是【关单消息】在【解锁库存消息】之后被消费

        must check 解决方案：
         我们可以在【消费定时关单消息的时候 】，额外发送一条【库存解锁消息】，
         让库存系统消费掉这条【库存解锁消息】，从而给解锁库存额外加一个保险
     */
}

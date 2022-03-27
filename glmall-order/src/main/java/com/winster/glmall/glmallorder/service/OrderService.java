package com.winster.glmall.glmallorder.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rabbitmq.client.Channel;
import com.winster.common.utils.PageUtils;
import com.winster.glmall.glmallorder.entity.OrderEntity;
import com.winster.glmall.glmallorder.entity.OrderReturnReasonEntity;
import com.winster.glmall.glmallorder.vo.OrderConfirmVo;
import com.winster.glmall.glmallorder.vo.OrderSubmitResponseVo;
import com.winster.glmall.glmallorder.vo.OrderSubmitVo;
import org.springframework.amqp.core.Message;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author winster
 * @email winsterhandsome@gmail.com
 * @date 2022-02-04 07:45:50
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void recieveMsg(Message message, OrderReturnReasonEntity content, Channel channel);

    /**
     * 获取订单确认页面需要的所有数据
     * @return
     */
    OrderConfirmVo getConfirmVo() throws ExecutionException, InterruptedException;

    OrderSubmitResponseVo submitOrder(OrderSubmitVo vo);

    void closeOrder(OrderEntity orderEntity);
}


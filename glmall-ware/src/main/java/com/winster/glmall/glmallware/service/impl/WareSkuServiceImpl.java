package com.winster.glmall.glmallware.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbitmq.client.Channel;
import com.winster.common.to.OrderItemTo;
import com.winster.common.to.OrderTo;
import com.winster.common.to.WareSkuLockTo;
import com.winster.common.to.mq.StockLockedDetailTo;
import com.winster.common.to.mq.StockLockedTo;
import com.winster.common.utils.PageUtils;
import com.winster.common.utils.Query;
import com.winster.common.utils.R;
import com.winster.glmall.glmallware.dao.WareSkuDao;
import com.winster.glmall.glmallware.entity.PurchaseDetailEntity;
import com.winster.glmall.glmallware.entity.WareOrderTaskDetailEntity;
import com.winster.glmall.glmallware.entity.WareOrderTaskEntity;
import com.winster.glmall.glmallware.entity.WareSkuEntity;
import com.winster.glmall.glmallware.feign.OrderFeign;
import com.winster.glmall.glmallware.service.WareOrderTaskDetailService;
import com.winster.glmall.glmallware.service.WareOrderTaskService;
import com.winster.glmall.glmallware.service.WareSkuService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


@Slf4j
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private WareOrderTaskService wareOrderTaskService;

    @Resource
    private WareOrderTaskDetailService wareOrderTaskDetailService;

    @Resource
    public OrderFeign orderFeign;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();

        Integer skuId = Integer.valueOf(StringUtils.isBlank((String) params.get("skuId")) ? "0" : (String) params.get("skuId"));
        Integer wareId = Integer.valueOf(StringUtils.isBlank((String) params.get("wareId")) ? "0" : (String) params.get("wareId"));

        if (skuId != 0) {
            wrapper.eq("sku_id", skuId);
        }

        if (wareId != 0) {
            wrapper.eq("ware_id", wareId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(List<PurchaseDetailEntity> purchaseDetailEntities) {

        // 1.根据仓库id和sku_id，查询是否已经有创建过这这个商品的库存信息
        // 2.如果有，则累加数量，如果没有，则新增库存信息再累加
        // 创建没有的库存
        purchaseDetailEntities.forEach(e -> {
            QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("sku_id", e.getSkuId()).eq("ware_id", e.getWareId());
            WareSkuEntity target = null;
            List<WareSkuEntity> wareSkuEntities = this.baseMapper.selectList(wrapper);
            if (CollectionUtils.isEmpty(wareSkuEntities)) {
                // 需要新建该库存
                WareSkuEntity entity = new WareSkuEntity();
                entity.setSkuId(e.getSkuId());
                entity.setWareId(e.getWareId());
                entity.setStock(0);
                entity.setStockLocked(0);
                this.save(entity);
                target = entity;
            } else {
                target = wareSkuEntities.get(0);
            }
            target.setStock(e.getSkuNum() + target.getStock());
            UpdateWrapper<WareSkuEntity> w = new UpdateWrapper<>();
            w.eq("sku_id", e.getSkuId()).eq("ware_id", e.getWareId());
            this.update(target, w);
        });

    }

    @Override
    public List<WareSkuEntity> getWareSkuByskuIds(List<Long> skuIds) {
        QueryWrapper<WareSkuEntity> w = new QueryWrapper<>();
        w.in("sku_id", skuIds);
        List<WareSkuEntity> list = this.list(w);
        return list;
    }

    /**
     * @param to
     * @return
     */
    @Override
    public Boolean orderLockStock(WareSkuLockTo to) {
        // 为所有商品锁定库存，有一个商品失败，本次锁定都失败

        /**
         * todo
         *  为某个订单锁定库存（追溯锁定数据的）
         *
         * */
        WareOrderTaskEntity wareOrderTask = new WareOrderTaskEntity();
        wareOrderTask.setOrderSn(to.getOrderSn());
        wareOrderTaskService.save(wareOrderTask);

        // 保存库存工作单的详情

        // todo 按照下单的地址，找到就近的仓库，锁定商品库存，这里不实现

        // 1. 找到商品在库存中的信息
        List<OrderItemTo> itemList = to.getLocks();
        List<Long> skuIds = itemList.stream().map(OrderItemTo::getSkuId).collect(Collectors.toList());
        QueryWrapper<WareSkuEntity> w = new QueryWrapper<>();
        w.in("sku_id", skuIds);
        List<WareSkuEntity> wareSkuEntityList = this.list(w);
        // 针对商品对库存进行分组
        Map<Long, List<WareSkuEntity>> wsMap = wareSkuEntityList.stream().collect(Collectors.groupingBy(WareSkuEntity::getSkuId));

        // 记录，skuId -> 需要锁定的库存
        Map<Long, Integer> skuIdLockedMap = new HashMap<>();
        // 锁定指定商品的库存
        for (Map.Entry<Long, List<WareSkuEntity>> entry : wsMap.entrySet()) {
            Long skuId = entry.getKey();
            List<WareSkuEntity> wareSkuEntities = entry.getValue();

            if (CollectionUtils.isEmpty(wareSkuEntities)) {
                throw new RuntimeException(skuId + "商品没有库存了");
            }

            AtomicReference<Boolean> allFail = new AtomicReference<>(true);
            wareSkuEntities.forEach(wareSkuEntity -> {
                OrderItemTo orderItemTo = itemList.stream().filter(item -> item.getSkuId().equals(skuId)).findAny().get();
                // 需要锁定的库存
                Integer lockNum = orderItemTo.getCount();
                if (lockNum + wareSkuEntity.getStockLocked() > wareSkuEntity.getStock()) {
                    // 当前仓库数量不够，需要换下一个仓库来尝试锁定
                    return;
                }
                // 只要有一个仓库能够锁定，就不算失败
                wareSkuEntity.setStockLocked(lockNum + wareSkuEntity.getStockLocked());
                skuIdLockedMap.put(skuId, lockNum);
                allFail.set(false);
            });

            if (allFail.get()) {
                // 锁定失败则回滚
                throw new RuntimeException(skuId + "商品锁定库存失败");
            }
        }
        // 保存库存数据
        List<WareSkuEntity> entityList = wsMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        this.updateBatchById(entityList);

        // 保存订单锁定任务的记录，便于追溯
        List<WareOrderTaskDetailEntity> wareOrderTaskDetailList = entityList.stream()
                .map(wareSkuEntity -> {
                    WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity();
                    entity.setSkuId(wareSkuEntity.getSkuId());
                    entity.setSkuName(wareSkuEntity.getSkuName());
                    entity.setSkuNum(skuIdLockedMap.get(wareSkuEntity.getSkuId()));
                    entity.setWareId(wareSkuEntity.getWareId());
                    entity.setTaskId(wareOrderTask.getId());
                    // 1表示锁定
                    entity.setLockStatus(1);
                    return entity;
                }).collect(Collectors.toList());
        wareOrderTaskDetailService.saveBatch(wareOrderTaskDetailList);

        // 所有商品锁定成功，才发送库存锁定成功消息
        List<StockLockedDetailTo> detailIds = wareOrderTaskDetailList.stream().map(d -> {
            StockLockedDetailTo to1 = new StockLockedDetailTo();
            BeanUtils.copyProperties(d, to1);
            return to1;
        }).collect(Collectors.toList());
        StockLockedTo stockLockedTo = new StockLockedTo();
        stockLockedTo.setId(wareOrderTask.getId());
        stockLockedTo.setDetailIds(detailIds);

        // 发送库存锁定消息
        rabbitTemplate.convertAndSend(
                "stock-event-exchange",
                "stock.locked",
                stockLockedTo);

        return true;
    }

    /**
     * 库存自动解锁
     * 1、下单时候，库存锁定失败，导致订单没有生成
     * 那么库存没有锁定，这种场景我们无需处理
     * <p>
     * 2、下单的时候，库存锁定成功，但是订单创建异常回滚，
     * 那么此时需要我们根据订单锁定详情，将所有被锁定的库存，一一回滚
     *
     * @param to
     * @param message
     */
    @Transactional
    @Override
    public void unlockStock(StockLockedTo to, Message message, Channel channel) throws IOException {
        System.out.println("收到超时库存工作单：" + JSON.toJSONString(to));

        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        Long id = to.getId();
        List<StockLockedDetailTo> detailIds = to.getDetailIds();
        // 只要受到消息，说明已经成功锁定
        // 1、判断订单是否存在
        WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(id);

        // 远程查询订单状态
        R r = orderFeign.getOneByOrderSn(taskEntity.getOrderSn());
        if (0 != (Integer) r.get("code")) {
            log.error("库存任务解锁消息消费失败，查询不到订单：{}", taskEntity.getOrderSn());
            channel.basicReject(deliveryTag, true);
            return;
        }
        OrderTo order = JSON.parseObject(JSON.toJSONString(r.get("order")), OrderTo.class);

        /*
              must check 订单查询失败，
               说明正好遇到库存锁定成功，订单创建失败回滚的情况，
               此时该消息的库存信息是需要进行解锁的
               2、订单不存在 【或者】 订单被取消 /无效订单，需要解锁库存
               order.getStatus(): 4，已关闭；5，无效订单
         */
        if (order == null
                || order.getStatus() == 4
                || order.getStatus() == 5) {
            // 3、解锁库存操作：修改库存工作单详情状态
            detailIds.forEach(d1 -> {
                WareSkuEntity one = this.getOne(new QueryWrapper<WareSkuEntity>()
                        .eq("sku_id", d1.getSkuId())
                        .eq("ware_id", d1.getWareId()));
                one.setStockLocked(one.getStockLocked() - d1.getSkuNum());
                this.updateById(one);
            });

            // 修改库存任务详情状态
            List<WareOrderTaskDetailEntity> list = detailIds.stream().map(d -> {
                WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity();
                BeanUtils.copyProperties(d, entity);
                return entity;
            }).collect(Collectors.toList());
            // 状态置为解锁
            list.forEach(d -> d.setLockStatus(2));
            wareOrderTaskDetailService.updateBatchById(list);
        } else {
            // must check 其他情况，说明订单已经创建成功，【库存任务状态不需要解锁】
        }

        // 应答消息，明确消费了消息
        channel.basicAck(deliveryTag, false);

    }

}
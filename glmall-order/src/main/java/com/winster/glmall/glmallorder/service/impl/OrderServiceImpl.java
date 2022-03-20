package com.winster.glmall.glmallorder.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbitmq.client.Channel;
import com.winster.common.constant.OrderConstant;
import com.winster.common.to.*;
import com.winster.common.utils.PageUtils;
import com.winster.common.utils.Query;
import com.winster.common.utils.R;
import com.winster.glmall.glmallorder.dao.OrderDao;
import com.winster.glmall.glmallorder.entity.OrderEntity;
import com.winster.glmall.glmallorder.entity.OrderItemEntity;
import com.winster.glmall.glmallorder.entity.OrderReturnReasonEntity;
import com.winster.glmall.glmallorder.enums.OrderStatusEnum;
import com.winster.glmall.glmallorder.feign.CartFeign;
import com.winster.glmall.glmallorder.feign.MemberFeign;
import com.winster.glmall.glmallorder.feign.ProductFeign;
import com.winster.glmall.glmallorder.feign.WareFeign;
import com.winster.glmall.glmallorder.interceptor.LoginUserInterceptor;
import com.winster.glmall.glmallorder.service.OrderItemService;
import com.winster.glmall.glmallorder.service.OrderService;
import com.winster.glmall.glmallorder.to.OrderCreateTo;
import com.winster.glmall.glmallorder.vo.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

//    @RabbitListener(queues = {"hello-java-queue"})
@Slf4j
@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Resource
    private OrderServiceImpl orderService1;

    @Resource
    private OrderServiceImpl orderService2;

    @Resource
    private OrderItemService orderItemService;

    @Resource
    private MemberFeign memberFeign;

    @Resource
    private CartFeign cartFeign;

    @Resource
    private ProductFeign productFeign;

    @Resource
    private ThreadPoolExecutor myBizThreadPool;

    @Resource
    private WareFeign wareFeign;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * @param message 方法参数：
     *                1、Message message：原生消息详细信息（头+体）
     *                2、<发送的消息类型> OrderReturnReasonEntity content
     *                3、当前传输数据的信道： Channel channel
     */
    @Override
    public void recieveMsg(Message message,
                           OrderReturnReasonEntity content,
                           Channel channel) {
        // 消息体
        byte[] body = message.getBody();
        // 消息头
        MessageProperties properties = message.getMessageProperties();
        // channel内自增的整数
        long deliveryTag = properties.getDeliveryTag();

        // 手动确认消费了数据
        // 签收消息：deliveryTag, 是否批量签收：multiple
        try {
            if (deliveryTag % 2 == 0) {
                channel.basicAck(deliveryTag, false);
            } else {
                // deliveryTag，是否批量multiple，是否重新入队：requeue
                channel.basicNack(deliveryTag, false, true);
            }
        } catch (IOException e) {
            log.error("确认响应失败：", e);
        }
    }

    @Override
    public OrderConfirmVo getConfirmVo() throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        MemberLoginedRes loginedRes = LoginUserInterceptor.loginedResThreadLocal.get();
        String s = LoginUserInterceptor.idThreadLocal.get();

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        // 1.远程获取当前会员的收获地址
        log.info("当前线程：{}", s);
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            log.info("{}-当前线程：{}", "addressFuture", Thread.currentThread().getName());
            // 将主线程的请求数据同步到子线程
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> currentLoginUserAddressList = memberFeign.getCurrentLoginUserAddressList(loginedRes.getId());
            confirmVo.setAddressVoList(currentLoginUserAddressList);
        }, myBizThreadPool);

        // 2.远程查询当前会员被选中的购物车
        CompletableFuture<Void> cartFuture = CompletableFuture.supplyAsync(() -> {
            log.info("{}-当前线程：{}", "cartFuture#supplyAsync", Thread.currentThread().getName());
            // 将主线程的请求数据同步到子线程
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> currentLoginUserCheckedCartItemList = cartFeign.getCurrentLoginUserCheckedCartItemList();
            confirmVo.setItemVos(currentLoginUserCheckedCartItemList);
            return currentLoginUserCheckedCartItemList;
        }, myBizThreadPool).thenAccept((currentLoginUserCheckedCartItemList) -> {
            // thenAccept用的是上一次supplyAsync的线程；thenAcceptAsync用的是新线程：ForkJoinPool.commonPool-worker-9
            log.info("{}-当前线程：{}", "cartFuture#thenAccept", Thread.currentThread().getName());

            // 3.获取用户积分
            Integer integration = loginedRes.getIntegration();
            confirmVo.setIntegration(integration);

            // 4.计算订单总金额和应付金额
            BigDecimal total = new BigDecimal(0);
            BigDecimal payPrice = new BigDecimal(0);
            Optional<BigDecimal> reduce = currentLoginUserCheckedCartItemList.stream()
                    .map(item -> item.getPrice().multiply(new BigDecimal(item.getCount()))).reduce(BigDecimal::add);
            if (reduce.isPresent()) {
                total = reduce.get().setScale(2, RoundingMode.DOWN);
            }

            Integer totalCount = currentLoginUserCheckedCartItemList.stream().map(OrderItemVo::getCount).reduce(Integer::sum).get();

            confirmVo.setTotalAmount(total);
            payPrice = total;
            confirmVo.setPayPrice(payPrice);
            confirmVo.setTotalCount(totalCount);

            // 为当前确认页面设置防重令牌
            String token = UUID.randomUUID().toString().replace("-", "");
            stringRedisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + loginedRes.getId(), token, 30, TimeUnit.MINUTES);
            confirmVo.setOrderToken(token);
        });

        // 等待子线程执行完毕再结束
        CompletableFuture.allOf(addressFuture, cartFuture).get();

        return confirmVo;
    }

    /**
     * @GlobalTransactional 开启seata分布式事务
     * seata的at自动提交模式，由于依赖全局锁实现机制，性能上是不好的，这种下单场景
     * 在业务上是属于高并发场景的，那么at模式并不适合用在这种地方
     * at模式适合的场景：非高并发业务
     *
     * 那么，我们可以考虑使用tcc模式
     *
     * 或者我们使用【最大努力通知】或者【可靠消息回滚】的异步消息补偿机制来完成
     * 高并发下，分布式事务的场景
     * @param vo
     * @return
     */
    @GlobalTransactional
    @Transactional
    @Override
    public OrderSubmitResponseVo submitOrder(OrderSubmitVo vo) {
        log.info("com.winster.glmall.glmallorder.service.impl.OrderServiceImpl.submitOrder: s1==s2  =>  {}", orderService1==orderService2);
        log.info("com.winster.glmall.glmallorder.service.impl.OrderServiceImpl.submitOrder: s1==this  =>  {}", orderService1==this);
        log.info("com.winster.glmall.glmallorder.service.impl.OrderServiceImpl.submitOrder: s2==this  =>  {}", orderService2==this);
        // todo 下单操作：创建订单、验令牌、验价格、锁库存...
        MemberLoginedRes loginedRes = LoginUserInterceptor.loginedResThreadLocal.get();
        OrderSubmitResponseVo responseVo = new OrderSubmitResponseVo();
        responseVo.setCode(0);
        // 1.验证令牌
        String orderToken = vo.getOrderToken();

        // lua脚本原子删除，防止接口重复提交导致重复提交订单
        // 返回值：0，删除失败；1，删除成功
        // 【令牌对比和删除操作必须保证原子性】
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long executeResult = stringRedisTemplate.execute(new DefaultRedisScript<>(script, Long.class),
                Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + loginedRes.getId()),
                orderToken
        );

        if (executeResult != 1L) {
            // 验证失败
            responseVo.setCode(-1);
            return responseVo;
        }
        OrderCreateTo order = createOrder(vo);

        // 验价
        BigDecimal payAmount = order.getOrder().getPayAmount();
        BigDecimal payPrice = vo.getPayPrice();
        if (Math.abs(payAmount.subtract(payPrice).doubleValue()) > 0.01) {
            // 验价失败
            responseVo.setCode(-2);
            return responseVo;
        }

        // 保存订单和订单项到数据库
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        orderEntity.setMemberId(loginedRes.getId());
        orderEntity.setMemberUsername(loginedRes.getUsername());

        List<OrderItemEntity> orderItems = order.getOrderItems();
        // 4. 远程调用库存服务，锁定库存，在支付成功后再解除库存锁定
        WareSkuLockTo to = new WareSkuLockTo();
        to.setOrderSn(orderEntity.getOrderSn());

        List<OrderItemTo> orderItemTos = orderItems.stream().map(item -> {
            OrderItemTo to1 = new OrderItemTo();
            to1.setCount(item.getSkuQuantity());
            to1.setSkuId(item.getSkuId());
            return to1;
        }).collect(Collectors.toList());
        to.setLocks(orderItemTos);
        R r = wareFeign.orderLockStock(to);
        if ((Integer) r.get("code") != 0) {
            // 库存锁定失败
            responseVo.setCode(-3);
            return responseVo;
        }

        // 往数据库保存订单和订单项
        this.save(orderEntity);
        orderItems.forEach(item -> item.setOrderId(orderEntity.getId()));
        orderItemService.saveBatch(orderItems);

        // todo 模拟异常
        int i = 10/0;

        // 订单创建成功
        responseVo.setOrderEntity(orderEntity);
        return responseVo;
    }

    /**
     * 创建订单
     *
     * @param vo
     * @return
     */
    public OrderCreateTo createOrder(OrderSubmitVo vo) {
        OrderCreateTo orderCreateTo = new OrderCreateTo();

        // 1、构建订单
        OrderEntity orderEntity = buildOrder(vo);

        // 2、构建所有订单项
        List<OrderItemEntity> orderItemList = buildOrderItems(orderEntity.getOrderSn());
        orderCreateTo.setOrderItems(orderItemList);

        // 3. 计算订单价格、积分
        computeOrderPriceAndIntergration(orderEntity, orderItemList);

        orderCreateTo.setOrder(orderEntity);
        orderCreateTo.setPayPrice(orderEntity.getPayAmount());

        return orderCreateTo;
    }

    /**
     * 计算订单价格
     *
     * @param orderEntity
     * @param orderItemList
     */
    private void computeOrderPriceAndIntergration(OrderEntity orderEntity, List<OrderItemEntity> orderItemList) {
        BigDecimal couponAmount = new BigDecimal(0);
        BigDecimal integrationAmount = new BigDecimal(0);
        BigDecimal promotionAmount = new BigDecimal(0);
        BigDecimal realAmount = new BigDecimal(0);
        Integer intergration = 0;
        Integer growth = 0;

        for (OrderItemEntity entity : orderItemList) {
            couponAmount = couponAmount.add(entity.getCouponAmount());
            integrationAmount = integrationAmount.add(entity.getIntegrationAmount());
            promotionAmount = promotionAmount.add(entity.getPromotionAmount());
            realAmount = realAmount.add(entity.getRealAmount());
            intergration += entity.getGiftIntegration();
            growth += entity.getGiftGrowth();
        }

        orderEntity.setCouponAmount(couponAmount);
        orderEntity.setIntegrationAmount(integrationAmount);
        orderEntity.setPromotionAmount(promotionAmount);
        orderEntity.setTotalAmount(realAmount);
        orderEntity.setPayAmount(orderEntity.getTotalAmount().add(orderEntity.getFreightAmount()));

        orderEntity.setIntegration(intergration);
        orderEntity.setGrowth(growth);

    }

    /**
     * 构建订单项
     *
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {

        // 远程获取购物车中的订单项
        List<OrderItemVo> currentItems = cartFeign.getCurrentLoginUserCheckedCartItemList();
        List<Long> skuIds = currentItems.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
        // 远程获取spu的信息
        List<SpuInfoWithSkuIdTo> spuList = productFeign.getSpuListBySkuIds(skuIds);

        List<OrderItemEntity> itemList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(currentItems)) {
            itemList = currentItems.stream().map(item -> {
                OrderItemEntity orderItemEntity = buildOrderItemEntity(orderSn, spuList, item);

                return orderItemEntity;
            }).collect(Collectors.toList());
        }
        return itemList;
    }

    private OrderItemEntity buildOrderItemEntity(String orderSn, List<SpuInfoWithSkuIdTo> spuList, OrderItemVo item) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        // 1. 订单信息：订单号
        orderItemEntity.setOrderSn(orderSn);
        // 2. 商品的spu信息：通过当前skuId远程查询spuId，这块应该放在循环外
        Optional<SpuInfoWithSkuIdTo> any = spuList.stream().filter(i -> i.getSkuIds().contains(item.getSkuId())).findAny();
        if (any.isPresent()) {
            SpuInfoWithSkuIdTo info = any.get();
            SpuInfoTo spuInfo = info.getSpuInfo();
            orderItemEntity.setSpuId(spuInfo.getId());
            orderItemEntity.setSpuBrand(spuInfo.getBrandName());
            orderItemEntity.setSpuName(spuInfo.getSpuName());
//                    orderItemEntity.setSpuPic(spuInfo.get);
        }

        // 3. 商品的sku信息
        orderItemEntity.setSkuId(item.getSkuId());
        orderItemEntity.setSkuName(item.getTitle());
        orderItemEntity.setSkuPic(item.getPrice().toString());
        orderItemEntity.setSkuQuantity(item.getCount());
        String s = StringUtils.collectionToDelimitedString(item.getSkuAttr(), ";");
        orderItemEntity.setSkuAttrsVals(s);
        // 4. 优惠信息[不做]
        // 5. 积分信息
        orderItemEntity.setGiftGrowth(item.getPrice().intValue());
        orderItemEntity.setGiftIntegration(item.getPrice().intValue());

        BigDecimal origen = item.getPrice().multiply(new BigDecimal(item.getCount()));

        // 6.计算单个购物项的真实价格
        orderItemEntity.setIntegrationAmount(new BigDecimal(0));
        orderItemEntity.setPromotionAmount(new BigDecimal(0));
        orderItemEntity.setCouponAmount(new BigDecimal(0));
        BigDecimal realAmount = origen.subtract(orderItemEntity.getIntegrationAmount())
                .subtract(orderItemEntity.getPromotionAmount())
                .subtract(orderItemEntity.getCouponAmount());

        orderItemEntity.setRealAmount(realAmount);
        return orderItemEntity;
    }

    /**
     * 构建订单
     *
     * @param vo
     * @return
     */
    private OrderEntity buildOrder(OrderSubmitVo vo) {
        //1.生成一个订单号
        OrderEntity orderEntity = new OrderEntity();
        String orderSn = IdWorker.getTimeId();
        orderEntity.setOrderSn(orderSn);
        // 2.获取收货地址信息
        // todo 一般是远程调用，这里写死：运费
        BigDecimal fare = new BigDecimal(69);
        orderEntity.setFreightAmount(fare);
        R r = memberFeign.info(vo.getAddrId());
        MemberAddressVo address = JSON.parseObject(JSON.toJSONString(r.get("memberReceiveAddress")), MemberAddressVo.class);
        orderEntity.setReceiverProvince(address.getProvince());
        orderEntity.setReceiverCity(address.getCity());
        orderEntity.setReceiverRegion(address.getRegion());
        orderEntity.setReceiverDetailAddress(address.getDetailAddress());
        orderEntity.setReceiverName(address.getName());
        orderEntity.setReceiverPhone(address.getPhone());
        orderEntity.setReceiverPostCode(address.getPostCode());

        // 设置订单相关状态
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setAutoConfirmDay(7);

        return orderEntity;
    }

}
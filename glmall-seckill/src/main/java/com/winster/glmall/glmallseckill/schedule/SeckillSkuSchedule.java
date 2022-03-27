package com.winster.glmall.glmallseckill.schedule;

import com.winster.glmall.glmallseckill.common.contant.SeckillConstant;
import com.winster.glmall.glmallseckill.service.SeckillSkuService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀商品的定时上架功能：
 * 每天晚上3点，将需要上架的秒杀商品进行上架操作
 */
@Slf4j
@Component
public class SeckillSkuSchedule {

    @Resource
    private SeckillSkuService seckillSkuService;

    @Resource
    private RedissonClient redissonClient;


    /**
     * 每天凌晨3点，上架最近三天的商品
     * 当天00:00:00 - 23:59:59
     * 明天00:00:00 - 23:59:59
     * 后天00:00:00 - 23:59:59
     */
    @Scheduled(cron = "0 0/2 * * * ?")
    public void uploadSeckillSkuLast3Days() {
        // 1、已经上架过的商品无需重复上架
        log.info("上架定时任务开始工作...");
        /*
            must check 为了保证幂等性，我们不能让定时任务直接就上架商品，
             因为秒杀服务器我们可能会部署成集群模式
             使用分布式锁对定时任务进行限制，或者使用xxl-job或者quartz分布式定时任务框架，
             来保证定时任务不被重复执行
             我们这里使用的是分布式锁来进行控制，以后可以考虑引进分布式定时任务框架
         */
        RLock lock = redissonClient.getLock(SeckillConstant.SECKILL_UPLOAD_LOCK);
        lock.lock(1, TimeUnit.MINUTES);
        try {
            seckillSkuService.uploadSeckillSkuLast3Days();
            log.info("上架定时任务【正常】结束...");
        } catch (Exception e) {
            log.error("上架定时任务，发生异常...", e);
        } finally {
            log.info("上架定时任务结束...");
            lock.unlock();
        }
    }





    /*
        must check 开启一个定时任务
         1、spring不支持7位的cron表达式，第七位的年，不允许出现
         2、unix的cron中，周1-7表示SUN-SAT，而spring中1-7表示MON-SUN
         3、定时任务中不应该阻塞线程，当前定时任务没有执行完，下一次定时任务的时间会被延长
            解决办法：定时任务+异步任务 => 解决定时任务阻塞的问题
                        1）定时任务执行业务代码的时候，应该放在异步线程中
                            -> CompletableFuture.runAsync(new Runnable(){}, executor);
                        2）支持配置定时任务线程池（自测，不好使）:
                        自动配置类：TaskSchedulingAutoConfiguration
                        默认情况下TaskSchedulingProperties的线程池只配置了一个线程，
                        我们可以修改spring.task.scheduling.pool.size对应配置，配置更大的线程数
                            -> @ConfigurationProperties("spring.task.scheduling")
                                    public class TaskSchedulingProperties {
                        3）直接使用 @Async 让定时任务异步执行，可以参考TaskExecutionAutoConfiguration
                        里的@ConfigurationProperties("spring.task.execution")
                                public class TaskExecutionProperties {
                                	private final Pool pool = new Pool();
                                    ...
                                }
                        对 Async 异步线程池进行定制
         4、*正斜杠6表示的含义是，整除6的秒数
     */
//    @Async
//    @Scheduled(cron = "*/6 * * ? * SUN")
    /*public void hello() throws InterruptedException {
        log.info("hello");
        Thread.sleep(7000);
    }*/
}

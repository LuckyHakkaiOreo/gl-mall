package com.winster.glmall.glmallseckill.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HelloSchedule {

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

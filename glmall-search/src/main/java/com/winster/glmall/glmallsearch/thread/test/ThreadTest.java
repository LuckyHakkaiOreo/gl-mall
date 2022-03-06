package com.winster.glmall.glmallsearch.thread.test;

import java.util.concurrent.*;

/**
 * 线程池执行流程：
 * 1.当一个任务交给线程池处理的时候,如果没达到【核心线程数】，则新建一个线程来执行任务
 * 2.【当核心线程满了】，则需要将新进来的线程保存到【工作队列】中
 * 3.如果此时，【工作队列满了】但是【最大线程数还没满】，则需要启动一个新的线程来执行该任务
 * <p>
 * 4.如果【工作队列满了】并且【最大线程数也满了】，则需要根据【拒绝策略】来执行拒绝逻辑，
 * 默认的拒绝策略：AbortPolicy（丢弃最新的，抛异常），
 * 此外还有：DiscardPolicy（丢弃最新的，不抛异常）、DiscardOldestPolicy（丢弃最老的）、CallerRunsPolicy（直接同步运行任务的run方法）
 * 也可以自定义异常策略：实现RejectedExecutionHandler接口
 * <p>
 * 5.当任务执行完毕了，线程池中会剩下很多的空闲线程，这些空闲线程会等到【最大存活时间】后，
 * 被销毁（销毁的个数=maximumPoolSize-corePoolSize）,线程吃中会允许corePoolSize个线程一直保留着；
 * 但如果我们设置allowCoreThreadTimeOut为true，则超时后，核心线程也会被销毁
 * <p>
 * 6.另外注意：设置【工作阻塞队列】的时候，一定要设置容量，因为很多工作队列的默认容量都很大：Integer.MAX_VALUE
 * 如果我们忘记设置了工作队列的容量，则的工作队列一直不会装满，也就不会开启超过【核心线程数】的线程来工作；
 * 另外一方面，一旦遇到高并发的场景，很可能会导致我们内存被耗尽，抛出oom
 */
public class ThreadTest {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(
                0,   // 核心线程数
                200,// 最大线程数
                3,// 最大存活时间
                TimeUnit.SECONDS,// 最大存活时间单位
                new LinkedBlockingQueue<>(10 * 10000),// 工作队列
                Executors.defaultThreadFactory(),// 线程工厂
                new ThreadPoolExecutor.AbortPolicy());// 异常处理拒绝策略

        /*CompletableFuture<Void> c1 = CompletableFuture.runAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getName());
        }, poolExecutor);*/

        // get方法会阻塞当前线程，直到子线程执行完毕才能继续执行
//        c1.get();
        System.out.println("主线程!!!!");
        /*CompletableFuture<String> c2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getName());
            int a = 10 / 0;
            return Thread.currentThread().getName();
        }, poolExecutor).whenCompleteAsync((result, exception) -> {
            // 不指明线程池：ForkJoinPool.commonPool-worker-9
            System.out.println("结果是：" + result + "==>" + Thread.currentThread().getName());
            System.out.println("异常是：" + exception + "==>" + Thread.currentThread().getName());
        }).exceptionally(throwable -> {
            return "程序异常:" + throwable.getMessage()+ "==>" + Thread.currentThread().getName();
        });
        String s = c2.get();
        System.out.println("ssss:" + s);*/

        /*
        whenCompleteAsync和whenComplete的区别：
        whenCompleteAsync方法会由线程池中选一个线程去执行，也可能是之前的线程；
        whenComplete方法会由之前执行该任务的线程去执行；
         */

        // ========================================handleAsync========================================
        /*CompletableFuture<String> c3 = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getName());
            int a = 10 / 3;
            return Thread.currentThread().getName() + a;
        }, poolExecutor).handleAsync((result, throwable) ->{
            // 这是在默认线程池执行的：pool-1-thread-1
            String s1 = throwable == null ? "程序正常结束：" + Thread.currentThread().getName() : "程序异常结束：" + throwable.getMessage();
            return s1;
        }, poolExecutor);*/

        /*.handleAsync((result, throwable) ->{
            // 这是在默认线程池执行的：ForkJoinPool.commonPool-worker-9
            String s1 = throwable == null ? "程序正常结束：" + Thread.currentThread().getName() : "程序异常结束：" + throwable.getMessage();
            return s1;
        });*/
/*.handle((result, throwable) -> {
            // 这是在主线程执行的！
            String s1 = throwable == null ? "程序正常结束：" + Thread.currentThread().getName() : "程序异常结束：" + throwable.getMessage();
            return s1;
        });*/
//        String s = c3.get();
//        System.out.println(s);



    }

    public static void main1(String[] args) {
        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(
                5,   // 核心线程数
                200,// 最大线程数
                3 * 60,// 最大存活时间
                TimeUnit.SECONDS,// 最大存活时间单位
                new LinkedBlockingQueue<>(10 * 10000),// 工作队列
                Executors.defaultThreadFactory(),// 线程工厂
                new ThreadPoolExecutor.AbortPolicy());// 异常处理拒绝策略
    }

    public static class Thread01 extends Thread {
        @Override
        public void run() {
            System.out.println("当前线程：");
            System.out.println("当前线程：");
        }
    }
}

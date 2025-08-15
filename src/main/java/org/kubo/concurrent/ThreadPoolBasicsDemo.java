package org.kubo.concurrent;

import java.util.concurrent.*;

/**
 * ThreadPoolExecutor核心概念快速演示
 */
public class ThreadPoolBasicsDemo {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== ThreadPoolExecutor 核心线程数动态修改演示 ===\n");
        
        // 创建线程池
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            2,                              // 初始核心线程数
            4,                              // 最大线程数  
            60L,                            // 非核心线程存活时间
            TimeUnit.SECONDS,               // 时间单位
            new LinkedBlockingQueue<>(3),   // 工作队列
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
        
        System.out.println("1. 初始状态:");
        printStatus(executor);
        
        // 提交任务
        System.out.println("\n2. 提交4个任务:");
        for (int i = 1; i <= 4; i++) {
            final int taskId = i;
            executor.submit(() -> {
                System.out.println("执行任务 " + taskId + " - 线程: " + Thread.currentThread().getName());
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("完成任务 " + taskId);
            });
        }
        
        Thread.sleep(1000);
        printStatus(executor);
        
        // 关键：动态修改核心线程数
        System.out.println("\n3. ✨ 动态修改核心线程数从2增加到4:");
        executor.setCorePoolSize(4);
        System.out.println("   setCorePoolSize(4) 调用完成!");
        
        Thread.sleep(1000);
        printStatus(executor);
        
        // 再次修改
        System.out.println("\n4. ✨ 动态修改核心线程数从4减少到1:");
        executor.setCorePoolSize(1);
        System.out.println("   setCorePoolSize(1) 调用完成!");
        
        Thread.sleep(2000);
        printStatus(executor);
        
        // 清理
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        
        System.out.println("\n=== 演示完成 ===");
        System.out.println("\n💡 关键知识点:");
        System.out.println("• setCorePoolSize() 方法可以在运行时动态修改核心线程数");
        System.out.println("• 增加核心线程数：立即创建新的核心线程");
        System.out.println("• 减少核心线程数：多余的核心线程会在空闲时被回收");
        System.out.println("• 这种动态调整对线程池性能调优非常有用");
    }
    
    private static void printStatus(ThreadPoolExecutor executor) {
        System.out.printf("   核心线程数: %d | 当前线程数: %d | 活跃线程数: %d | 队列任务数: %d%n",
            executor.getCorePoolSize(),
            executor.getPoolSize(), 
            executor.getActiveCount(),
            executor.getQueue().size()
        );
    }
}

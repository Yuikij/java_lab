package org.kubo.concurrent;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ThreadPoolExecutor 详细演示demo
 * 包含：
 * 1. 线程池基础知识和参数说明
 * 2. 动态修改核心线程数
 * 3. 线程池状态监控
 * 4. 拒绝策略演示
 */
public class ThreadPoolExecutorDemo {
    

    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== ThreadPoolExecutor 详细演示 ===\n");
        
        // 演示1: 基础线程池创建和参数说明
        demonstrateBasicThreadPool();
        
        Thread.sleep(1000);
        
        // 演示2: 动态修改核心线程数
        demonstrateDynamicCorePoolSize();
        
        Thread.sleep(1000);
        
        // 演示3: 线程池状态监控
        demonstrateThreadPoolMonitoring();
        
        Thread.sleep(1000);
        
        // 演示4: 拒绝策略演示
        demonstrateRejectionPolicies();
    }
    
    /**
     * 演示1: ThreadPoolExecutor基础知识
     */
    private static void demonstrateBasicThreadPool() {
        System.out.println("=== 1. ThreadPoolExecutor基础知识演示 ===");
        
        /*
         * ThreadPoolExecutor构造参数详解：
         * 1. corePoolSize: 核心线程数 - 池中始终保持的线程数量
         * 2. maximumPoolSize: 最大线程数 - 池中允许的最大线程数量
         * 3. keepAliveTime: 线程存活时间 - 非核心线程闲置多长时间后被回收
         * 4. unit: 时间单位 - keepAliveTime的时间单位
         * 5. workQueue: 工作队列 - 存储等待执行的任务
         * 6. threadFactory: 线程工厂 - 创建新线程时使用
         * 7. handler: 拒绝策略 - 任务无法执行时的处理策略
         */
        
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            2,                              // 核心线程数
            4,                              // 最大线程数
            60L,                            // 线程存活时间
            TimeUnit.SECONDS,               // 时间单位
            new LinkedBlockingQueue<>(2),   // 有界队列，容量为2
            new CustomThreadFactory("Demo"), // 自定义线程工厂
            new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略：调用者执行
        );
        
        System.out.println("初始线程池状态:");
        printThreadPoolStatus(executor);
        
        // 提交一些任务
        for (int i = 1; i <= 3; i++) {
            executor.submit(new SampleTask("Task-" + i, 2000));
        }
        
        // 等待一段时间后查看状态
        try {
            Thread.sleep(1000);
            System.out.println("\n提交3个任务后的状态:");
            printThreadPoolStatus(executor);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
        
        System.out.println("\n=== 基础演示结束 ===\n");
    }
    
    /**
     * 演示2: 动态修改核心线程数
     */
    private static void demonstrateDynamicCorePoolSize() throws InterruptedException {
        System.out.println("=== 2. 动态修改核心线程数演示 ===");
        
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            2,                              // 初始核心线程数
            6,                              // 最大线程数
            30L,                            // 线程存活时间
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(10),  // 队列容量
            new CustomThreadFactory("Dynamic"),
            new ThreadPoolExecutor.AbortPolicy()
        );
        
        System.out.println("初始配置 - 核心线程数: " + executor.getCorePoolSize());
        printThreadPoolStatus(executor);
        
        // 提交一些长时间运行的任务
        for (int i = 1; i <= 5; i++) {
            executor.submit(new SampleTask("Initial-" + i, 3000));
        }
        
        Thread.sleep(1000);
        System.out.println("\n提交5个任务后:");
        printThreadPoolStatus(executor);
        
        // 动态增加核心线程数
        System.out.println("\n--- 动态增加核心线程数到5 ---");
        executor.setCorePoolSize(5);
        System.out.println("核心线程数已修改为: " + executor.getCorePoolSize());
        
        Thread.sleep(1000);
        System.out.println("\n增加核心线程数后:");
        printThreadPoolStatus(executor);
        
        // 再提交一些任务
        for (int i = 1; i <= 3; i++) {
            executor.submit(new SampleTask("Additional-" + i, 2000));
        }
        
        Thread.sleep(1000);
        System.out.println("\n再次提交任务后:");
        printThreadPoolStatus(executor);
        
        // 动态减少核心线程数
        System.out.println("\n--- 动态减少核心线程数到3 ---");
        executor.setCorePoolSize(3);
        System.out.println("核心线程数已修改为: " + executor.getCorePoolSize());
        
        Thread.sleep(2000);
        System.out.println("\n减少核心线程数后:");
        printThreadPoolStatus(executor);
        
        // 关闭线程池
        executor.shutdown();
        if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }
        
        System.out.println("\n=== 动态修改演示结束 ===\n");
    }
    
    /**
     * 演示3: 线程池状态监控
     */
    private static void demonstrateThreadPoolMonitoring() throws InterruptedException {
        System.out.println("=== 3. 线程池状态监控演示 ===");
        
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            3, 6, 60L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(5),
            new CustomThreadFactory("Monitor"),
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
        
        // 启动监控线程
        Thread monitorThread = new Thread(new ThreadPoolMonitor(executor));
        monitorThread.setDaemon(true);
        monitorThread.start();
        
        System.out.println("开始监控线程池状态...\n");
        
        // 分阶段提交任务
        System.out.println("第1阶段: 提交3个任务");
        for (int i = 1; i <= 3; i++) {
            executor.submit(new SampleTask("Monitor-1-" + i, 2000));
        }
        Thread.sleep(1500);
        
        System.out.println("\n第2阶段: 再提交5个任务");
        for (int i = 1; i <= 5; i++) {
            executor.submit(new SampleTask("Monitor-2-" + i, 3000));
        }
        Thread.sleep(1500);
        
        System.out.println("\n第3阶段: 动态调整核心线程数到5");
        executor.setCorePoolSize(5);
        Thread.sleep(2000);
        
        System.out.println("\n第4阶段: 再提交3个任务");
        for (int i = 1; i <= 3; i++) {
            executor.submit(new SampleTask("Monitor-3-" + i, 1000));
        }
        Thread.sleep(3000);
        
        executor.shutdown();
        if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }
        
        System.out.println("\n=== 监控演示结束 ===\n");
    }
    
    /**
     * 演示4: 拒绝策略演示
     */
    private static void demonstrateRejectionPolicies() throws InterruptedException {
        System.out.println("=== 4. 拒绝策略演示 ===");
        
        // 创建一个容易饱和的线程池
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            1, 2, 30L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(2),  // 很小的队列
            new CustomThreadFactory("Rejection"),
            new CustomRejectionHandler()  // 自定义拒绝策略
        );
        
        System.out.println("线程池配置: 核心线程数=1, 最大线程数=2, 队列容量=2");
        
        // 提交超过容量的任务
        for (int i = 1; i <= 8; i++) {
            try {
                executor.submit(new SampleTask("Reject-" + i, 3000));
                System.out.println("成功提交任务: Reject-" + i);
            } catch (RejectedExecutionException e) {
                System.out.println("任务被拒绝: Reject-" + i + " - " + e.getMessage());
            }
            Thread.sleep(200);
        }
        
        Thread.sleep(2000);
        printThreadPoolStatus(executor);
        
        executor.shutdown();
        if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }
        
        System.out.println("\n=== 拒绝策略演示结束 ===\n");
    }
    
    /**
     * 打印线程池详细状态
     */
    private static void printThreadPoolStatus(ThreadPoolExecutor executor) {
        System.out.println("┌─────────────────────────────────────────┐");
        System.out.println("│           线程池状态信息                   │");
        System.out.println("├─────────────────────────────────────────┤");
        System.out.printf("│ 核心线程数: %-8d 最大线程数: %-8d │%n", 
            executor.getCorePoolSize(), executor.getMaximumPoolSize());
        System.out.printf("│ 当前线程数: %-8d 活跃线程数: %-8d │%n", 
            executor.getPoolSize(), executor.getActiveCount());
        System.out.printf("│ 队列大小: %-10d 剩余容量: %-8d │%n", 
            executor.getQueue().size(), executor.getQueue().remainingCapacity());
        System.out.printf("│ 已完成任务: %-8d 总任务数: %-10d │%n", 
            executor.getCompletedTaskCount(), executor.getTaskCount());
        System.out.printf("│ 是否关闭: %-10s 是否终止: %-10s │%n", 
            executor.isShutdown(), executor.isTerminated());
        System.out.println("└─────────────────────────────────────────┘");
    }
    
    /**
     * 示例任务类
     */
    static class SampleTask implements Runnable {
        private final String taskName;
        private final long executionTime;
        
        public SampleTask(String taskName, long executionTime) {
            this.taskName = taskName;
            this.executionTime = executionTime;
        }
        
        @Override
        public void run() {
            String threadName = Thread.currentThread().getName();
            System.out.println("[" + threadName + "] 开始执行任务: " + taskName);
            
            try {
                Thread.sleep(executionTime);
                System.out.println("[" + threadName + "] 完成任务: " + taskName);
            } catch (InterruptedException e) {
                System.out.println("[" + threadName + "] 任务被中断: " + taskName);
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * 自定义线程工厂
     */
    static class CustomThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;
        
        public CustomThreadFactory(String prefix) {
            this.namePrefix = prefix + "-Thread-";
        }
        
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, namePrefix + threadNumber.getAndIncrement());
            thread.setDaemon(false);
            thread.setPriority(Thread.NORM_PRIORITY);
            return thread;
        }
    }
    
    /**
     * 线程池监控器
     */
    static class ThreadPoolMonitor implements Runnable {
        private final ThreadPoolExecutor executor;
        private volatile boolean monitoring = true;
        
        public ThreadPoolMonitor(ThreadPoolExecutor executor) {
            this.executor = executor;
        }
        
        @Override
        public void run() {
            while (monitoring && !executor.isTerminated()) {
                try {
                    System.out.printf("[监控] 核心线程: %d, 当前线程: %d, 活跃线程: %d, 队列任务: %d%n",
                        executor.getCorePoolSize(),
                        executor.getPoolSize(),
                        executor.getActiveCount(),
                        executor.getQueue().size()
                    );
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    monitoring = false;
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
    
    /**
     * 自定义拒绝策略
     */
    static class CustomRejectionHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            System.out.println("❌ 自定义拒绝策略: 任务被拒绝 - " + r.toString());
            System.out.println("   当前线程池状态: 活跃线程=" + executor.getActiveCount() + 
                             ", 队列大小=" + executor.getQueue().size());
            
            // 可以在这里实现自定义的处理逻辑，比如：
            // 1. 记录日志
            // 2. 存储到数据库
            // 3. 发送到消息队列
            // 4. 抛出自定义异常
            
            throw new RejectedExecutionException("线程池已饱和，无法处理新任务");
        }
    }
}

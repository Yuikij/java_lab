package org.kubo.concurrent;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.ArrayList;

/**
 * Java多线程创建方式完整演示
 * 
 * 本demo演示了Java中创建多线程的7种主要方式：
 * 1. 继承Thread类
 * 2. 实现Runnable接口
 * 3. 实现Callable接口 + FutureTask
 * 4. 使用线程池ExecutorService
 * 5. 使用Lambda表达式
 * 6. 使用匿名内部类
 * 7. 使用CompletableFuture
 */
public class JavaMultiThreadCreationDemo {
    
    private static final AtomicInteger demoCounter = new AtomicInteger(1);
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== Java多线程创建方式完整演示 ===\n");
        
        // 方式1: 继承Thread类
        demonstrateExtendsThread();
        
        // 方式2: 实现Runnable接口
        demonstrateRunnableInterface();
        
        // 方式3: 实现Callable接口 + FutureTask
        demonstrateCallableInterface();
        
        // 方式4: 使用线程池ExecutorService
        demonstrateExecutorService();
        
        // 方式5: 使用Lambda表达式
        demonstrateLambdaExpression();
        
        // 方式6: 使用匿名内部类
        demonstrateAnonymousClass();
        
        // 方式7: 使用CompletableFuture
        demonstrateCompletableFuture();
        
        // 总结和比较
        printSummaryAndComparison();
    }
    
    /**
     * 方式1: 继承Thread类
     * 优点: 简单直接，可以直接调用Thread的方法
     * 缺点: Java单继承限制，不够灵活
     */
    private static void demonstrateExtendsThread() throws InterruptedException {
        System.out.println("=== 方式1: 继承Thread类 ===");
        
        // 创建并启动线程
        MyThread thread1 = new MyThread("Worker-1");
        MyThread thread2 = new MyThread("Worker-2");
        
        thread1.start();
        thread2.start();
        
        // 等待线程完成
        thread1.join();
        thread2.join();
        
        System.out.println("方式1演示完成\n");
    }
    
    /**
     * 方式2: 实现Runnable接口
     * 优点: 避免单继承限制，更好的面向对象设计
     * 缺点: 无法直接返回结果
     */
    private static void demonstrateRunnableInterface() throws InterruptedException {
        System.out.println("=== 方式2: 实现Runnable接口 ===");
        
        // 创建Runnable任务
        MyRunnable task1 = new MyRunnable("Task-1");
        MyRunnable task2 = new MyRunnable("Task-2");
        
        // 创建线程并启动
        Thread thread1 = new Thread(task1);
        Thread thread2 = new Thread(task2);
        
        thread1.start();
        thread2.start();
        
        thread1.join();
        thread2.join();
        
        System.out.println("方式2演示完成\n");
    }
    
    /**
     * 方式3: 实现Callable接口 + FutureTask
     * 优点: 可以返回结果，可以抛出异常
     * 缺点: 相对复杂一些
     */
    private static void demonstrateCallableInterface() throws Exception {
        System.out.println("=== 方式3: 实现Callable接口 + FutureTask ===");
        
        // 创建Callable任务
        MyCallable callable1 = new MyCallable("Callable-1", 1000);
        MyCallable callable2 = new MyCallable("Callable-2", 1500);
        
        // 包装成FutureTask
        FutureTask<String> futureTask1 = new FutureTask<>(callable1);
        FutureTask<String> futureTask2 = new FutureTask<>(callable2);
        
        // 创建线程并启动
        Thread thread1 = new Thread(futureTask1);
        Thread thread2 = new Thread(futureTask2);
        
        thread1.start();
        thread2.start();
        
        // 获取执行结果
        String result1 = futureTask1.get(); // 阻塞等待结果
        String result2 = futureTask2.get();
        
        System.out.println("获取到结果: " + result1);
        System.out.println("获取到结果: " + result2);
        System.out.println("方式3演示完成\n");
    }
    
    /**
     * 方式4: 使用线程池ExecutorService
     * 优点: 线程复用，资源控制，功能强大
     * 缺点: 需要管理线程池生命周期
     */
    private static void demonstrateExecutorService() throws Exception {
        System.out.println("=== 方式4: 使用线程池ExecutorService ===");
        
        // 创建线程池
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        // 提交Runnable任务
        executor.submit(() -> performTask("Pool-Runnable-1", 1000));
        executor.submit(() -> performTask("Pool-Runnable-2", 800));
        
        // 提交Callable任务
        Future<String> future1 = executor.submit(new MyCallable("Pool-Callable-1", 1200));
        Future<String> future2 = executor.submit(new MyCallable("Pool-Callable-2", 900));
        
        // 获取结果
        System.out.println("线程池执行结果: " + future1.get());
        System.out.println("线程池执行结果: " + future2.get());
        
        // 关闭线程池
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        System.out.println("方式4演示完成\n");
    }
    
    /**
     * 方式5: 使用Lambda表达式
     * 优点: 代码简洁，现代化写法
     * 缺点: 适用于简单任务
     */
    private static void demonstrateLambdaExpression() throws InterruptedException {
        System.out.println("=== 方式5: 使用Lambda表达式 ===");
        
        // 直接使用Lambda创建线程
        Thread thread1 = new Thread(() -> {
            performTask("Lambda-1", 800);
        });
        
        Thread thread2 = new Thread(() -> {
            performTask("Lambda-2", 600);
        });
        
        thread1.start();
        thread2.start();
        
        thread1.join();
        thread2.join();
        
        // 结合线程池使用Lambda
        ExecutorService executor = Executors.newCachedThreadPool();
        
        executor.submit(() -> performTask("Lambda-Pool-1", 500));
        executor.submit(() -> performTask("Lambda-Pool-2", 700));
        
        executor.shutdown();
        executor.awaitTermination(3, TimeUnit.SECONDS);
        
        System.out.println("方式5演示完成\n");
    }
    
    /**
     * 方式6: 使用匿名内部类
     * 优点: 灵活，可以访问外部变量
     * 缺点: 代码相对冗长
     */
    private static void demonstrateAnonymousClass() throws InterruptedException {
        System.out.println("=== 方式6: 使用匿名内部类 ===");
        
        final String prefix = "Anonymous";
        
        // 匿名Runnable
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                performTask(prefix + "-1", 600);
            }
        });
        
        // 匿名Thread子类
        Thread thread2 = new Thread() {
            @Override
            public void run() {
                performTask(prefix + "-2", 800);
            }
        };
        
        thread1.start();
        thread2.start();
        
        thread1.join();
        thread2.join();
        
        System.out.println("方式6演示完成\n");
    }
    
    /**
     * 方式7: 使用CompletableFuture
     * 优点: 异步编程，支持链式调用，功能强大
     * 缺点: 相对复杂，需要理解异步编程概念
     */
    private static void demonstrateCompletableFuture() throws Exception {
        System.out.println("=== 方式7: 使用CompletableFuture ===");
        
        // 创建异步任务
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            performTask("CompletableFuture-1", 1000);
            return "CompletableFuture-1 完成";
        });
        
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            performTask("CompletableFuture-2", 800);
            return "CompletableFuture-2 完成";
        });
        
        // 链式操作
        CompletableFuture<String> combinedFuture = future1.thenCombine(future2, 
            (result1, result2) -> result1 + " & " + result2);
        
        // 获取最终结果
        String finalResult = combinedFuture.get();
        System.out.println("组合结果: " + finalResult);
        
        // 使用异步回调
        CompletableFuture.runAsync(() -> performTask("Async-Callback", 500))
                        .thenRun(() -> System.out.println("异步回调完成"));
        
        Thread.sleep(1000); // 等待异步任务完成
        System.out.println("方式7演示完成\n");
    }
    
    /**
     * 工具方法：执行任务
     */
    private static void performTask(String taskName, long sleepTime) {
        String threadName = Thread.currentThread().getName();
        System.out.println("🔄 [" + threadName + "] 开始执行: " + taskName);
        
        try {
            Thread.sleep(sleepTime);
            System.out.println("✅ [" + threadName + "] 完成任务: " + taskName);
        } catch (InterruptedException e) {
            System.out.println("❌ [" + threadName + "] 任务被中断: " + taskName);
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 总结和比较各种方式
     */
    private static void printSummaryAndComparison() {
        System.out.println("=== 📊 各种方式对比总结 ===");
        System.out.println();
        
        System.out.println("┌────────────────────────────────────────────────────────────────────┐");
        System.out.println("│                        Java多线程创建方式对比                          │");
        System.out.println("├────────────────┬─────────────────┬─────────────────┬─────────────────┤");
        System.out.println("│ 创建方式       │ 优点            │ 缺点            │ 推荐使用场景     │");
        System.out.println("├────────────────┼─────────────────┼─────────────────┼─────────────────┤");
        System.out.println("│ 1.继承Thread   │ 简单直接        │ 单继承限制      │ 学习、简单场景   │");
        System.out.println("│ 2.实现Runnable │ 避免继承限制    │ 无返回值        │ 一般多线程任务   │");
        System.out.println("│ 3.实现Callable │ 有返回值        │ 相对复杂        │ 需要返回结果     │");
        System.out.println("│ 4.线程池       │ 资源控制强      │ 管理复杂        │ 生产环境首选     │");
        System.out.println("│ 5.Lambda表达式 │ 代码简洁        │ 仅适合简单任务  │ 现代Java开发     │");
        System.out.println("│ 6.匿名内部类   │ 灵活性好        │ 代码冗长        │ 特殊需求场景     │");
        System.out.println("│ 7.CompletableFuture│ 异步编程强大 │ 学习成本高      │ 复杂异步场景     │");
        System.out.println("└────────────────┴─────────────────┴─────────────────┴─────────────────┘");
        
        System.out.println("\n💡 最佳实践建议:");
        System.out.println("• 📈 生产环境: 优先使用线程池 (ExecutorService)");
        System.out.println("• 🔄 异步编程: 使用 CompletableFuture");
        System.out.println("• ⚡ 简单任务: 使用 Lambda 表达式");
        System.out.println("• 📚 学习阶段: 从 Thread 和 Runnable 开始");
        System.out.println("• 🎯 需要返回值: 使用 Callable + Future 或 CompletableFuture");
        
        System.out.println("\n🔒 线程安全注意事项:");
        System.out.println("• 多线程访问共享资源时要考虑同步");
        System.out.println("• 合理使用 synchronized、Lock、原子类等同步机制");
        System.out.println("• 避免死锁、活锁等并发问题");
        System.out.println("• 优先使用线程安全的集合类");
    }
    
    // ====================== 内部类定义 ======================
    
    /**
     * 方式1: 继承Thread类
     */
    static class MyThread extends Thread {
        private final String taskName;
        
        public MyThread(String taskName) {
            this.taskName = taskName;
            setName("MyThread-" + taskName);
        }
        
        @Override
        public void run() {
            performTask(taskName, 1000);
        }
    }
    
    /**
     * 方式2: 实现Runnable接口
     */
    static class MyRunnable implements Runnable {
        private final String taskName;
        
        public MyRunnable(String taskName) {
            this.taskName = taskName;
        }
        
        @Override
        public void run() {
            performTask(taskName, 800);
        }
    }
    
    /**
     * 方式3: 实现Callable接口
     */
    static class MyCallable implements Callable<String> {
        private final String taskName;
        private final long sleepTime;
        
        public MyCallable(String taskName, long sleepTime) {
            this.taskName = taskName;
            this.sleepTime = sleepTime;
        }
        
        @Override
        public String call() throws Exception {
            String threadName = Thread.currentThread().getName();
            System.out.println("🔄 [" + threadName + "] Callable开始执行: " + taskName);
            
            Thread.sleep(sleepTime);
            
            String result = taskName + " 执行完成，耗时: " + sleepTime + "ms";
            System.out.println("✅ [" + threadName + "] Callable完成: " + result);
            
            return result;
        }
    }
}

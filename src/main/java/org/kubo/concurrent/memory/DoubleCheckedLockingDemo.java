package org.kubo.concurrent.memory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 双重检查锁定（Double-Checked Locking）模式演示
 * 
 * 双重检查锁定是一种用于延迟初始化的并发设计模式，旨在减少同步开销。
 * 但是，由于指令重排序的存在，这个模式在没有正确处理的情况下会出现问题。
 * 
 * 主要问题：
 * 1. 指令重排序可能导致返回未完全初始化的对象
 * 2. 在多处理器系统中存在可见性问题
 * 
 * 解决方案：
 * 1. 使用volatile关键字防止重排序
 * 2. 使用静态内部类实现延迟初始化
 * 3. 使用枚举实现单例
 * 
 * @author kubo
 */
public class DoubleCheckedLockingDemo {
    
    public static void main(String[] args) {
        System.out.println("===============================================");
        System.out.println("         双重检查锁定（DCL）模式演示");
        System.out.println("===============================================\n");
        
        // 演示错误的双重检查锁定实现
        demonstrateBrokenDoubleCheckedLocking();
        sleep(1000);
        
        // 演示正确的双重检查锁定实现
        demonstrateCorrectDoubleCheckedLocking();
        sleep(1000);
        
        // 演示静态内部类解决方案
        demonstrateStaticInnerClassSolution();
        sleep(1000);
        
        // 演示枚举单例解决方案
        demonstrateEnumSingletonSolution();
        sleep(1000);
        
        // 性能对比测试
        performanceComparison();
    }
    
    /**
     * 演示错误的双重检查锁定实现
     */
    private static void demonstrateBrokenDoubleCheckedLocking() {
        System.out.println("❌ 错误的双重检查锁定实现");
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("可能存在指令重排序问题，导致返回未完全初始化的对象");
        System.out.println();
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        int threadCount = 10;
        int testsPerThread = 1000;
        
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i + 1;
            Thread thread = new Thread(() -> {
                try {
                    startLatch.await(); // 等待同时开始
                    
                    for (int j = 0; j < testsPerThread; j++) {
                        // 重置单例状态
                        BrokenSingleton.reset();
                        
                        BrokenSingleton instance = BrokenSingleton.getInstance();
                        if (instance.isProperlyInitialized()) {
                            successCount.incrementAndGet();
                        } else {
                            failureCount.incrementAndGet();
                            if (failureCount.get() <= 5) {
                                System.out.println("线程" + threadId + ": 检测到未完全初始化的对象！");
                            }
                        }
                    }
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            }, "BrokenTest-" + threadId);
            thread.start();
        }
        
        startLatch.countDown(); // 开始测试
        
        try {
            endLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        int totalTests = threadCount * testsPerThread;
        System.out.println("错误实现测试结果:");
        System.out.println("  总测试次数: " + totalTests);
        System.out.println("  成功次数: " + successCount.get());
        System.out.println("  失败次数: " + failureCount.get());
        System.out.println("  成功率: " + (successCount.get() * 100.0 / totalTests) + "%");
        System.out.println();
    }
    
    /**
     * 演示正确的双重检查锁定实现
     */
    private static void demonstrateCorrectDoubleCheckedLocking() {
        System.out.println("✅ 正确的双重检查锁定实现");
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("使用volatile关键字防止指令重排序");
        System.out.println();
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        int threadCount = 10;
        int testsPerThread = 1000;
        
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i + 1;
            Thread thread = new Thread(() -> {
                try {
                    startLatch.await();
                    
                    for (int j = 0; j < testsPerThread; j++) {
                        CorrectSingleton.reset();
                        
                        CorrectSingleton instance = CorrectSingleton.getInstance();
                        if (instance.isProperlyInitialized()) {
                            successCount.incrementAndGet();
                        } else {
                            failureCount.incrementAndGet();
                            System.out.println("线程" + threadId + ": 意外的初始化失败！");
                        }
                    }
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            }, "CorrectTest-" + threadId);
            thread.start();
        }
        
        startLatch.countDown();
        
        try {
            endLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        int totalTests = threadCount * testsPerThread;
        System.out.println("正确实现测试结果:");
        System.out.println("  总测试次数: " + totalTests);
        System.out.println("  成功次数: " + successCount.get());
        System.out.println("  失败次数: " + failureCount.get());
        System.out.println("  成功率: " + (successCount.get() * 100.0 / totalTests) + "%");
        System.out.println();
    }
    
    /**
     * 演示静态内部类解决方案
     */
    private static void demonstrateStaticInnerClassSolution() {
        System.out.println("🏗️ 静态内部类解决方案");
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("利用类加载机制保证线程安全和延迟初始化");
        System.out.println();
        
        long startTime = System.nanoTime();
        
        // 测试多线程获取单例
        int threadCount = 5;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicReference<StaticInnerClassSingleton> firstInstance = new AtomicReference<>();
        AtomicInteger sameInstanceCount = new AtomicInteger(0);
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i + 1;
            Thread thread = new Thread(() -> {
                StaticInnerClassSingleton instance = StaticInnerClassSingleton.getInstance();
                
                if (firstInstance.compareAndSet(null, instance)) {
                    System.out.println("线程" + threadId + ": 获取到第一个实例");
                } else if (firstInstance.get() == instance) {
                    sameInstanceCount.incrementAndGet();
                    System.out.println("线程" + threadId + ": 获取到相同实例");
                } else {
                    System.out.println("线程" + threadId + ": 获取到不同实例！");
                }
                
                latch.countDown();
            }, "StaticTest-" + threadId);
            thread.start();
        }
        
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long endTime = System.nanoTime();
        
        System.out.println("静态内部类测试结果:");
        System.out.println("  获取相同实例的线程数: " + (sameInstanceCount.get() + 1));
        System.out.println("  执行时间: " + (endTime - startTime) / 1_000_000.0 + "ms");
        System.out.println();
    }
    
    /**
     * 演示枚举单例解决方案
     */
    private static void demonstrateEnumSingletonSolution() {
        System.out.println("🔢 枚举单例解决方案");
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("最安全的单例实现方式，天然防止序列化和反射攻击");
        System.out.println();
        
        long startTime = System.nanoTime();
        
        int threadCount = 5;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicReference<EnumSingleton> firstInstance = new AtomicReference<>();
        AtomicInteger sameInstanceCount = new AtomicInteger(0);
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i + 1;
            Thread thread = new Thread(() -> {
                EnumSingleton instance = EnumSingleton.INSTANCE;
                
                if (firstInstance.compareAndSet(null, instance)) {
                    System.out.println("线程" + threadId + ": 获取到第一个枚举实例");
                } else if (firstInstance.get() == instance) {
                    sameInstanceCount.incrementAndGet();
                    System.out.println("线程" + threadId + ": 获取到相同枚举实例");
                } else {
                    System.out.println("线程" + threadId + ": 获取到不同枚举实例！");
                }
                
                // 调用业务方法
                String result = instance.doSomething("Task-" + threadId);
                System.out.println("线程" + threadId + ": " + result);
                
                latch.countDown();
            }, "EnumTest-" + threadId);
            thread.start();
        }
        
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long endTime = System.nanoTime();
        
        System.out.println("枚举单例测试结果:");
        System.out.println("  获取相同实例的线程数: " + (sameInstanceCount.get() + 1));
        System.out.println("  执行时间: " + (endTime - startTime) / 1_000_000.0 + "ms");
        System.out.println();
    }
    
    /**
     * 性能对比测试
     */
    private static void performanceComparison() {
        System.out.println("📊 性能对比测试");
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("对比不同单例实现的性能");
        System.out.println();
        
        int iterations = 1_000_000;
        
        // 测试正确的双重检查锁定
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            CorrectSingleton.getInstance();
        }
        long dclTime = System.nanoTime() - startTime;
        
        // 测试静态内部类
        startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            StaticInnerClassSingleton.getInstance();
        }
        long staticTime = System.nanoTime() - startTime;
        
        // 测试枚举单例
        startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            EnumSingleton instance = EnumSingleton.INSTANCE;
        }
        long enumTime = System.nanoTime() - startTime;
        
        // 测试同步方法单例
        startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            SynchronizedSingleton.getInstance();
        }
        long syncTime = System.nanoTime() - startTime;
        
        System.out.println("性能测试结果 (" + iterations + " 次调用):");
        System.out.printf("  双重检查锁定: %.2f ms%n", dclTime / 1_000_000.0);
        System.out.printf("  静态内部类:   %.2f ms%n", staticTime / 1_000_000.0);
        System.out.printf("  枚举单例:     %.2f ms%n", enumTime / 1_000_000.0);
        System.out.printf("  同步方法:     %.2f ms%n", syncTime / 1_000_000.0);
        System.out.println();
        
        System.out.println("推荐使用顺序:");
        System.out.println("1. 枚举单例 - 最安全，代码最简洁");
        System.out.println("2. 静态内部类 - 性能好，延迟初始化");
        System.out.println("3. 双重检查锁定 - 复杂但灵活");
        System.out.println("4. 同步方法 - 简单但性能差");
    }
    
    private static void sleep(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

/**
 * 错误的双重检查锁定单例实现
 * 问题：没有volatile，可能存在指令重排序
 */
class BrokenSingleton {
    private static BrokenSingleton instance; // 没有volatile！
    private boolean initialized = false;
    private String data;
    
    private BrokenSingleton() {
        // 模拟复杂的初始化过程
        try {
            Thread.sleep(1); // 模拟初始化延迟
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        this.data = "Initialized-" + System.nanoTime();
        this.initialized = true;
    }
    
    public static BrokenSingleton getInstance() {
        if (instance == null) {                    // 第一次检查
            synchronized (BrokenSingleton.class) {
                if (instance == null) {            // 第二次检查
                    instance = new BrokenSingleton(); // 可能发生重排序！
                }
            }
        }
        return instance;
    }
    
    public boolean isProperlyInitialized() {
        return initialized && data != null;
    }
    
    public static void reset() {
        instance = null;
    }
}

/**
 * 正确的双重检查锁定单例实现
 * 使用volatile防止指令重排序
 */
class CorrectSingleton {
    private static volatile CorrectSingleton instance; // 使用volatile！
    private boolean initialized = false;
    private String data;
    
    private CorrectSingleton() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        this.data = "Initialized-" + System.nanoTime();
        this.initialized = true;
    }
    
    public static CorrectSingleton getInstance() {
        if (instance == null) {                    // 第一次检查
            synchronized (CorrectSingleton.class) {
                if (instance == null) {            // 第二次检查
                    instance = new CorrectSingleton(); // volatile防止重排序
                }
            }
        }
        return instance;
    }
    
    public boolean isProperlyInitialized() {
        return initialized && data != null;
    }
    
    public static void reset() {
        instance = null;
    }
}

/**
 * 静态内部类单例实现
 * 利用类加载机制保证线程安全
 */
class StaticInnerClassSingleton {
    private boolean initialized = false;
    private String data;
    
    private StaticInnerClassSingleton() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        this.data = "StaticInitialized-" + System.nanoTime();
        this.initialized = true;
        System.out.println("StaticInnerClassSingleton 实例被创建");
    }
    
    // 静态内部类，只有在被引用时才会加载
    private static class SingletonHolder {
        private static final StaticInnerClassSingleton INSTANCE = new StaticInnerClassSingleton();
    }
    
    public static StaticInnerClassSingleton getInstance() {
        return SingletonHolder.INSTANCE; // 类加载机制保证线程安全
    }
    
    public boolean isProperlyInitialized() {
        return initialized && data != null;
    }
    
    public String getData() {
        return data;
    }
}

/**
 * 枚举单例实现
 * 最安全的单例实现方式
 */
enum EnumSingleton {
    INSTANCE;
    
    private boolean initialized = false;
    private String data;
    
    // 枚举的构造方法
    EnumSingleton() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        this.data = "EnumInitialized-" + System.nanoTime();
        this.initialized = true;
        System.out.println("EnumSingleton 实例被创建");
    }
    
    public String doSomething(String input) {
        return "EnumSingleton处理: " + input + " -> " + data;
    }
    
    public boolean isProperlyInitialized() {
        return initialized && data != null;
    }
}

/**
 * 同步方法单例实现（用于性能对比）
 */
class SynchronizedSingleton {
    private static SynchronizedSingleton instance;
    private boolean initialized = false;
    private String data;
    
    private SynchronizedSingleton() {
        this.data = "SyncInitialized-" + System.nanoTime();
        this.initialized = true;
    }
    
    public static synchronized SynchronizedSingleton getInstance() {
        if (instance == null) {
            instance = new SynchronizedSingleton();
        }
        return instance;
    }
    
    public boolean isProperlyInitialized() {
        return initialized && data != null;
    }
}

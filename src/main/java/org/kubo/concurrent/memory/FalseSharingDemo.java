package org.kubo.concurrent.memory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


/**
 * 伪共享（False Sharing）问题演示
 * 
 * 伪共享是多核处理器中一个重要的性能问题。当多个线程访问同一缓存行中的不同变量时，
 * 会导致缓存行在CPU核心之间频繁传输，严重影响性能。
 * 
 * 问题原理：
 * 1. CPU缓存以缓存行（cache line）为单位，通常是64字节
 * 2. 当一个CPU修改缓存行中的数据时，其他CPU的相同缓存行会失效
 * 3. 即使不同线程访问的是不同变量，只要它们在同一缓存行，就会互相影响
 * 
 * 解决方案：
 * 1. 缓存行填充（Padding）
 * 2. @Contended注解（Java 8+）
 * 3. 合理的数据结构设计
 * 
 * @author kubo
 */
public class FalseSharingDemo {
    
    // 缓存行大小（通常是64字节）
    private static final int CACHE_LINE_SIZE = 64;
    private static final int ITERATIONS = 10_000_000;
    
    public static void main(String[] args) {
        System.out.println("===============================================");
        System.out.println("           伪共享（False Sharing）演示");
        System.out.println("===============================================\n");
        
        // 演示伪共享问题
        demonstrateFalseSharing();
        sleep(1000);
        
        // 演示缓存行填充解决方案
        demonstrateCacheLinePadding();
        sleep(1000);
        
        // 演示@Contended注解解决方案
        demonstrateContendedAnnotation();
        sleep(1000);
        
        // 演示数组中的伪共享
        demonstrateArrayFalseSharing();
        sleep(1000);
        
        // 性能对比测试
        performanceComparison();
    }
    
    /**
     * 演示伪共享问题
     */
    private static void demonstrateFalseSharing() {
        System.out.println("❌ 伪共享问题演示");
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("多个变量位于同一缓存行，导致性能下降");
        System.out.println();
        
        FalseSharingExample example = new FalseSharingExample();
        
        long startTime = System.nanoTime();
        
        // 启动多个线程，每个线程访问不同的变量
        Thread[] threads = new Thread[4];
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threads.length);
        
        for (int i = 0; i < threads.length; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                try {
                    startLatch.await(); // 等待同时开始
                    
                    for (int j = 0; j < ITERATIONS; j++) {
                        example.increment(threadId);
                    }
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            }, "FalseSharing-" + threadId);
        }
        
        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }
        
        startLatch.countDown(); // 开始执行
        
        try {
            endLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long endTime = System.nanoTime();
        double duration = (endTime - startTime) / 1_000_000.0;
        
        System.out.println("伪共享测试结果:");
        System.out.println("  执行时间: " + String.format("%.2f", duration) + " ms");
        System.out.println("  每秒操作数: " + String.format("%.0f", (ITERATIONS * threads.length) / (duration / 1000)));
        example.printResults();
        System.out.println();
    }
    
    /**
     * 演示缓存行填充解决方案
     */
    private static void demonstrateCacheLinePadding() {
        System.out.println("✅ 缓存行填充解决方案");
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("通过填充使每个变量独占一个缓存行");
        System.out.println();
        
        PaddedExample example = new PaddedExample();
        
        long startTime = System.nanoTime();
        
        Thread[] threads = new Thread[4];
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threads.length);
        
        for (int i = 0; i < threads.length; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                try {
                    startLatch.await();
                    
                    for (int j = 0; j < ITERATIONS; j++) {
                        example.increment(threadId);
                    }
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            }, "Padded-" + threadId);
        }
        
        for (Thread thread : threads) {
            thread.start();
        }
        
        startLatch.countDown();
        
        try {
            endLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long endTime = System.nanoTime();
        double duration = (endTime - startTime) / 1_000_000.0;
        
        System.out.println("缓存行填充测试结果:");
        System.out.println("  执行时间: " + String.format("%.2f", duration) + " ms");
        System.out.println("  每秒操作数: " + String.format("%.0f", (ITERATIONS * threads.length) / (duration / 1000)));
        example.printResults();
        System.out.println();
    }
    
    /**
     * 演示@Contended注解解决方案
     */
    private static void demonstrateContendedAnnotation() {
        System.out.println("🏷️ @Contended注解解决方案");
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("使用@Contended注解自动添加填充");
        System.out.println("注意: 需要JVM参数 -XX:-RestrictContended");
        System.out.println();
        
        ContendedExample example = new ContendedExample();
        
        long startTime = System.nanoTime();
        
        Thread[] threads = new Thread[4];
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threads.length);
        
        for (int i = 0; i < threads.length; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                try {
                    startLatch.await();
                    
                    for (int j = 0; j < ITERATIONS; j++) {
                        example.increment(threadId);
                    }
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            }, "Contended-" + threadId);
        }
        
        for (Thread thread : threads) {
            thread.start();
        }
        
        startLatch.countDown();
        
        try {
            endLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long endTime = System.nanoTime();
        double duration = (endTime - startTime) / 1_000_000.0;
        
        System.out.println("@Contended注解测试结果:");
        System.out.println("  执行时间: " + String.format("%.2f", duration) + " ms");
        System.out.println("  每秒操作数: " + String.format("%.0f", (ITERATIONS * threads.length) / (duration / 1000)));
        example.printResults();
        System.out.println();
    }
    
    /**
     * 演示数组中的伪共享
     */
    private static void demonstrateArrayFalseSharing() {
        System.out.println("📊 数组中的伪共享演示");
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("数组元素可能位于同一缓存行，造成伪共享");
        System.out.println();
        
        // 测试相邻数组元素（伪共享）
        long startTime = System.nanoTime();
        testArrayAccess(true);
        long adjacentTime = System.nanoTime() - startTime;
        
        // 测试分散数组元素（避免伪共享）
        startTime = System.nanoTime();
        testArrayAccess(false);
        long dispersedTime = System.nanoTime() - startTime;
        
        System.out.println("数组伪共享测试结果:");
        System.out.println("  相邻元素访问时间: " + String.format("%.2f", adjacentTime / 1_000_000.0) + " ms");
        System.out.println("  分散元素访问时间: " + String.format("%.2f", dispersedTime / 1_000_000.0) + " ms");
        System.out.println("  性能提升: " + String.format("%.1f", (double) adjacentTime / dispersedTime) + "x");
        System.out.println();
    }
    
    /**
     * 测试数组访问模式
     */
    private static void testArrayAccess(boolean adjacent) {
        final int arraySize = 1024;
        final int threadCount = 4;
        final long[] array = new long[arraySize];
        
        Thread[] threads = new Thread[threadCount];
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                try {
                    startLatch.await();
                    
                    // 计算访问的数组索引
                    int index = adjacent ? threadId : threadId * (arraySize / threadCount);
                    
                    for (int j = 0; j < ITERATIONS / 10; j++) {
                        array[index]++;
                    }
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            }, "ArrayTest-" + threadId);
        }
        
        for (Thread thread : threads) {
            thread.start();
        }
        
        startLatch.countDown();
        
        try {
            endLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 性能对比测试
     */
    private static void performanceComparison() {
        System.out.println("📈 性能对比测试");
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("对比不同实现方式的性能差异");
        System.out.println();
        
        int rounds = 5;
        long[] falseSharingTimes = new long[rounds];
        long[] paddedTimes = new long[rounds];
        long[] contendedTimes = new long[rounds];
        
        // 预热JVM
        warmup();
        
        for (int round = 0; round < rounds; round++) {
            System.out.println("第 " + (round + 1) + " 轮测试:");
            
            // 测试伪共享
            falseSharingTimes[round] = measurePerformance(() -> {
                FalseSharingExample example = new FalseSharingExample();
                runConcurrentTest(example::increment);
            });
            
            // 测试填充
            paddedTimes[round] = measurePerformance(() -> {
                PaddedExample example = new PaddedExample();
                runConcurrentTest(example::increment);
            });
            
            // 测试@Contended
            contendedTimes[round] = measurePerformance(() -> {
                ContendedExample example = new ContendedExample();
                runConcurrentTest(example::increment);
            });
            
            System.out.printf("  伪共享: %.2f ms, 填充: %.2f ms, @Contended: %.2f ms%n",
                            falseSharingTimes[round] / 1_000_000.0,
                            paddedTimes[round] / 1_000_000.0,
                            contendedTimes[round] / 1_000_000.0);
        }
        
        // 计算平均值
        long avgFalseSharing = average(falseSharingTimes);
        long avgPadded = average(paddedTimes);
        long avgContended = average(contendedTimes);
        
        System.out.println("\n平均性能结果:");
        System.out.printf("  伪共享: %.2f ms%n", avgFalseSharing / 1_000_000.0);
        System.out.printf("  缓存行填充: %.2f ms (%.1fx faster)%n", 
                        avgPadded / 1_000_000.0, 
                        (double) avgFalseSharing / avgPadded);
        System.out.printf("  @Contended: %.2f ms (%.1fx faster)%n", 
                        avgContended / 1_000_000.0, 
                        (double) avgFalseSharing / avgContended);
    }
    
    private static void warmup() {
        System.out.println("JVM预热中...");
        for (int i = 0; i < 3; i++) {
            FalseSharingExample example = new FalseSharingExample();
            runConcurrentTest(example::increment);
        }
        System.out.println("预热完成\n");
    }
    
    private static long measurePerformance(Runnable task) {
        long startTime = System.nanoTime();
        task.run();
        return System.nanoTime() - startTime;
    }
    
    private static void runConcurrentTest(TestTask task) {
        final int threadCount = 4;
        final int iterations = ITERATIONS / 10; // 减少迭代次数以加快测试
        
        Thread[] threads = new Thread[threadCount];
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < iterations; j++) {
                        task.execute(threadId);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }
        
        for (Thread thread : threads) {
            thread.start();
        }
        
        startLatch.countDown();
        
        try {
            endLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private static long average(long[] values) {
        long sum = 0;
        for (long value : values) {
            sum += value;
        }
        return sum / values.length;
    }
    
    private static void sleep(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @FunctionalInterface
    interface TestTask {
        void execute(int threadId);
    }
}

/**
 * 伪共享示例 - 多个变量位于同一缓存行
 */
class FalseSharingExample {
    // 这些long变量很可能位于同一个64字节的缓存行中
    public volatile long counter0 = 0;
    public volatile long counter1 = 0;
    public volatile long counter2 = 0;
    public volatile long counter3 = 0;
    
    public void increment(int threadId) {
        switch (threadId) {
            case 0: counter0++; break;
            case 1: counter1++; break;
            case 2: counter2++; break;
            case 3: counter3++; break;
        }
    }
    
    public void printResults() {
        System.out.printf("  计数器值: %d, %d, %d, %d%n", counter0, counter1, counter2, counter3);
    }
}

/**
 * 缓存行填充示例 - 使用填充避免伪共享
 */
class PaddedExample {
    // 每个计数器都用填充字节分隔，确保独占缓存行
    public volatile long counter0 = 0;
    private long p01, p02, p03, p04, p05, p06, p07; // 填充
    
    public volatile long counter1 = 0;
    private long p11, p12, p13, p14, p15, p16, p17; // 填充
    
    public volatile long counter2 = 0;
    private long p21, p22, p23, p24, p25, p26, p27; // 填充
    
    public volatile long counter3 = 0;
    private long p31, p32, p33, p34, p35, p36, p37; // 填充
    
    public void increment(int threadId) {
        switch (threadId) {
            case 0: counter0++; break;
            case 1: counter1++; break;
            case 2: counter2++; break;
            case 3: counter3++; break;
        }
    }
    
    public void printResults() {
        System.out.printf("  计数器值: %d, %d, %d, %d%n", counter0, counter1, counter2, counter3);
    }
}

/**
 * @Contended注解示例 - 自动添加填充
 * 注意：需要JVM参数 -XX:-RestrictContended 才能生效
 * 由于@Contended在模块化后访问受限，这里用注释形式展示
 */
class ContendedExample {
    // @Contended注解会自动添加填充，避免伪共享
    // @Contended  // 实际使用时取消注释
    public volatile long counter0 = 0;
    private long pad0_1, pad0_2, pad0_3, pad0_4, pad0_5, pad0_6, pad0_7; // 手动填充
    
    // @Contended  // 实际使用时取消注释
    public volatile long counter1 = 0;
    private long pad1_1, pad1_2, pad1_3, pad1_4, pad1_5, pad1_6, pad1_7; // 手动填充
    
    // @Contended  // 实际使用时取消注释
    public volatile long counter2 = 0;
    private long pad2_1, pad2_2, pad2_3, pad2_4, pad2_5, pad2_6, pad2_7; // 手动填充
    
    // @Contended  // 实际使用时取消注释
    public volatile long counter3 = 0;
    private long pad3_1, pad3_2, pad3_3, pad3_4, pad3_5, pad3_6, pad3_7; // 手动填充
    
    public void increment(int threadId) {
        switch (threadId) {
            case 0: counter0++; break;
            case 1: counter1++; break;
            case 2: counter2++; break;
            case 3: counter3++; break;
        }
    }
    
    public void printResults() {
        System.out.printf("  计数器值: %d, %d, %d, %d%n", counter0, counter1, counter2, counter3);
    }
}

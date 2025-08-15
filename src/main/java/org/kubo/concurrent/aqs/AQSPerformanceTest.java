package org.kubo.concurrent.aqs;

import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AQS性能测试和原理分析
 * 
 * 测试内容：
 * 1. synchronized vs ReentrantLock 性能对比
 * 2. 公平锁 vs 非公平锁 性能对比
 * 3. 不同竞争强度下的性能表现
 * 4. AQS队列机制的效果验证
 */
public class AQSPerformanceTest {
    
    private static final int THREAD_COUNT = 10;
    private static final int OPERATIONS_PER_THREAD = 100000;
    
    /**
     * 性能测试结果类
     */
    public static class PerformanceResult {
        private final String testName;
        private final long durationMs;
        private final long totalOperations;
        private final double operationsPerSecond;
        
        public PerformanceResult(String testName, long durationMs, long totalOperations) {
            this.testName = testName;
            this.durationMs = durationMs;
            this.totalOperations = totalOperations;
            this.operationsPerSecond = (totalOperations * 1000.0) / durationMs;
        }
        
        @Override
        public String toString() {
            return String.format("%s: %d ms, %.2f ops/sec", 
                               testName, durationMs, operationsPerSecond);
        }
    }
    
    /**
     * synchronized测试
     */
    public static class SynchronizedTest {
        private final AtomicInteger counter = new AtomicInteger(0);
        private final Object lock = new Object();
        
        public void increment() {
            synchronized (lock) {
                counter.incrementAndGet();
            }
        }
        
        public int getCounter() {
            return counter.get();
        }
    }
    
    /**
     * ReentrantLock测试
     */
    public static class ReentrantLockTest {
        private final AtomicInteger counter = new AtomicInteger(0);
        private final ReentrantLock lock;
        
        public ReentrantLockTest(boolean fair) {
            this.lock = new ReentrantLock(fair);
        }
        
        public void increment() {
            lock.lock();
            try {
                counter.incrementAndGet();
            } finally {
                lock.unlock();
            }
        }
        
        public int getCounter() {
            return counter.get();
        }
        
        public boolean isFair() {
            return lock.isFair();
        }
    }
    
    /**
     * 执行性能测试
     */
    public static PerformanceResult runPerformanceTest(String testName, Runnable testLogic) 
            throws InterruptedException {
        
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(THREAD_COUNT);
        AtomicLong totalOperations = new AtomicLong(0);
        
        // 创建工作线程
        Thread[] threads = new Thread[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i] = new Thread(() -> {
                try {
                    startLatch.await(); // 等待开始信号
                    
                    for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                        testLogic.run();
                        totalOperations.incrementAndGet();
                    }
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finishLatch.countDown();
                }
            });
        }
        
        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }
        
        // 开始计时
        long startTime = System.currentTimeMillis();
        startLatch.countDown(); // 发出开始信号
        
        // 等待所有线程完成
        finishLatch.await();
        long endTime = System.currentTimeMillis();
        
        return new PerformanceResult(testName, endTime - startTime, totalOperations.get());
    }
    
    /**
     * AQS队列机制演示
     */
    public static class AQSQueueDemo {
        private final ReentrantLock lock = new ReentrantLock();
        private final AtomicInteger waitingThreads = new AtomicInteger(0);
        
        public void demonstrateQueue() throws InterruptedException {
            System.out.println("=== AQS队列机制演示 ===");
            
            // 第一个线程获取锁并长时间持有
            Thread holder = new Thread(() -> {
                lock.lock();
                try {
                    System.out.println("持有者线程获取锁，将持有5秒");
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    System.out.println("持有者线程释放锁");
                    lock.unlock();
                }
            }, "Holder");
            
            holder.start();
            Thread.sleep(100); // 确保holder先获取锁
            
            // 创建多个等待线程
            Thread[] waiters = new Thread[5];
            for (int i = 0; i < waiters.length; i++) {
                final int waiterId = i;
                waiters[i] = new Thread(() -> {
                    try {
                        System.out.println("等待线程 " + waiterId + " 尝试获取锁");
                        waitingThreads.incrementAndGet();
                        
                        long startWait = System.currentTimeMillis();
                        lock.lock();
                        long endWait = System.currentTimeMillis();
                        
                        try {
                            System.out.println("等待线程 " + waiterId + " 获取到锁，等待时间: " + 
                                             (endWait - startWait) + "ms，队列长度: " + lock.getQueueLength());
                            Thread.sleep(500); // 模拟工作
                        } finally {
                            waitingThreads.decrementAndGet();
                            lock.unlock();
                            System.out.println("等待线程 " + waiterId + " 释放锁");
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }, "Waiter-" + i);
            }
            
            // 启动等待线程
            for (Thread waiter : waiters) {
                waiter.start();
                Thread.sleep(50); // 间隔启动，观察队列形成
            }
            
            // 监控队列状态
            Thread monitor = new Thread(() -> {
                while (waitingThreads.get() > 0) {
                    try {
                        System.out.println("队列监控 - 队列长度: " + lock.getQueueLength() + 
                                         ", 等待线程数: " + waitingThreads.get() + 
                                         ", 锁被持有: " + lock.isLocked());
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }, "Monitor");
            
            monitor.start();
            
            // 等待所有线程完成
            holder.join();
            for (Thread waiter : waiters) {
                waiter.join();
            }
            monitor.interrupt();
            monitor.join();
            
            System.out.println("AQS队列演示完成\n");
        }
    }
    
    /**
     * 竞争强度测试
     */
    public static void testContentionLevels() throws InterruptedException {
        System.out.println("=== 不同竞争强度测试 ===");
        
        int[] threadCounts = {1, 2, 5, 10, 20};
        
        for (int threadCount : threadCounts) {
            ReentrantLockTest test = new ReentrantLockTest(false);
            
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch finishLatch = new CountDownLatch(threadCount);
            
            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < threadCount; i++) {
                new Thread(() -> {
                    try {
                        startLatch.await();
                        for (int j = 0; j < 10000; j++) {
                            test.increment();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        finishLatch.countDown();
                    }
                }).start();
            }
            
            startLatch.countDown();
            finishLatch.await();
            
            long duration = System.currentTimeMillis() - startTime;
            long totalOps = threadCount * 10000L;
            double opsPerSec = (totalOps * 1000.0) / duration;
            
            System.out.printf("线程数: %2d, 耗时: %4d ms, 吞吐量: %.0f ops/sec%n", 
                            threadCount, duration, opsPerSec);
        }
        System.out.println();
    }
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== AQS性能测试开始 ===\n");
        
        // 1. synchronized vs ReentrantLock
        System.out.println("1. synchronized vs ReentrantLock 性能对比：");
        
        SynchronizedTest syncTest = new SynchronizedTest();
        PerformanceResult syncResult = runPerformanceTest("synchronized", 
                                                          syncTest::increment);
        
        ReentrantLockTest lockTest = new ReentrantLockTest(false);
        PerformanceResult lockResult = runPerformanceTest("ReentrantLock(非公平)", 
                                                          lockTest::increment);
        
        System.out.println(syncResult);
        System.out.println(lockResult);
        System.out.printf("性能比较: ReentrantLock是synchronized的 %.2f 倍%n", 
                         lockResult.operationsPerSecond / syncResult.operationsPerSecond);
        System.out.println();
        
        // 2. 公平锁 vs 非公平锁
        System.out.println("2. 公平锁 vs 非公平锁性能对比：");
        
        ReentrantLockTest unfairTest = new ReentrantLockTest(false);
        PerformanceResult unfairResult = runPerformanceTest("非公平锁", 
                                                           unfairTest::increment);
        
        ReentrantLockTest fairTest = new ReentrantLockTest(true);
        PerformanceResult fairResult = runPerformanceTest("公平锁", 
                                                         fairTest::increment);
        
        System.out.println(unfairResult);
        System.out.println(fairResult);
        System.out.printf("性能比较: 非公平锁是公平锁的 %.2f 倍%n", 
                         unfairResult.operationsPerSecond / fairResult.operationsPerSecond);
        System.out.println();
        
        // 3. 竞争强度测试
        testContentionLevels();
        
        // 4. AQS队列机制演示
        AQSQueueDemo queueDemo = new AQSQueueDemo();
        queueDemo.demonstrateQueue();
        
        System.out.println("=== AQS性能测试完成 ===");
    }
}

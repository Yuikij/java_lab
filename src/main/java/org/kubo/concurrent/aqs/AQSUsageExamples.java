package org.kubo.concurrent.aqs;

import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AQS使用示例 - 展示基于AQS实现的标准JUC类的使用
 * 
 * 主要包括：
 * 1. ReentrantLock - 可重入独占锁
 * 2. ReentrantReadWriteLock - 读写锁
 * 3. CountDownLatch - 倒计时门闩
 * 4. Semaphore - 信号量
 * 5. CyclicBarrier - 循环屏障
 */
public class AQSUsageExamples {
    
    /**
     * ReentrantLock使用示例
     * 特点：可重入、可中断、可超时、公平/非公平
     */
    public static class ReentrantLockExample {
        private final ReentrantLock lock = new ReentrantLock();
        private final AtomicInteger counter = new AtomicInteger(0);
        
        public void increment() {
            lock.lock();
            try {
                counter.incrementAndGet();
                // 演示可重入特性
                recursiveMethod(3);
            } finally {
                lock.unlock();
            }
        }
        
        private void recursiveMethod(int depth) {
            if (depth <= 0) return;
            
            lock.lock(); // 同一线程可重复获取锁
            try {
                System.out.println("递归深度: " + depth + ", 持有计数: " + lock.getHoldCount());
                recursiveMethod(depth - 1);
            } finally {
                lock.unlock();
            }
        }
        
        public boolean tryIncrementWithTimeout(long timeout, TimeUnit unit) 
                throws InterruptedException {
            if (lock.tryLock(timeout, unit)) {
                try {
                    counter.incrementAndGet();
                    return true;
                } finally {
                    lock.unlock();
                }
            }
            return false;
        }
        
        public int getCounter() {
            return counter.get();
        }
        
        public boolean isLocked() {
            return lock.isLocked();
        }
    }
    
    /**
     * ReadWriteLock使用示例
     * 特点：读读并发、读写互斥、写写互斥
     */
    public static class ReadWriteLockExample {
        private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
        private final Lock readLock = rwLock.readLock();
        private final Lock writeLock = rwLock.writeLock();
        private volatile String data = "初始数据";
        
        public String readData() {
            readLock.lock();
            try {
                System.out.println(Thread.currentThread().getName() + " 正在读取数据...");
                Thread.sleep(1000); // 模拟读取操作
                return data;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            } finally {
                readLock.unlock();
            }
        }
        
        public void writeData(String newData) {
            writeLock.lock();
            try {
                System.out.println(Thread.currentThread().getName() + " 正在写入数据...");
                Thread.sleep(2000); // 模拟写入操作
                data = newData;
                System.out.println(Thread.currentThread().getName() + " 写入完成: " + newData);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                writeLock.unlock();
            }
        }
        
        public int getReadLockCount() {
            return rwLock.getReadLockCount();
        }
        
        public boolean isWriteLocked() {
            return rwLock.isWriteLocked();
        }
    }
    
    /**
     * CountDownLatch使用示例
     * 特点：一次性使用、主线程等待工作线程完成
     */
    public static class CountDownLatchExample {
        public static void demonstrateCountDownLatch() throws InterruptedException {
            int workerCount = 5;
            CountDownLatch startLatch = new CountDownLatch(1); // 控制所有线程同时开始
            CountDownLatch finishLatch = new CountDownLatch(workerCount); // 等待所有线程完成
            
            // 创建工作线程
            for (int i = 0; i < workerCount; i++) {
                final int workerId = i;
                new Thread(() -> {
                    try {
                        System.out.println("工作线程 " + workerId + " 准备就绪，等待开始信号");
                        startLatch.await(); // 等待开始信号
                        
                        System.out.println("工作线程 " + workerId + " 开始工作");
                        Thread.sleep((workerId + 1) * 500); // 模拟工作
                        System.out.println("工作线程 " + workerId + " 完成工作");
                        
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        finishLatch.countDown(); // 标记完成
                    }
                }).start();
            }
            
            Thread.sleep(1000); // 等待所有线程就绪
            System.out.println("主线程发出开始信号");
            startLatch.countDown(); // 发出开始信号
            
            System.out.println("主线程等待所有工作线程完成...");
            finishLatch.await(); // 等待所有工作完成
            System.out.println("所有工作线程完成，主线程继续执行");
        }
    }
    
    /**
     * Semaphore使用示例
     * 特点：控制同时访问资源的线程数量
     */
    public static class SemaphoreExample {
        private final Semaphore semaphore;
        private final AtomicInteger activeUsers = new AtomicInteger(0);
        
        public SemaphoreExample(int permits) {
            this.semaphore = new Semaphore(permits);
        }
        
        public void accessResource(String userId) {
            try {
                System.out.println(userId + " 尝试获取资源访问权限，可用许可：" + 
                                 semaphore.availablePermits());
                semaphore.acquire(); // 获取许可
                
                int current = activeUsers.incrementAndGet();
                System.out.println(userId + " 获得访问权限，当前活跃用户：" + current);
                
                // 模拟使用资源
                Thread.sleep(2000);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                int current = activeUsers.decrementAndGet();
                System.out.println(userId + " 释放资源，当前活跃用户：" + current);
                semaphore.release(); // 释放许可
            }
        }
        
        public int getAvailablePermits() {
            return semaphore.availablePermits();
        }
        
        public int getQueueLength() {
            return semaphore.getQueueLength();
        }
    }
    
    /**
     * CyclicBarrier使用示例
     * 特点：可重复使用、所有线程到达屏障点后一起继续
     */
    public static class CyclicBarrierExample {
        public static void demonstrateCyclicBarrier() throws InterruptedException {
            int parties = 3;
            CyclicBarrier barrier = new CyclicBarrier(parties, () -> {
                System.out.println("=== 所有线程都到达屏障，开始下一阶段 ===");
            });
            
            // 创建工作线程
            Thread[] threads = new Thread[parties];
            for (int i = 0; i < parties; i++) {
                final int workerId = i;
                threads[i] = new Thread(() -> {
                    try {
                        for (int phase = 1; phase <= 2; phase++) {
                            System.out.println("线程 " + workerId + " 完成阶段 " + phase);
                            Thread.sleep((workerId + 1) * 1000);
                            
                            System.out.println("线程 " + workerId + " 到达屏障，等待其他线程");
                            barrier.await(); // 等待其他线程
                            
                            System.out.println("线程 " + workerId + " 通过屏障，继续执行");
                        }
                    } catch (InterruptedException | BrokenBarrierException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            
            for (Thread thread : threads) {
                thread.start();
            }
            
            for (Thread thread : threads) {
                thread.join();
            }
            
            System.out.println("CyclicBarrier可以重复使用，当前parties: " + barrier.getParties());
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== AQS使用示例演示 ===\n");
        
        demonstrateReentrantLock();
        System.out.println();
        
        demonstrateReadWriteLock();
        System.out.println();
        
        System.out.println("3. CountDownLatch演示：");
        CountDownLatchExample.demonstrateCountDownLatch();
        System.out.println();
        
        demonstrateSemaphore();
        System.out.println();
        
        System.out.println("5. CyclicBarrier演示：");
        CyclicBarrierExample.demonstrateCyclicBarrier();
    }
    
    private static void demonstrateReentrantLock() throws InterruptedException {
        System.out.println("1. ReentrantLock演示：");
        ReentrantLockExample example = new ReentrantLockExample();
        
        // 创建多个线程竞争锁
        Thread[] threads = new Thread[3];
        for (int i = 0; i < threads.length; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                System.out.println("线程 " + threadId + " 开始执行");
                example.increment();
                System.out.println("线程 " + threadId + " 执行完毕，计数器: " + example.getCounter());
            });
        }
        
        for (Thread thread : threads) {
            thread.start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        // 演示超时获取锁
        Thread timeoutThread = new Thread(() -> {
            try {
                boolean acquired = example.tryIncrementWithTimeout(500, TimeUnit.MILLISECONDS);
                System.out.println("超时获取锁结果: " + acquired);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        timeoutThread.start();
        timeoutThread.join();
    }
    
    private static void demonstrateReadWriteLock() throws InterruptedException {
        System.out.println("2. ReadWriteLock演示：");
        ReadWriteLockExample example = new ReadWriteLockExample();
        
        // 创建读线程
        Thread[] readers = new Thread[3];
        for (int i = 0; i < readers.length; i++) {
            final int readerId = i;
            readers[i] = new Thread(() -> {
                String data = example.readData();
                System.out.println("读线程 " + readerId + " 读取到: " + data);
            }, "Reader-" + i);
        }
        
        // 创建写线程
        Thread writer = new Thread(() -> {
            example.writeData("新数据-" + System.currentTimeMillis());
        }, "Writer");
        
        // 启动所有线程
        for (Thread reader : readers) {
            reader.start();
        }
        Thread.sleep(500);
        writer.start();
        
        // 等待所有线程完成
        for (Thread reader : readers) {
            reader.join();
        }
        writer.join();
        
        System.out.println("读锁持有数: " + example.getReadLockCount());
        System.out.println("写锁是否被持有: " + example.isWriteLocked());
    }
    
    private static void demonstrateSemaphore() throws InterruptedException {
        System.out.println("4. Semaphore演示（最多2个并发用户）：");
        SemaphoreExample example = new SemaphoreExample(2);
        
        // 创建多个用户尝试访问资源
        Thread[] users = new Thread[5];
        for (int i = 0; i < users.length; i++) {
            final String userId = "用户" + i;
            users[i] = new Thread(() -> example.accessResource(userId));
        }
        
        for (Thread user : users) {
            user.start();
        }
        
        for (Thread user : users) {
            user.join();
        }
        
        System.out.println("最终可用许可: " + example.getAvailablePermits());
    }
}

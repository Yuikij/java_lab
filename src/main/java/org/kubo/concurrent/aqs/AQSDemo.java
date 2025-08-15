package org.kubo.concurrent.aqs;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * AQS (AbstractQueuedSynchronizer) 演示
 * 
 * AQS是Java并发包的核心基础框架，提供了一个基于FIFO队列的同步框架。
 * 它是ReentrantLock、CountDownLatch、Semaphore等同步器的基础。
 * 
 * AQS核心思想：
 * 1. 使用一个int类型的state变量表示同步状态
 * 2. 使用FIFO队列来管理等待线程
 * 3. 提供独占和共享两种模式
 * 4. 支持可中断的锁获取以及超时获取
 */
public class AQSDemo {
    
    /**
     * 自定义独占锁 - 基于AQS实现
     * 演示AQS的独占模式使用
     */
    public static class CustomExclusiveLock {
        private final Sync sync = new Sync();
        
        private static class Sync extends AbstractQueuedSynchronizer {
            // 是否被独占
            @Override
            protected boolean isHeldExclusively() {
                return getState() == 1;
            }
            
            // 尝试获取独占锁
            @Override
            public boolean tryAcquire(int acquires) {
                if (compareAndSetState(0, 1)) {
                    setExclusiveOwnerThread(Thread.currentThread());
                    return true;
                }
                return false;
            }
            
            // 尝试释放独占锁
            @Override
            protected boolean tryRelease(int releases) {
                if (getState() == 0) {
                    throw new IllegalMonitorStateException();
                }
                setExclusiveOwnerThread(null);
                setState(0);
                return true;
            }
        }
        
        public void lock() {
            sync.acquire(1);
        }
        
        public boolean tryLock() {
            return sync.tryAcquire(1);
        }
        
        public void unlock() {
            sync.release(1);
        }
        
        public boolean isLocked() {
            return sync.isHeldExclusively();
        }
    }
    
    /**
     * 自定义共享锁 - 基于AQS实现
     * 演示AQS的共享模式使用（类似Semaphore）
     */
    public static class CustomSharedLock {
        private final Sync sync;
        
        public CustomSharedLock(int permits) {
            sync = new Sync(permits);
        }
        
        private static class Sync extends AbstractQueuedSynchronizer {
            Sync(int permits) {
                setState(permits);
            }
            
            @Override
            protected int tryAcquireShared(int acquires) {
                for (;;) {
                    int available = getState();
                    int remaining = available - acquires;
                    if (remaining < 0 || compareAndSetState(available, remaining)) {
                        return remaining;
                    }
                }
            }
            
            @Override
            protected boolean tryReleaseShared(int releases) {
                for (;;) {
                    int current = getState();
                    int next = current + releases;
                    if (next < current) { // overflow
                        throw new Error("Maximum permit count exceeded");
                    }
                    if (compareAndSetState(current, next)) {
                        return true;
                    }
                }
            }
        }
        
        public void acquire() throws InterruptedException {
            sync.acquireSharedInterruptibly(1);
        }
        
        public boolean tryAcquire() {
            return sync.tryAcquireShared(1) >= 0;
        }
        
        public boolean tryAcquire(long timeout, TimeUnit unit) throws InterruptedException {
            return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
        }
        
        public void release() {
            sync.releaseShared(1);
        }
        
        public int availablePermits() {
            return sync.getState();
        }
    }
    
    /**
     * 自定义CountDownLatch - 基于AQS实现
     * 演示AQS共享模式的另一种应用
     */
    public static class CustomCountDownLatch {
        private final Sync sync;
        
        public CustomCountDownLatch(int count) {
            if (count < 0) throw new IllegalArgumentException("count < 0");
            this.sync = new Sync(count);
        }
        
        private static class Sync extends AbstractQueuedSynchronizer {
            Sync(int count) {
                setState(count);
            }
            
            int getCount() {
                return getState();
            }
            
            @Override
            protected int tryAcquireShared(int acquires) {
                return getState() == 0 ? 1 : -1;
            }
            
            @Override
            protected boolean tryReleaseShared(int releases) {
                for (;;) {
                    int c = getState();
                    if (c == 0) return false;
                    int nextc = c - 1;
                    if (compareAndSetState(c, nextc)) {
                        return nextc == 0;
                    }
                }
            }
        }
        
        public void await() throws InterruptedException {
            sync.acquireSharedInterruptibly(1);
        }
        
        public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
            return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
        }
        
        public void countDown() {
            sync.releaseShared(1);
        }
        
        public long getCount() {
            return sync.getCount();
        }
        
        @Override
        public String toString() {
            return super.toString() + "[Count = " + sync.getCount() + "]";
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== AQS 演示 ===\n");
        
        demonstrateExclusiveLock();
        System.out.println();
        
        demonstrateSharedLock();
        System.out.println();
        
        demonstrateCountDownLatch();
    }
    
    /**
     * 演示自定义独占锁
     */
    private static void demonstrateExclusiveLock() throws InterruptedException {
        System.out.println("1. 自定义独占锁演示：");
        CustomExclusiveLock lock = new CustomExclusiveLock();
        
        // 创建多个线程竞争锁
        Thread[] threads = new Thread[3];
        for (int i = 0; i < threads.length; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                try {
                    System.out.println("线程 " + threadId + " 尝试获取锁");
                    lock.lock();
                    System.out.println("线程 " + threadId + " 获取到锁，正在执行...");
                    Thread.sleep(1000);
                    System.out.println("线程 " + threadId + " 执行完毕，释放锁");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock();
                }
            });
        }
        
        for (Thread thread : threads) {
            thread.start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
    }
    
    /**
     * 演示自定义共享锁
     */
    private static void demonstrateSharedLock() throws InterruptedException {
        System.out.println("2. 自定义共享锁演示（permits=2）：");
        CustomSharedLock sharedLock = new CustomSharedLock(2);
        
        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                try {
                    System.out.println("线程 " + threadId + " 尝试获取共享锁，剩余许可：" + 
                                     sharedLock.availablePermits());
                    sharedLock.acquire();
                    System.out.println("线程 " + threadId + " 获取到共享锁，剩余许可：" + 
                                     sharedLock.availablePermits());
                    Thread.sleep(2000);
                    System.out.println("线程 " + threadId + " 释放共享锁");
                    sharedLock.release();
                } catch (InterruptedException e) {
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
    }
    
    /**
     * 演示自定义CountDownLatch
     */
    private static void demonstrateCountDownLatch() throws InterruptedException {
        System.out.println("3. 自定义CountDownLatch演示：");
        CustomCountDownLatch latch = new CustomCountDownLatch(3);
        
        // 创建等待线程
        Thread waiter = new Thread(() -> {
            try {
                System.out.println("主线程等待所有工作线程完成...");
                latch.await();
                System.out.println("所有工作线程完成，主线程继续执行");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        // 创建工作线程
        Thread[] workers = new Thread[3];
        for (int i = 0; i < workers.length; i++) {
            final int workerId = i;
            workers[i] = new Thread(() -> {
                try {
                    System.out.println("工作线程 " + workerId + " 开始工作");
                    Thread.sleep((workerId + 1) * 1000);
                    System.out.println("工作线程 " + workerId + " 完成工作");
                    latch.countDown();
                    System.out.println("CountDown后，剩余计数：" + latch.getCount());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        waiter.start();
        for (Thread worker : workers) {
            worker.start();
        }
        
        waiter.join();
        for (Thread worker : workers) {
            worker.join();
        }
    }
}

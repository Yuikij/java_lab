package org.kubo.concurrent.memory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Java并发编程三大特性演示：原子性、可见性、有序性
 * 
 * 这个类通过实际代码演示并发编程中最重要的三个概念：
 * 1. 原子性 (Atomicity) - 操作要么全部执行，要么全部不执行
 * 2. 可见性 (Visibility) - 一个线程对共享变量的修改，其他线程能够立即看到
 * 3. 有序性 (Ordering) - 程序执行的顺序按照代码的先后顺序执行
 * 
 * @author kubo
 */
public class ConcurrencyPropertiesDemo {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("      Java并发编程三大特性演示");
        System.out.println("=================================================");
        
        // 1. 原子性演示
        System.out.println("\n1. 原子性 (Atomicity) 演示");
        System.out.println("------------------------------------------");
        demonstrateAtomicity();
        
        // 2. 可见性演示
        System.out.println("\n2. 可见性 (Visibility) 演示");
        System.out.println("------------------------------------------");
        demonstrateVisibility();
        
        // 3. 有序性演示
        System.out.println("\n3. 有序性 (Ordering) 演示");
        System.out.println("------------------------------------------");
        demonstrateOrdering();
        
        System.out.println("\n=================================================");
        System.out.println("      演示完成");
        System.out.println("=================================================");
    }
    
    /**
     * 原子性演示
     * 原子性是指一个操作或者多个操作要么全部执行并且执行的过程不会被任何因素打断，要么就都不执行
     */
    private static void demonstrateAtomicity() {
        System.out.println("原子性：确保操作不可分割，要么全部执行成功，要么全部失败");
        
        AtomicityDemo demo = new AtomicityDemo();
        demo.runAtomicityTest();
    }
    
    /**
     * 可见性演示
     * 可见性是指当多个线程访问同一个变量时，一个线程修改了这个变量的值，其他线程能够立即看得到修改的值
     */
    private static void demonstrateVisibility() {
        System.out.println("可见性：确保一个线程对共享变量的修改能被其他线程立即看到");
        
        VisibilityDemo demo = new VisibilityDemo();
        demo.runVisibilityTest();
    }
    
    /**
     * 有序性演示
     * 有序性是指程序执行的顺序按照代码的先后顺序执行
     */
    private static void demonstrateOrdering() {
        System.out.println("有序性：确保程序按照代码顺序执行，防止指令重排序带来的问题");
        
        OrderingDemo demo = new OrderingDemo();
        demo.runOrderingTest();
    }
}

/**
 * 原子性演示类
 */
class AtomicityDemo {
    private int normalCounter = 0;
    private AtomicInteger atomicCounter = new AtomicInteger(0);
    private int synchronizedCounter = 0;
    // private final ReentrantLock lock = new ReentrantLock(); // 暂时不使用，避免警告
    
    public void runAtomicityTest() {
        int threadCount = 10;
        int incrementsPerThread = 1000;
        
        System.out.println("启动 " + threadCount + " 个线程，每个线程执行 " + incrementsPerThread + " 次递增操作");
        
        // 测试普通变量（非原子性）
        testNormalCounter(threadCount, incrementsPerThread);
        
        // 测试原子变量（原子性）
        testAtomicCounter(threadCount, incrementsPerThread);
        
        // 测试同步方法（原子性）
        testSynchronizedCounter(threadCount, incrementsPerThread);
    }
    
    private void testNormalCounter(int threadCount, int incrementsPerThread) {
        normalCounter = 0;
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    normalCounter++; // 非原子操作：读取->计算->写入，可能被其他线程打断
                }
                latch.countDown();
            }, "普通计数器线程-" + i).start();
        }
        
        try {
            latch.await();
            long endTime = System.currentTimeMillis();
            
            int expectedValue = threadCount * incrementsPerThread;
            System.out.println("【普通变量测试】");
            System.out.println("  预期结果: " + expectedValue);
            System.out.println("  实际结果: " + normalCounter);
            System.out.println("  是否正确: " + (normalCounter == expectedValue ? "✓" : "✗"));
            System.out.println("  耗时: " + (endTime - startTime) + "ms");
            
            if (normalCounter != expectedValue) {
                System.out.println("  💡 分析: 普通变量的++操作不是原子的，在并发环境下会丢失更新");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private void testAtomicCounter(int threadCount, int incrementsPerThread) {
        atomicCounter.set(0);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    atomicCounter.incrementAndGet(); // 原子操作：CAS机制保证原子性
                }
                latch.countDown();
            }, "原子计数器线程-" + i).start();
        }
        
        try {
            latch.await();
            long endTime = System.currentTimeMillis();
            
            int expectedValue = threadCount * incrementsPerThread;
            System.out.println("\n【原子变量测试】");
            System.out.println("  预期结果: " + expectedValue);
            System.out.println("  实际结果: " + atomicCounter.get());
            System.out.println("  是否正确: " + (atomicCounter.get() == expectedValue ? "✓" : "✗"));
            System.out.println("  耗时: " + (endTime - startTime) + "ms");
            System.out.println("  💡 分析: AtomicInteger使用CAS操作保证了原子性");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private void testSynchronizedCounter(int threadCount, int incrementsPerThread) {
        synchronizedCounter = 0;
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    incrementSynchronizedCounter(); // 同步方法保证原子性
                }
                latch.countDown();
            }, "同步计数器线程-" + i).start();
        }
        
        try {
            latch.await();
            long endTime = System.currentTimeMillis();
            
            int expectedValue = threadCount * incrementsPerThread;
            System.out.println("\n【同步方法测试】");
            System.out.println("  预期结果: " + expectedValue);
            System.out.println("  实际结果: " + synchronizedCounter);
            System.out.println("  是否正确: " + (synchronizedCounter == expectedValue ? "✓" : "✗"));
            System.out.println("  耗时: " + (endTime - startTime) + "ms");
            System.out.println("  💡 分析: synchronized关键字通过互斥锁保证了原子性");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private synchronized void incrementSynchronizedCounter() {
        synchronizedCounter++;
    }
}

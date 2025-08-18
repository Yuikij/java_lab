package org.kubo.concurrent.memory;

import java.util.concurrent.CountDownLatch;


/**
 * 有序性演示类
 * 
 * 有序性是指程序执行的顺序按照代码的先后顺序执行。
 * 但是在多线程环境下，由于编译器优化、CPU指令重排序等原因，
 * 程序的执行顺序可能与代码顺序不一致，这可能导致并发问题。
 * 
 * Java内存模型(JMM)通过happens-before规则来保证有序性：
 * 1. 程序顺序规则：同一线程内的操作按照程序顺序执行
 * 2. 监视器锁规则：unlock操作happen-before后续的lock操作
 * 3. volatile变量规则：对volatile变量的写happen-before后续对该变量的读
 * 4. 传递性规则：如果A happen-before B，B happen-before C，则A happen-before C
 * 
 * @author kubo
 */
public class OrderingDemo {
    
    // 注：这里移除了未使用的字段来避免编译警告
    
    public void runOrderingTest() {
        System.out.println("有序性测试说明：");
        System.out.println("- 通过多次执行两个线程的操作，观察指令重排序的影响");
        System.out.println("- 演示volatile关键字如何防止指令重排序");
        System.out.println("- 展示happens-before规则的实际效果");
        System.out.println();
        
        // 测试指令重排序
        testInstructionReordering();
        
        // 测试volatile的内存屏障效果
        testVolatileMemoryBarrier();
        
        // 测试synchronized的有序性保证
        testSynchronizedOrdering();
    }
    
    /**
     * 测试指令重排序现象
     * 经典的指令重排序示例：两个线程同时执行，观察是否会出现(0,0)的结果
     */
    private void testInstructionReordering() {
        System.out.println("【指令重排序测试】");
        System.out.println("测试场景：");
        System.out.println("  线程1: x=1; r1=y");
        System.out.println("  线程2: y=1; r2=x");
        System.out.println("理论上不应该出现r1=0且r2=0的情况，但指令重排序可能导致这种结果");
        System.out.println();
        
        int testRounds = 100000;
        int reorderingCases = 0;
        
        for (int round = 0; round < testRounds; round++) {
            // 用于接收结果的数组
            int[] results = new int[2];
            CountDownLatch latch = new CountDownLatch(2);
            
            // 创建共享变量的容器
            int[] sharedVars = new int[2]; // [0]=x, [1]=y
            
            // 线程1
            Thread thread1 = new Thread(() -> {
                sharedVars[0] = 1;        // 写操作1: x = 1
                results[0] = sharedVars[1]; // 读操作1: r1 = y
                latch.countDown();
            });
            
            // 线程2  
            Thread thread2 = new Thread(() -> {
                sharedVars[1] = 1;        // 写操作2: y = 1
                results[1] = sharedVars[0]; // 读操作2: r2 = x
                latch.countDown();
            });
            
            // 启动线程
            thread1.start();
            thread2.start();
            
            try {
                latch.await();
                
                // 检查结果
                if (results[0] == 0 && results[1] == 0) {
                    reorderingCases++;
                }
                
                thread1.join();
                thread2.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        System.out.println("测试轮数: " + testRounds);
        System.out.println("出现重排序的次数: " + reorderingCases);
        System.out.println("重排序概率: " + String.format("%.6f%%", (double)reorderingCases / testRounds * 100));
        
        if (reorderingCases > 0) {
            System.out.println("✓ 检测到指令重排序现象");
            System.out.println("💡 分析: CPU和编译器的优化可能改变指令执行顺序");
        } else {
            System.out.println("⚠ 本次测试未检测到指令重排序（可能需要更多轮次或特定硬件环境）");
        }
        System.out.println();
    }
    
    /**
     * 测试volatile的内存屏障效果
     * volatile写操作会在前面插入StoreStore屏障，在后面插入StoreLoad屏障
     * volatile读操作会在后面插入LoadLoad和LoadStore屏障
     */
    private void testVolatileMemoryBarrier() {
        System.out.println("【volatile内存屏障测试】");
        System.out.println("volatile关键字通过内存屏障防止指令重排序：");
        System.out.println("- volatile写之前的操作不会被重排序到volatile写之后");
        System.out.println("- volatile读之后的操作不会被重排序到volatile读之前");
        System.out.println();
        
        VolatileOrderingExample example = new VolatileOrderingExample();
        
        // 测试volatile变量的有序性保证
        int testRounds = 10000;
        final int[] violations = {0}; // 使用数组来解决final问题
        
        for (int round = 0; round < testRounds; round++) {
            example.reset();
            
            CountDownLatch latch = new CountDownLatch(2);
            
            // 写入线程
            Thread writerThread = new Thread(() -> {
                example.writer();
                latch.countDown();
            });
            
            // 读取线程
            Thread readerThread = new Thread(() -> {
                if (!example.reader()) {
                    violations[0]++;
                }
                latch.countDown();
            });
            
            writerThread.start();
            readerThread.start();
            
            try {
                latch.await();
                writerThread.join();
                readerThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        System.out.println("测试轮数: " + testRounds);
        System.out.println("有序性违反次数: " + violations[0]);
        System.out.println("成功率: " + String.format("%.2f%%", (double)(testRounds - violations[0]) / testRounds * 100));
        
        if (violations[0] == 0) {
            System.out.println("✓ volatile成功保证了有序性");
        } else {
            System.out.println("⚠ 检测到有序性违反，可能需要调整测试条件");
        }
        System.out.println();
    }
    
    /**
     * 测试synchronized的有序性保证
     * synchronized关键字保证互斥执行，从而保证有序性
     */
    private void testSynchronizedOrdering() {
        System.out.println("【synchronized有序性测试】");
        System.out.println("synchronized通过互斥锁保证临界区内操作的有序性");
        System.out.println();
        
        SynchronizedOrderingExample example = new SynchronizedOrderingExample();
        
        int threadCount = 10;
        int operationsPerThread = 1000;
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        long startTime = System.currentTimeMillis();
        
        // 启动多个线程并发执行
        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    example.synchronizedOperation();
                }
                latch.countDown();
            }, "同步线程-" + i).start();
        }
        
        try {
            latch.await();
            long endTime = System.currentTimeMillis();
            
            boolean isConsistent = example.checkConsistency();
            
            System.out.println("线程数量: " + threadCount);
            System.out.println("每线程操作数: " + operationsPerThread);
            System.out.println("总操作数: " + (threadCount * operationsPerThread));
            System.out.println("实际计数: " + example.getCounter());
            System.out.println("数据一致性: " + (isConsistent ? "✓" : "✗"));
            System.out.println("执行时间: " + (endTime - startTime) + "ms");
            
            if (isConsistent) {
                System.out.println("✓ synchronized成功保证了有序性和一致性");
            } else {
                System.out.println("✗ 检测到数据不一致，可能存在并发问题");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println();
    }
}

/**
 * volatile有序性示例
 */
class VolatileOrderingExample {
    private int normalVar = 0;
    private volatile boolean flag = false;
    
    public void reset() {
        normalVar = 0;
        flag = false;
    }
    
    public void writer() {
        normalVar = 42;    // 普通变量写入
        flag = true;       // volatile变量写入（内存屏障确保之前的写入不会重排序到这之后）
    }
    
    public boolean reader() {
        if (flag) {        // volatile变量读取（内存屏障确保之后的读取不会重排序到这之前）
            return normalVar == 42; // 如果flag为true，normalVar应该已经是42
        }
        return true; // 如果flag还是false，认为测试正常
    }
}

/**
 * synchronized有序性示例
 */
class SynchronizedOrderingExample {
    private int counter = 0;
    private int checkSum = 0;
    
    public synchronized void synchronizedOperation() {
        // 在同步块中，这些操作按顺序执行，不会被重排序
        int oldValue = counter;
        counter++;
        checkSum += counter;
        
        // 确保操作的原子性和有序性
        assert counter == oldValue + 1 : "Counter increment failed";
    }
    
    public synchronized int getCounter() {
        return counter;
    }
    
    public synchronized boolean checkConsistency() {
        // 检查数据的一致性
        // checkSum应该等于1+2+3+...+counter = counter*(counter+1)/2
        int expectedCheckSum = counter * (counter + 1) / 2;
        return checkSum == expectedCheckSum;
    }
}

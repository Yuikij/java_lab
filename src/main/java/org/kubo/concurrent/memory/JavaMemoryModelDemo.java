package org.kubo.concurrent.memory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * Java内存模型(JMM)核心概念演示
 * 
 * Java内存模型定义了Java程序中多线程程序的内存访问规则，包括：
 * 1. 主内存与工作内存的关系
 * 2. 内存间交互操作（load、store、read、write等）
 * 3. volatile语义
 * 4. synchronized语义
 * 5. final语义
 * 6. happens-before规则
 * 
 * @author kubo
 */
public class JavaMemoryModelDemo {
    
    public static void main(String[] args) {
        System.out.println("===============================================");
        System.out.println("           Java内存模型(JMM)演示");
        System.out.println("===============================================\n");
        
        // 演示主内存与工作内存
        demonstrateMainMemoryAndWorkingMemory();
        
        sleep(2000);
        
        // 演示volatile的内存语义
        demonstrateVolatileSemantics();
        
        sleep(2000);
        
        // 演示synchronized的内存语义
        demonstrateSynchronizedSemantics();
        
        sleep(2000);
        
        // 演示final的内存语义
        demonstrateFinalSemantics();
        
        sleep(2000);
        
        // 演示对象构造过程的内存模型
        demonstrateObjectConstructionMemoryModel();
    }
    
    /**
     * 演示主内存与工作内存的关系
     */
    private static void demonstrateMainMemoryAndWorkingMemory() {
        System.out.println("🧠 主内存与工作内存演示");
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("每个线程都有自己的工作内存，线程间不能直接访问对方的工作内存");
        System.out.println("所有共享变量都存储在主内存中");
        System.out.println();
        
        SharedVariableExample example = new SharedVariableExample();
        
        // 启动写线程
        Thread writerThread = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                example.updateValue(i * 10);
                System.out.println("Writer线程: 将共享变量更新为 " + (i * 10));
                sleep(500);
            }
        }, "Writer-Thread");
        
        // 启动读线程
        Thread readerThread = new Thread(() -> {
            int lastValue = 0;
            while (!Thread.currentThread().isInterrupted()) {
                int currentValue = example.getValue();
                if (currentValue != lastValue) {
                    System.out.println("Reader线程: 检测到共享变量变化 " + lastValue + " -> " + currentValue);
                    lastValue = currentValue;
                }
                
                if (currentValue >= 50) {
                    break;
                }
                sleep(100);
            }
        }, "Reader-Thread");
        
        writerThread.start();
        readerThread.start();
        
        try {
            writerThread.join();
            readerThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("主内存与工作内存演示完成\n");
    }
    
    /**
     * 演示volatile的内存语义
     */
    private static void demonstrateVolatileSemantics() {
        System.out.println("⚡ volatile内存语义演示");
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("volatile保证：");
        System.out.println("1. 可见性：修改立即同步到主内存，读取时从主内存获取最新值");
        System.out.println("2. 有序性：禁止特定的指令重排序");
        System.out.println("3. 原子性：对volatile变量的单次读/写是原子的");
        System.out.println();
        
        VolatileSemanticExample example = new VolatileSemanticExample();
        
        // 启动多个线程测试volatile的可见性
        Thread[] threads = new Thread[3];
        
        for (int i = 0; i < threads.length; i++) {
            final int threadId = i + 1;
            threads[i] = new Thread(() -> {
                // 等待一段时间后开始工作
                sleep(threadId * 200);
                
                example.performWork(threadId);
                
                System.out.println("线程" + threadId + ": 工作完成，设置完成标志");
                example.setCompleted(threadId);
                
            }, "Worker-" + threadId);
        }
        
        // 启动监控线程
        Thread monitorThread = new Thread(() -> {
            while (example.getCompletedCount() < 3) {
                System.out.println("Monitor: 当前完成的线程数 = " + example.getCompletedCount());
                sleep(300);
            }
            System.out.println("Monitor: 所有线程都已完成！");
        }, "Monitor");
        
        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }
        monitorThread.start();
        
        try {
            for (Thread thread : threads) {
                thread.join();
            }
            monitorThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("volatile语义演示完成\n");
    }
    
    /**
     * 演示synchronized的内存语义
     */
    private static void demonstrateSynchronizedSemantics() {
        System.out.println("🔒 synchronized内存语义演示");
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("synchronized保证：");
        System.out.println("1. 原子性：同步块内的操作具有原子性");
        System.out.println("2. 可见性：进入同步块时从主内存读取，退出时写回主内存");
        System.out.println("3. 有序性：同步块内的操作不会被重排序到同步块外");
        System.out.println();
        
        SynchronizedSemanticExample example = new SynchronizedSemanticExample();
        
        // 启动多个线程进行并发操作
        Thread[] threads = new Thread[5];
        final int operationsPerThread = 1000;
        
        for (int i = 0; i < threads.length; i++) {
            final int threadId = i + 1;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    example.increment();
                    example.updateData(threadId, j + 1);
                }
                System.out.println("线程" + threadId + ": 完成 " + operationsPerThread + " 次操作");
            }, "SyncWorker-" + threadId);
        }
        
        long startTime = System.currentTimeMillis();
        
        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }
        
        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long endTime = System.currentTimeMillis();
        
        int expectedCount = threads.length * operationsPerThread;
        int actualCount = example.getCounter();
        
        System.out.println("预期计数: " + expectedCount);
        System.out.println("实际计数: " + actualCount);
        System.out.println("数据一致性: " + (expectedCount == actualCount ? "✓" : "✗"));
        System.out.println("执行时间: " + (endTime - startTime) + "ms");
        
        example.printData();
        
        System.out.println("synchronized语义演示完成\n");
    }
    
    /**
     * 演示final的内存语义
     */
    private static void demonstrateFinalSemantics() {
        System.out.println("🛡️ final内存语义演示");
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("final字段的内存语义：");
        System.out.println("1. 构造函数内对final字段的写入，在构造函数返回前对其他线程可见");
        System.out.println("2. 对final字段的读取不能重排序到构造函数之前");
        System.out.println("3. final字段一旦初始化完成，就保证了不可变性和可见性");
        System.out.println();
        
        // 测试final字段的初始化安全性
        FinalSemanticExample.testFinalFieldSafety();
        
        System.out.println("final语义演示完成\n");
    }
    
    /**
     * 演示对象构造过程的内存模型
     */
    private static void demonstrateObjectConstructionMemoryModel() {
        System.out.println("🏗️ 对象构造过程内存模型演示");
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("对象构造过程中的内存模型问题：");
        System.out.println("1. 对象引用在构造完成前就可能被其他线程看到");
        System.out.println("2. 其他线程可能看到部分构造的对象");
        System.out.println("3. final字段可以保证构造安全性");
        System.out.println();
        
        ObjectConstructionExample.demonstrateConstructionRace();
        
        System.out.println("对象构造内存模型演示完成\n");
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
 * 共享变量示例（演示主内存与工作内存）
 */
class SharedVariableExample {
    private int sharedValue = 0;
    
    public void updateValue(int newValue) {
        this.sharedValue = newValue;
        // 注意：普通变量的修改可能不会立即同步到主内存
    }
    
    public int getValue() {
        return this.sharedValue;
        // 注意：读取时可能从工作内存获取缓存值，而不是主内存的最新值
    }
}

/**
 * volatile语义示例
 */
class VolatileSemanticExample {
    private volatile int completedCount = 0;
    private volatile boolean[] threadCompleted = new boolean[3];
    
    public void performWork(int threadId) {
        System.out.println("线程" + threadId + ": 开始执行工作");
        
        // 模拟工作负载
        long sum = 0;
        for (int i = 0; i < 1000000; i++) {
            sum += i;
        }
        
        System.out.println("线程" + threadId + ": 工作执行完毕，结果=" + sum);
    }
    
    public void setCompleted(int threadId) {
        threadCompleted[threadId - 1] = true;
        
        // 统计完成的线程数
        int count = 0;
        for (boolean completed : threadCompleted) {
            if (completed) count++;
        }
        
        // volatile写操作，保证立即同步到主内存
        completedCount = count;
    }
    
    public int getCompletedCount() {
        // volatile读操作，保证从主内存读取最新值
        return completedCount;
    }
}

/**
 * synchronized语义示例
 */
class SynchronizedSemanticExample {
    private int counter = 0;
    private StringBuilder data = new StringBuilder();
    private final Object lock = new Object();
    
    public void increment() {
        synchronized (lock) {
            // 同步块保证原子性、可见性、有序性
            counter++;
        }
    }
    
    public void updateData(int threadId, int operation) {
        synchronized (lock) {
            // 所有对共享数据的访问都在同步块内
            data.append("[T").append(threadId).append("-").append(operation).append("]");
        }
    }
    
    public int getCounter() {
        synchronized (lock) {
            return counter;
        }
    }
    
    public void printData() {
        synchronized (lock) {
            System.out.println("数据长度: " + data.length() + " 字符");
            if (data.length() > 100) {
                System.out.println("数据样本: " + data.substring(0, 100) + "...");
            } else {
                System.out.println("完整数据: " + data.toString());
            }
        }
    }
}

/**
 * final语义示例
 */
class FinalSemanticExample {
    private final int finalValue;
    private final String finalString;
    private final int[] finalArray;
    
    // 普通字段作为对比
    private int normalValue;
    private String normalString;
    
    public FinalSemanticExample(int value, String str) {
        // final字段的初始化必须在构造函数中完成
        this.finalValue = value;
        this.finalString = str;
        this.finalArray = new int[]{1, 2, 3, value};
        
        // 普通字段可能在构造完成前对其他线程可见时还未初始化
        this.normalValue = value;
        this.normalString = str;
        
        // 模拟构造过程中的复杂操作
        for (int i = 0; i < 1000; i++) {
            Math.sqrt(i);
        }
    }
    
    public static void testFinalFieldSafety() {
        final AtomicInteger testCount = new AtomicInteger(0);
        final AtomicInteger successCount = new AtomicInteger(0);
        
        for (int test = 0; test < 10; test++) {
            // 创建对象的线程
            Thread creator = new Thread(() -> {
                for (int i = 0; i < 1000; i++) {
                    FinalSemanticExample obj = new FinalSemanticExample(i, "test-" + i);
                    // 对象创建后立即设置为静态变量（模拟发布到其他线程）
                    TestHolder.instance = obj;
                    LockSupport.parkNanos(1000); // 微小延迟
                }
            }, "Creator-" + test);
            
            // 读取对象的线程
            Thread reader = new Thread(() -> {
                for (int i = 0; i < 1000; i++) {
                    FinalSemanticExample obj = TestHolder.instance;
                    if (obj != null) {
                        testCount.incrementAndGet();
                        
                        // 检查final字段是否已经正确初始化
                        if (obj.finalValue >= 0 && 
                            obj.finalString != null && 
                            obj.finalArray != null && 
                            obj.finalArray.length == 4) {
                            successCount.incrementAndGet();
                        } else {
                            System.out.println("检测到final字段未正确初始化！");
                        }
                    }
                    LockSupport.parkNanos(1000); // 微小延迟
                }
            }, "Reader-" + test);
            
            creator.start();
            reader.start();
            
            try {
                creator.join();
                reader.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        System.out.println("final字段安全性测试结果:");
        System.out.println("总测试次数: " + testCount.get());
        System.out.println("成功次数: " + successCount.get());
        System.out.println("成功率: " + (successCount.get() * 100.0 / testCount.get()) + "%");
    }
    
    // 静态内部类用于测试
    static class TestHolder {
        static volatile FinalSemanticExample instance;
    }
}

/**
 * 对象构造过程示例
 */
class ObjectConstructionExample {
    private static volatile UnsafeObject unsafeInstance;
    private static volatile SafeObject safeInstance;
    
    public static void demonstrateConstructionRace() {
        System.out.println("测试不安全的对象构造：");
        
        // 测试不安全的对象构造
        Thread unsafeCreator = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                unsafeInstance = new UnsafeObject(i);
                LockSupport.parkNanos(1000);
            }
        }, "UnsafeCreator");
        
        Thread unsafeReader = new Thread(() -> {
            int nullCount = 0, incompleteCount = 0, completeCount = 0;
            
            for (int i = 0; i < 1000; i++) {
                UnsafeObject obj = unsafeInstance;
                if (obj == null) {
                    nullCount++;
                } else if (obj.isIncomplete()) {
                    incompleteCount++;
                    System.out.println("检测到不完整的对象！");
                } else {
                    completeCount++;
                }
                LockSupport.parkNanos(1000);
            }
            
            System.out.println("不安全对象测试结果 - null: " + nullCount + 
                             ", 不完整: " + incompleteCount + 
                             ", 完整: " + completeCount);
        }, "UnsafeReader");
        
        unsafeCreator.start();
        unsafeReader.start();
        
        try {
            unsafeCreator.join();
            unsafeReader.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("\n测试安全的对象构造（使用final）：");
        
        // 测试安全的对象构造
        Thread safeCreator = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                safeInstance = new SafeObject(i);
                LockSupport.parkNanos(1000);
            }
        }, "SafeCreator");
        
        Thread safeReader = new Thread(() -> {
            int nullCount = 0, completeCount = 0;
            
            for (int i = 0; i < 1000; i++) {
                SafeObject obj = safeInstance;
                if (obj == null) {
                    nullCount++;
                } else {
                    completeCount++;
                    // final字段保证对象一旦可见就是完整的
                }
                LockSupport.parkNanos(1000);
            }
            
            System.out.println("安全对象测试结果 - null: " + nullCount + 
                             ", 完整: " + completeCount);
        }, "SafeReader");
        
        safeCreator.start();
        safeReader.start();
        
        try {
            safeCreator.join();
            safeReader.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 不安全的对象（可能被其他线程看到部分构造的状态）
     */
    static class UnsafeObject {
        private int value1;
        private int value2;
        private String description;
        
        public UnsafeObject(int value) {
            this.value1 = value;
            
            // 模拟构造过程中的延迟
            for (int i = 0; i < 1000; i++) {
                Math.sqrt(i);
            }
            
            this.value2 = value * 2;
            this.description = "Object-" + value;
        }
        
        public boolean isIncomplete() {
            return value2 != value1 * 2 || description == null;
        }
    }
    
    /**
     * 安全的对象（使用final字段保证构造安全性）
     */
    static class SafeObject {
        private final int value1;
        private final int value2;
        private final String description;
        
        public SafeObject(int value) {
            this.value1 = value;
            
            // 即使构造过程中有延迟，final字段也能保证安全性
            for (int i = 0; i < 1000; i++) {
                Math.sqrt(i);
            }
            
            this.value2 = value * 2;
            this.description = "SafeObject-" + value;
        }
        
        public int getValue1() { return value1; }
        public int getValue2() { return value2; }
        public String getDescription() { return description; }
    }
}

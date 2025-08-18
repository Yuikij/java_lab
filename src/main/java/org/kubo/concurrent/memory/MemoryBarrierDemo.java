package org.kubo.concurrent.memory;

import sun.misc.Unsafe;
import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 内存屏障（Memory Barrier）演示
 * 
 * 内存屏障是一种CPU指令，用于控制特定条件下的重排序和内存可见性问题。
 * Java中的内存屏障主要通过以下方式实现：
 * 
 * 1. LoadLoad屏障：确保屏障前的读操作在屏障后的读操作之前完成
 * 2. StoreStore屏障：确保屏障前的写操作在屏障后的写操作之前完成  
 * 3. LoadStore屏障：确保屏障前的读操作在屏障后的写操作之前完成
 * 4. StoreLoad屏障：确保屏障前的写操作在屏障后的读操作之前完成
 * 
 * volatile关键字会插入内存屏障：
 * - volatile写之前插入StoreStore屏障
 * - volatile写之后插入StoreLoad屏障
 * - volatile读之前插入LoadLoad屏障
 * - volatile读之后插入LoadStore屏障
 * 
 * @author kubo
 */
public class MemoryBarrierDemo {
    
    // 获取Unsafe实例用于直接内存操作
    private static final Unsafe unsafe;
    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);
        } catch (Exception e) {
            throw new RuntimeException("无法获取Unsafe实例", e);
        }
    }
    
    public static void main(String[] args) {
        System.out.println("===============================================");
        System.out.println("           内存屏障（Memory Barrier）演示");
        System.out.println("===============================================\n");
        
        // 演示volatile的内存屏障效果
        demonstrateVolatileMemoryBarrier();
        sleep(1000);
        
        // 演示重排序问题和内存屏障的解决方案
        demonstrateReorderingAndBarrier();
        sleep(1000);
        
        // 演示StoreLoad屏障
        demonstrateStoreLoadBarrier();
        sleep(1000);
        
        // 演示写写屏障（StoreStore）
        demonstrateStoreStoreBarrier();
        sleep(1000);
        
        // 演示读读屏障（LoadLoad）
        demonstrateLoadLoadBarrier();
        sleep(1000);
        
        // 演示Unsafe的内存屏障方法
        demonstrateUnsafeMemoryBarriers();
    }
    
    /**
     * 演示volatile的内存屏障效果
     */
    private static void demonstrateVolatileMemoryBarrier() {
        System.out.println("🚧 volatile内存屏障效果演示");
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("volatile变量会在特定位置插入内存屏障，防止重排序");
        System.out.println();
        
        VolatileBarrierExample example = new VolatileBarrierExample();
        
        // 启动多个读线程
        Thread[] readers = new Thread[3];
        for (int i = 0; i < readers.length; i++) {
            final int readerId = i + 1;
            readers[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    if (example.checkDataConsistency()) {
                        // 如果volatile屏障工作正常，应该不会看到不一致的状态
                        continue;
                    } else {
                        System.out.println("读线程" + readerId + ": 检测到数据不一致！");
                        break;
                    }
                }
                System.out.println("读线程" + readerId + ": 完成1000次一致性检查");
            }, "Reader-" + readerId);
        }
        
        // 启动写线程
        Thread writer = new Thread(() -> {
            for (int i = 1; i <= 100; i++) {
                example.updateData(i);
                if (i % 20 == 0) {
                    System.out.println("写线程: 完成第" + i + "次数据更新");
                }
                sleep(10);
            }
        }, "Writer");
        
        // 启动所有线程
        for (Thread reader : readers) {
            reader.start();
        }
        writer.start();
        
        try {
            writer.join();
            for (Thread reader : readers) {
                reader.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("volatile内存屏障演示完成\n");
    }
    
    /**
     * 演示重排序问题和内存屏障的解决方案
     */
    private static void demonstrateReorderingAndBarrier() {
        System.out.println("🔄 重排序问题与内存屏障解决方案");
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("对比有无内存屏障时的重排序行为");
        System.out.println();
        
        System.out.println("测试1: 无内存屏障的重排序问题");
        testReorderingWithoutBarrier();
        
        sleep(1000);
        
        System.out.println("\n测试2: 使用内存屏障防止重排序");
        testReorderingWithBarrier();
        
        System.out.println("重排序与屏障演示完成\n");
    }
    
    /**
     * 测试无内存屏障时的重排序问题
     */
    private static void testReorderingWithoutBarrier() {
        ReorderingExample example = new ReorderingExample();
        AtomicInteger inconsistentCount = new AtomicInteger(0);
        int testRounds = 10000;
        
        for (int round = 0; round < testRounds; round++) {
            example.reset();
            CountDownLatch latch = new CountDownLatch(2);
            
            Thread thread1 = new Thread(() -> {
                example.writeWithoutBarrier();
                latch.countDown();
            });
            
            Thread thread2 = new Thread(() -> {
                if (!example.readWithoutBarrier()) {
                    inconsistentCount.incrementAndGet();
                }
                latch.countDown();
            });
            
            thread1.start();
            thread2.start();
            
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        System.out.println("  无屏障测试结果: " + testRounds + "轮测试中，" + 
                          inconsistentCount.get() + "次检测到重排序");
    }
    
    /**
     * 测试使用内存屏障防止重排序
     */
    private static void testReorderingWithBarrier() {
        ReorderingExample example = new ReorderingExample();
        AtomicInteger inconsistentCount = new AtomicInteger(0);
        int testRounds = 10000;
        
        for (int round = 0; round < testRounds; round++) {
            example.reset();
            CountDownLatch latch = new CountDownLatch(2);
            
            Thread thread1 = new Thread(() -> {
                example.writeWithBarrier();
                latch.countDown();
            });
            
            Thread thread2 = new Thread(() -> {
                if (!example.readWithBarrier()) {
                    inconsistentCount.incrementAndGet();
                }
                latch.countDown();
            });
            
            thread1.start();
            thread2.start();
            
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        System.out.println("  有屏障测试结果: " + testRounds + "轮测试中，" + 
                          inconsistentCount.get() + "次检测到重排序");
    }
    
    /**
     * 演示StoreLoad屏障
     */
    private static void demonstrateStoreLoadBarrier() {
        System.out.println("📝➡️📖 StoreLoad屏障演示");
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("StoreLoad屏障确保写操作在读操作之前完成");
        System.out.println();
        
        StoreLoadBarrierExample example = new StoreLoadBarrierExample();
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        int testCount = 1000;
        
        // 启动多个测试线程
        Thread[] threads = new Thread[4];
        for (int i = 0; i < threads.length; i++) {
            final int threadId = i + 1;
            threads[i] = new Thread(() -> {
                try {
                    startLatch.await(); // 等待同时开始
                    
                    for (int j = 0; j < testCount / threads.length; j++) {
                        if (example.testStoreLoadBarrier(threadId, j)) {
                            successCount.incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "StoreLoad-" + threadId);
        }
        
        for (Thread thread : threads) {
            thread.start();
        }
        
        sleep(100);
        startLatch.countDown(); // 开始测试
        
        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("StoreLoad屏障测试结果: " + testCount + "次测试中，" + 
                          successCount.get() + "次成功");
        System.out.println("StoreLoad屏障演示完成\n");
    }
    
    /**
     * 演示StoreStore屏障
     */
    private static void demonstrateStoreStoreBarrier() {
        System.out.println("📝➡️📝 StoreStore屏障演示");
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("StoreStore屏障确保前面的写操作在后面的写操作之前完成");
        System.out.println();
        
        StoreStoreBarrierExample example = new StoreStoreBarrierExample();
        
        Thread writerThread = new Thread(() -> {
            for (int i = 1; i <= 100; i++) {
                example.writeSequence(i);
                if (i % 25 == 0) {
                    System.out.println("写线程: 完成第" + i + "次写入序列");
                }
            }
        }, "Writer");
        
        Thread readerThread = new Thread(() -> {
            int checkCount = 0;
            int successCount = 0;
            
            while (checkCount < 1000) {
                if (example.checkWriteOrder()) {
                    successCount++;
                }
                checkCount++;
                
                if (checkCount % 200 == 0) {
                    System.out.println("读线程: 完成" + checkCount + "次检查，" + 
                                     "成功率 " + (successCount * 100 / checkCount) + "%");
                }
                
                sleep(1);
            }
        }, "Reader");
        
        writerThread.start();
        readerThread.start();
        
        try {
            writerThread.join();
            readerThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("StoreStore屏障演示完成\n");
    }
    
    /**
     * 演示LoadLoad屏障
     */
    private static void demonstrateLoadLoadBarrier() {
        System.out.println("📖➡️📖 LoadLoad屏障演示");
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("LoadLoad屏障确保前面的读操作在后面的读操作之前完成");
        System.out.println();
        
        LoadLoadBarrierExample example = new LoadLoadBarrierExample();
        
        // 写线程：持续更新数据
        Thread writerThread = new Thread(() -> {
            for (int i = 1; i <= 1000; i++) {
                example.updateData(i);
                sleep(10);
            }
        }, "Writer");
        
        // 读线程：测试读取顺序
        Thread readerThread = new Thread(() -> {
            int testCount = 0;
            int consistentCount = 0;
            
            while (testCount < 500) {
                if (example.testReadOrder()) {
                    consistentCount++;
                }
                testCount++;
                
                if (testCount % 100 == 0) {
                    System.out.println("读线程: 完成" + testCount + "次测试，" + 
                                     "一致性 " + (consistentCount * 100 / testCount) + "%");
                }
                
                sleep(20);
            }
        }, "Reader");
        
        writerThread.start();
        readerThread.start();
        
        try {
            writerThread.join();
            readerThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("LoadLoad屏障演示完成\n");
    }
    
    /**
     * 演示Unsafe的内存屏障方法
     */
    private static void demonstrateUnsafeMemoryBarriers() {
        System.out.println("⚠️ Unsafe内存屏障方法演示");
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("使用Unsafe类的内存屏障方法直接控制内存可见性");
        System.out.println("注意: 这些方法在Java 9+中已被限制访问");
        System.out.println();
        
        UnsafeBarrierExample example = new UnsafeBarrierExample();
        
        // 测试线程
        Thread[] threads = new Thread[2];
        
        threads[0] = new Thread(() -> {
            for (int i = 1; i <= 50; i++) {
                example.writeWithUnsafeBarrier(i);
                System.out.println("写线程: 使用Unsafe屏障写入 " + i);
                sleep(100);
            }
        }, "UnsafeWriter");
        
        threads[1] = new Thread(() -> {
            int lastValue = 0;
            for (int i = 0; i < 50; i++) {
                int currentValue = example.readWithUnsafeBarrier();
                if (currentValue != lastValue) {
                    System.out.println("读线程: 使用Unsafe屏障读取 " + currentValue);
                    lastValue = currentValue;
                }
                sleep(100);
            }
        }, "UnsafeReader");
        
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
        
        System.out.println("Unsafe内存屏障演示完成\n");
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
 * volatile屏障示例
 */
class VolatileBarrierExample {
    private int data1 = 0;
    private int data2 = 0;
    private volatile boolean ready = false;
    
    public void updateData(int value) {
        data1 = value;        // 普通写操作
        data2 = value * 2;    // 普通写操作
        
        ready = true;         // volatile写操作，插入屏障
        // 在volatile写之前插入StoreStore屏障，确保data1、data2的写入不会重排序到ready写入之后
        // 在volatile写之后插入StoreLoad屏障，防止后续读操作重排序到写操作之前
    }
    
    public boolean checkDataConsistency() {
        if (ready) {          // volatile读操作，插入屏障
            // 在volatile读之前插入LoadLoad屏障，确保后续读操作不会重排序到ready读取之前
            // 在volatile读之后插入LoadStore屏障，防止后续写操作重排序到读操作之前
            
            return data2 == data1 * 2;  // 如果屏障工作正常，这应该总是true
        }
        return true; // ready为false时认为正常
    }
}

/**
 * 重排序示例
 */
class ReorderingExample {
    private int data = 0;
    private boolean flag = false;
    private volatile int volatileData = 0;
    private volatile boolean volatileFlag = false;
    
    public void reset() {
        data = 0;
        flag = false;
        volatileData = 0;
        volatileFlag = false;
    }
    
    // 无内存屏障的写操作（可能发生重排序）
    public void writeWithoutBarrier() {
        data = 42;       // 写入数据
        flag = true;     // 设置标志
        // 这两个操作可能被重排序
    }
    
    // 无内存屏障的读操作
    public boolean readWithoutBarrier() {
        if (flag) {      // 检查标志
            return data == 42;  // 检查数据
        }
        return true;
    }
    
    // 使用volatile提供内存屏障的写操作
    public void writeWithBarrier() {
        volatileData = 42;        // volatile写，提供内存屏障
        volatileFlag = true;      // volatile写，提供内存屏障
    }
    
    // 使用volatile提供内存屏障的读操作
    public boolean readWithBarrier() {
        if (volatileFlag) {       // volatile读，提供内存屏障
            return volatileData == 42;  // volatile读，提供内存屏障
        }
        return true;
    }
}

/**
 * StoreLoad屏障示例
 */
class StoreLoadBarrierExample {
    private volatile int sharedValue = 0;
    private volatile boolean testFlag = false;
    
    public boolean testStoreLoadBarrier(int threadId, int testId) {
        int writeValue = threadId * 1000 + testId;
        
        // 写操作
        sharedValue = writeValue;    // volatile写，插入StoreLoad屏障
        
        // 读操作（由于StoreLoad屏障，应该能读取到刚才写入的值）
        boolean flag = testFlag;     // volatile读
        int readValue = sharedValue; // volatile读
        
        return readValue == writeValue;
    }
}

/**
 * StoreStore屏障示例
 */
class StoreStoreBarrierExample {
    private int sequence1 = 0;
    private int sequence2 = 0;
    private volatile boolean updated = false;
    
    public void writeSequence(int value) {
        sequence1 = value;           // 第一个写操作
        sequence2 = value + 100;     // 第二个写操作
        
        updated = true;              // volatile写，插入StoreStore屏障
        // StoreStore屏障确保sequence1和sequence2的写入在updated写入之前完成
    }
    
    public boolean checkWriteOrder() {
        if (updated) {
            int s1 = sequence1;
            int s2 = sequence2;
            
            // 如果StoreStore屏障工作正常，s2应该总是等于s1+100
            return s2 == s1 + 100;
        }
        return true;
    }
}

/**
 * LoadLoad屏障示例
 */
class LoadLoadBarrierExample {
    private volatile int value1 = 0;
    private volatile int value2 = 0;
    
    public void updateData(int newValue) {
        value1 = newValue;
        value2 = newValue * 10;
    }
    
    public boolean testReadOrder() {
        int v1 = value1;     // 第一个volatile读，插入LoadLoad屏障
        int v2 = value2;     // 第二个volatile读
        
        // LoadLoad屏障确保v1的读取在v2的读取之前完成
        // 如果屏障工作正常，要么v2 == v1 * 10，要么两者都是旧值
        return v2 == v1 * 10 || (v1 == 0 && v2 == 0);
    }
}

/**
 * Unsafe屏障示例
 */
class UnsafeBarrierExample {
    private static final Unsafe unsafe = getUnsafe();
    private int data = 0;
    
    private static Unsafe getUnsafe() {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Unsafe) field.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public void writeWithUnsafeBarrier(int value) {
        data = value;
        
        // 使用Unsafe插入内存屏障
        unsafe.storeFence();    // StoreStore + StoreLoad屏障
        unsafe.fullFence();     // 全屏障（LoadLoad + LoadStore + StoreLoad + StoreStore）
    }
    
    public int readWithUnsafeBarrier() {
        unsafe.loadFence();     // LoadLoad + LoadStore屏障
        
        int result = data;
        
        unsafe.loadFence();     // 再次插入读屏障
        
        return result;
    }
}

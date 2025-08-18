package org.kubo.concurrent.memory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadLocalRandom;

/**
 * CPU缓存模型演示
 * 
 * 现代CPU采用多级缓存架构：
 * L1缓存：最快，通常32-64KB，每个核心独有
 * L2缓存：中等速度，通常256KB-1MB，每个核心独有或共享
 * L3缓存：较慢，通常几MB到几十MB，多核心共享
 * 主内存：最慢，几GB到几百GB
 * 
 * 缓存一致性协议（如MESI）确保多核心间的数据一致性：
 * M (Modified): 缓存行被修改，与主内存不一致
 * E (Exclusive): 缓存行独占，与主内存一致
 * S (Shared): 缓存行被多个CPU共享，与主内存一致
 * I (Invalid): 缓存行无效
 * 
 * @author kubo
 */
public class CPUCacheDemo {
    
    private static final int ARRAY_SIZE = 64 * 1024 * 1024; // 64MB数组
    private static final int ITERATIONS = 1000;
    
    public static void main(String[] args) {
        System.out.println("===============================================");
        System.out.println("             CPU缓存模型演示");
        System.out.println("===============================================\n");
        
        // 演示缓存行大小的影响
        demonstrateCacheLineSize();
        sleep(1000);
        
        // 演示局部性原理
        demonstrateLocalityPrinciple();
        sleep(1000);
        
        // 演示缓存一致性开销
        demonstrateCacheCoherence();
        sleep(1000);
        
        // 演示预取器的影响
        demonstratePrefetching();
        sleep(1000);
        
        // 演示多级缓存
        demonstrateMultiLevelCache();
        sleep(1000);
        
        // 演示缓存友好的数据结构
        demonstrateCacheFriendlyDataStructures();
    }
    
    /**
     * 演示缓存行大小的影响
     */
    private static void demonstrateCacheLineSize() {
        System.out.println("📏 缓存行大小影响演示");
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("测试不同步长访问对性能的影响");
        System.out.println();
        
        int[] array = new int[ARRAY_SIZE / 4]; // 16M integers
        
        // 测试不同的步长
        int[] strides = {1, 2, 4, 8, 16, 32, 64};
        
        for (int stride : strides) {
            long startTime = System.nanoTime();
            
            // 按步长访问数组
            long sum = 0;
            for (int i = 0; i < array.length; i += stride) {
                sum += array[i];
            }
            
            long endTime = System.nanoTime();
            double duration = (endTime - startTime) / 1_000_000.0;
            
            int accessCount = array.length / stride;
            double throughput = accessCount / (duration / 1000);
            
            System.out.printf("步长 %2d: 访问 %8d 个元素, 用时 %6.2f ms, 吞吐量 %8.0f ops/s%n",
                            stride, accessCount, duration, throughput);
            
            // 防止编译器优化
            if (sum == Long.MAX_VALUE) {
                System.out.println("Impossible");
            }
        }
        
        System.out.println("\n观察: 步长越大，缓存命中率越低，性能越差");
        System.out.println();
    }
    
    /**
     * 演示局部性原理
     */
    private static void demonstrateLocalityPrinciple() {
        System.out.println("🎯 局部性原理演示");
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("对比顺序访问和随机访问的性能差异");
        System.out.println();
        
        int arraySize = 16 * 1024 * 1024; // 16M integers = 64MB
        int[] array = new int[arraySize];
        
        // 初始化数组
        for (int i = 0; i < arraySize; i++) {
            array[i] = i;
        }
        
        // 测试顺序访问（时间局部性 + 空间局部性）
        long startTime = System.nanoTime();
        long sum = 0;
        for (int iteration = 0; iteration < 10; iteration++) {
            for (int i = 0; i < arraySize; i++) {
                sum += array[i];
            }
        }
        long sequentialTime = System.nanoTime() - startTime;
        
        // 测试随机访问（破坏局部性）
        int[] randomIndices = generateRandomIndices(arraySize, arraySize * 10);
        
        startTime = System.nanoTime();
        sum = 0;
        for (int index : randomIndices) {
            sum += array[index];
        }
        long randomTime = System.nanoTime() - startTime;
        
        System.out.printf("顺序访问: %.2f ms%n", sequentialTime / 1_000_000.0);
        System.out.printf("随机访问: %.2f ms%n", randomTime / 1_000_000.0);
        System.out.printf("性能差异: %.1fx%n", (double) randomTime / sequentialTime);
        
        // 防止编译器优化
        if (sum == Long.MAX_VALUE) {
            System.out.println("Impossible");
        }
        
        System.out.println("\n观察: 顺序访问利用了缓存的局部性，性能显著优于随机访问");
        System.out.println();
    }
    
    /**
     * 演示缓存一致性开销
     */
    private static void demonstrateCacheCoherence() {
        System.out.println("🔄 缓存一致性开销演示");
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("测试多线程访问共享数据时的缓存一致性开销");
        System.out.println();
        
        CacheCoherenceTest test = new CacheCoherenceTest();
        
        // 测试单线程性能（基准）
        long singleThreadTime = test.runSingleThreadTest();
        
        // 测试多线程访问不同数据（无缓存冲突）
        long multiThreadNoContentionTime = test.runMultiThreadTest(false);
        
        // 测试多线程访问相同数据（缓存冲突）
        long multiThreadContentionTime = test.runMultiThreadTest(true);
        
        System.out.printf("单线程基准: %.2f ms%n", singleThreadTime / 1_000_000.0);
        System.out.printf("多线程无冲突: %.2f ms (%.1fx)%n", 
                        multiThreadNoContentionTime / 1_000_000.0,
                        (double) multiThreadNoContentionTime / singleThreadTime);
        System.out.printf("多线程有冲突: %.2f ms (%.1fx)%n", 
                        multiThreadContentionTime / 1_000_000.0,
                        (double) multiThreadContentionTime / singleThreadTime);
        
        System.out.println("\n观察: 缓存冲突显著影响多线程性能");
        System.out.println();
    }
    
    /**
     * 演示预取器的影响
     */
    private static void demonstratePrefetching() {
        System.out.println("🔮 预取器影响演示");
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("测试可预测访问模式和不可预测访问模式的性能差异");
        System.out.println();
        
        int arraySize = 8 * 1024 * 1024; // 8M integers
        int[] array = new int[arraySize];
        
        // 初始化数组
        for (int i = 0; i < arraySize; i++) {
            array[i] = ThreadLocalRandom.current().nextInt();
        }
        
        // 测试可预测的模式（步长固定）
        long startTime = System.nanoTime();
        long sum = 0;
        for (int iteration = 0; iteration < 100; iteration++) {
            for (int i = 0; i < arraySize; i += 8) { // 固定步长
                sum += array[i];
            }
        }
        long predictableTime = System.nanoTime() - startTime;
        
        // 测试不可预测的模式（步长变化）
        startTime = System.nanoTime();
        sum = 0;
        for (int iteration = 0; iteration < 100; iteration++) {
            int index = 0;
            while (index < arraySize) {
                sum += array[index];
                // 步长在4-12之间变化，破坏预取器的预测
                index += 4 + (iteration + index) % 9;
            }
        }
        long unpredictableTime = System.nanoTime() - startTime;
        
        System.out.printf("可预测模式: %.2f ms%n", predictableTime / 1_000_000.0);
        System.out.printf("不可预测模式: %.2f ms%n", unpredictableTime / 1_000_000.0);
        System.out.printf("性能差异: %.1fx%n", (double) unpredictableTime / predictableTime);
        
        // 防止编译器优化
        if (sum == Long.MAX_VALUE) {
            System.out.println("Impossible");
        }
        
        System.out.println("\n观察: 预取器能显著提升可预测访问模式的性能");
        System.out.println();
    }
    
    /**
     * 演示多级缓存
     */
    private static void demonstrateMultiLevelCache() {
        System.out.println("🏗️ 多级缓存演示");
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("测试不同数据大小对应的缓存级别");
        System.out.println();
        
        // 测试不同大小的数组，观察缓存层级的影响
        int[] sizes = {
            32 * 1024,      // 32KB - 适合L1缓存
            256 * 1024,     // 256KB - 适合L2缓存
            8 * 1024 * 1024, // 8MB - 适合L3缓存
            64 * 1024 * 1024 // 64MB - 超出L3缓存
        };
        
        String[] cacheNames = {"L1缓存", "L2缓存", "L3缓存", "主内存"};
        
        for (int i = 0; i < sizes.length; i++) {
            int size = sizes[i] / 4; // 转换为int数量
            int[] array = new int[size];
            
            // 初始化数组
            for (int j = 0; j < size; j++) {
                array[j] = j;
            }
            
            long startTime = System.nanoTime();
            
            // 多次遍历数组
            long sum = 0;
            for (int iteration = 0; iteration < 1000; iteration++) {
                for (int j = 0; j < size; j++) {
                    sum += array[j];
                }
            }
            
            long endTime = System.nanoTime();
            double duration = (endTime - startTime) / 1_000_000.0;
            double throughput = (size * 1000L) / (duration / 1000);
            
            System.out.printf("%s级别 (%6dKB): 用时 %8.2f ms, 吞吐量 %10.0f ops/s%n",
                            cacheNames[i], sizes[i] / 1024, duration, throughput);
            
            // 防止编译器优化
            if (sum == Long.MAX_VALUE) {
                System.out.println("Impossible");
            }
        }
        
        System.out.println("\n观察: 数据大小超出缓存容量时，性能显著下降");
        System.out.println();
    }
    
    /**
     * 演示缓存友好的数据结构
     */
    private static void demonstrateCacheFriendlyDataStructures() {
        System.out.println("💡 缓存友好数据结构演示");
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("对比数组(AoS)和结构体数组(SoA)的性能");
        System.out.println();
        
        int elementCount = 1024 * 1024; // 1M elements
        
        // Array of Structures (AoS) - 缓存不友好
        Point[] aos = new Point[elementCount];
        for (int i = 0; i < elementCount; i++) {
            aos[i] = new Point(i, i + 1, i + 2);
        }
        
        // Structure of Arrays (SoA) - 缓存友好
        PointSoA soa = new PointSoA(elementCount);
        for (int i = 0; i < elementCount; i++) {
            soa.setPoint(i, i, i + 1, i + 2);
        }
        
        // 测试AoS访问模式（只访问x坐标）
        long startTime = System.nanoTime();
        double sum = 0;
        for (int iteration = 0; iteration < 100; iteration++) {
            for (Point point : aos) {
                sum += point.x; // 每次都要加载整个Point对象
            }
        }
        long aosTime = System.nanoTime() - startTime;
        
        // 测试SoA访问模式（只访问x坐标）
        startTime = System.nanoTime();
        sum = 0;
        for (int iteration = 0; iteration < 100; iteration++) {
            for (int i = 0; i < elementCount; i++) {
                sum += soa.x[i]; // 只加载x数组，缓存友好
            }
        }
        long soaTime = System.nanoTime() - startTime;
        
        System.out.printf("AoS (对象数组): %.2f ms%n", aosTime / 1_000_000.0);
        System.out.printf("SoA (数组结构): %.2f ms%n", soaTime / 1_000_000.0);
        System.out.printf("性能提升: %.1fx%n", (double) aosTime / soaTime);
        
        // 防止编译器优化
        if (sum == Double.MAX_VALUE) {
            System.out.println("Impossible");
        }
        
        System.out.println("\n观察: 根据访问模式选择合适的数据结构能显著提升性能");
        System.out.println();
    }
    
    private static int[] generateRandomIndices(int arraySize, int count) {
        int[] indices = new int[count];
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < count; i++) {
            indices[i] = random.nextInt(arraySize);
        }
        return indices;
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
 * 缓存一致性测试类
 */
class CacheCoherenceTest {
    private static final int OPERATIONS = 10_000_000;
    private volatile long[] sharedCounters = new long[8]; // 共享计数器
    private volatile long[][] separateCounters = new long[8][8]; // 独立计数器
    
    public long runSingleThreadTest() {
        long startTime = System.nanoTime();
        
        long counter = 0;
        for (int i = 0; i < OPERATIONS; i++) {
            counter++;
        }
        
        // 防止编译器优化
        if (counter != OPERATIONS) {
            throw new RuntimeException("Unexpected result");
        }
        
        return System.nanoTime() - startTime;
    }
    
    public long runMultiThreadTest(boolean contention) {
        final int threadCount = 4;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        
        long startTime = System.nanoTime();
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            Thread thread = new Thread(() -> {
                try {
                    startLatch.await();
                    
                    if (contention) {
                        // 所有线程访问同一个缓存行，造成缓存冲突
                        for (int j = 0; j < OPERATIONS / threadCount; j++) {
                            sharedCounters[0]++; // 所有线程都访问索引0
                        }
                    } else {
                        // 每个线程访问不同的缓存行，避免冲突
                        for (int j = 0; j < OPERATIONS / threadCount; j++) {
                            separateCounters[threadId][0]++; // 每个线程访问自己的数组
                        }
                    }
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            }, "CacheTest-" + threadId);
            thread.start();
        }
        
        startLatch.countDown();
        
        try {
            endLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return System.nanoTime() - startTime;
    }
}

/**
 * Array of Structures (AoS) - 传统的面向对象方式
 */
class Point {
    double x, y, z;
    
    Point(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}

/**
 * Structure of Arrays (SoA) - 缓存友好的方式
 */
class PointSoA {
    double[] x, y, z;
    
    PointSoA(int size) {
        x = new double[size];
        y = new double[size];
        z = new double[size];
    }
    
    void setPoint(int index, double x, double y, double z) {
        this.x[index] = x;
        this.y[index] = y;
        this.z[index] = z;
    }
}

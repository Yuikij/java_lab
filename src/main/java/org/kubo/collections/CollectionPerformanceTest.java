package org.kubo.collections;

import java.util.*;
import java.util.concurrent.*;

/**
 * 集合性能综合测试类
 * 对比不同集合在各种操作下的性能表现
 */
@SuppressWarnings("unused")
public class CollectionPerformanceTest {

    private static final int SMALL_SIZE = 1000;
    private static final int MEDIUM_SIZE = 10000;
    private static final int LARGE_SIZE = 100000;
    
    /**
     * 运行集合性能测试
     */
    public static void run() {
        System.out.println("\n==== 集合性能综合测试 ====");
        
        // List性能测试
        listPerformanceTest();
        
        // Set性能测试
        setPerformanceTest();
        
        // Map性能测试
        mapPerformanceTest();
        
        // Queue性能测试
        queuePerformanceTest();
        
        // 内存使用测试
        memoryUsageTest();
        
        // 并发性能测试
        concurrentPerformanceTest();
        
        // 总结和建议
        performanceSummary();
    }

    /**
     * List性能测试
     */
    private static void listPerformanceTest() {
        System.out.println("\n--- List性能测试 ---");
        
        int[] sizes = {SMALL_SIZE, MEDIUM_SIZE, LARGE_SIZE};
        
        for (int size : sizes) {
            System.out.printf("\n数据规模: %,d 元素%n", size);
            
            // 准备测试数据
            List<Integer> testData = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                testData.add(i);
            }
            Collections.shuffle(testData);
            
            // 测试添加性能
            testListAddPerformance(testData, size);
            
            // 测试随机访问性能
            testListRandomAccessPerformance(testData, size);
            
            // 测试插入性能
            testListInsertPerformance(size / 10);
            
            // 测试删除性能
            testListRemovePerformance(size / 10);
        }
    }

    private static void testListAddPerformance(List<Integer> testData, int size) {
        System.out.println("  添加操作性能:");
        
        long startTime = System.nanoTime();
        List<Integer> arrayList = new ArrayList<>();
        for (Integer item : testData) {
            arrayList.add(item);
        }
        long arrayListTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        List<Integer> linkedList = new LinkedList<>();
        for (Integer item : testData) {
            linkedList.add(item);
        }
        long linkedListTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        List<Integer> vector = new Vector<>();
        for (Integer item : testData) {
            vector.add(item);
        }
        long vectorTime = System.nanoTime() - startTime;
        
        System.out.printf("    ArrayList: %,d ns%n", arrayListTime);
        System.out.printf("    LinkedList: %,d ns (%.1fx)%n", 
                         linkedListTime, (double)linkedListTime / arrayListTime);
        System.out.printf("    Vector: %,d ns (%.1fx)%n", 
                         vectorTime, (double)vectorTime / arrayListTime);
    }

    private static void testListRandomAccessPerformance(List<Integer> testData, int size) {
        System.out.println("  随机访问性能:");
        
        List<Integer> arrayList = new ArrayList<>(testData);
        List<Integer> linkedList = new LinkedList<>(testData);
        List<Integer> vector = new Vector<>(testData);
        
        Random random = new Random(12345);
        int accessCount = Math.min(size / 10, 10000);
        
        long startTime = System.nanoTime();
        long sum = 0;
        for (int i = 0; i < accessCount; i++) {
            sum += arrayList.get(random.nextInt(size));
        }
        long arrayListTime = System.nanoTime() - startTime;
        
        random = new Random(12345);
        startTime = System.nanoTime();
        sum = 0;
        for (int i = 0; i < accessCount; i++) {
            sum += linkedList.get(random.nextInt(size));
        }
        long linkedListTime = System.nanoTime() - startTime;
        
        random = new Random(12345);
        startTime = System.nanoTime();
        sum = 0;
        for (int i = 0; i < accessCount; i++) {
            sum += vector.get(random.nextInt(size));
        }
        long vectorTime = System.nanoTime() - startTime;
        
        System.out.printf("    ArrayList: %,d ns (%,d 次访问)%n", arrayListTime, accessCount);
        System.out.printf("    LinkedList: %,d ns (%.1fx slower)%n", 
                         linkedListTime, (double)linkedListTime / arrayListTime);
        System.out.printf("    Vector: %,d ns (%.1fx)%n", 
                         vectorTime, (double)vectorTime / arrayListTime);
    }

    private static void testListInsertPerformance(int insertCount) {
        System.out.println("  中间插入性能:");
        
        List<Integer> arrayList = new ArrayList<>();
        List<Integer> linkedList = new LinkedList<>();
        
        // 预填充一些数据
        for (int i = 0; i < insertCount; i++) {
            arrayList.add(i);
            linkedList.add(i);
        }
        
        long startTime = System.nanoTime();
        for (int i = 0; i < insertCount; i++) {
            arrayList.add(arrayList.size() / 2, i);
        }
        long arrayListTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        for (int i = 0; i < insertCount; i++) {
            linkedList.add(linkedList.size() / 2, i);
        }
        long linkedListTime = System.nanoTime() - startTime;
        
        System.out.printf("    ArrayList中间插入: %,d ns (%,d 次)%n", arrayListTime, insertCount);
        System.out.printf("    LinkedList中间插入: %,d ns (%.1fx)%n", 
                         linkedListTime, (double)linkedListTime / arrayListTime);
    }

    private static void testListRemovePerformance(int removeCount) {
        System.out.println("  中间删除性能:");
        
        List<Integer> arrayList = new ArrayList<>();
        List<Integer> linkedList = new LinkedList<>();
        
        // 预填充数据
        for (int i = 0; i < removeCount * 2; i++) {
            arrayList.add(i);
            linkedList.add(i);
        }
        
        long startTime = System.nanoTime();
        for (int i = 0; i < removeCount; i++) {
            arrayList.remove(arrayList.size() / 2);
        }
        long arrayListTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        for (int i = 0; i < removeCount; i++) {
            linkedList.remove(linkedList.size() / 2);
        }
        long linkedListTime = System.nanoTime() - startTime;
        
        System.out.printf("    ArrayList中间删除: %,d ns (%,d 次)%n", arrayListTime, removeCount);
        System.out.printf("    LinkedList中间删除: %,d ns (%.1fx)%n", 
                         linkedListTime, (double)linkedListTime / arrayListTime);
    }

    /**
     * Set性能测试
     */
    private static void setPerformanceTest() {
        System.out.println("\n--- Set性能测试 ---");
        
        int testSize = MEDIUM_SIZE;
        System.out.printf("数据规模: %,d 元素%n", testSize);
        
        // 准备测试数据
        List<String> testData = new ArrayList<>();
        for (int i = 0; i < testSize; i++) {
            testData.add("String_" + i);
        }
        Collections.shuffle(testData);
        
        // 添加性能测试
        System.out.println("  添加操作性能:");
        
        long startTime = System.nanoTime();
        Set<String> hashSet = new HashSet<>();
        for (String item : testData) {
            hashSet.add(item);
        }
        long hashSetTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        Set<String> linkedHashSet = new LinkedHashSet<>();
        for (String item : testData) {
            linkedHashSet.add(item);
        }
        long linkedHashSetTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        Set<String> treeSet = new TreeSet<>();
        for (String item : testData) {
            treeSet.add(item);
        }
        long treeSetTime = System.nanoTime() - startTime;
        
        System.out.printf("    HashSet: %,d ns%n", hashSetTime);
        System.out.printf("    LinkedHashSet: %,d ns (%.1fx)%n", 
                         linkedHashSetTime, (double)linkedHashSetTime / hashSetTime);
        System.out.printf("    TreeSet: %,d ns (%.1fx)%n", 
                         treeSetTime, (double)treeSetTime / hashSetTime);
        
        // 查找性能测试
        System.out.println("  查找操作性能:");
        
        int lookupCount = testSize / 2;
        Random random = new Random(12345);
        
        startTime = System.nanoTime();
        int found = 0;
        for (int i = 0; i < lookupCount; i++) {
            if (hashSet.contains("String_" + random.nextInt(testSize))) found++;
        }
        long hashSetLookupTime = System.nanoTime() - startTime;
        
        random = new Random(12345);
        startTime = System.nanoTime();
        found = 0;
        for (int i = 0; i < lookupCount; i++) {
            if (linkedHashSet.contains("String_" + random.nextInt(testSize))) found++;
        }
        long linkedHashSetLookupTime = System.nanoTime() - startTime;
        
        random = new Random(12345);
        startTime = System.nanoTime();
        found = 0;
        for (int i = 0; i < lookupCount; i++) {
            if (treeSet.contains("String_" + random.nextInt(testSize))) found++;
        }
        long treeSetLookupTime = System.nanoTime() - startTime;
        
        System.out.printf("    HashSet查找: %,d ns (%,d 次)%n", hashSetLookupTime, lookupCount);
        System.out.printf("    LinkedHashSet查找: %,d ns (%.1fx)%n", 
                         linkedHashSetLookupTime, (double)linkedHashSetLookupTime / hashSetLookupTime);
        System.out.printf("    TreeSet查找: %,d ns (%.1fx)%n", 
                         treeSetLookupTime, (double)treeSetLookupTime / hashSetLookupTime);
    }

    /**
     * Map性能测试
     */
    private static void mapPerformanceTest() {
        System.out.println("\n--- Map性能测试 ---");
        
        int testSize = MEDIUM_SIZE;
        System.out.printf("数据规模: %,d 元素%n", testSize);
        
        // 准备测试数据
        List<String> keys = new ArrayList<>();
        List<String> values = new ArrayList<>();
        for (int i = 0; i < testSize; i++) {
            keys.add("Key_" + i);
            values.add("Value_" + i);
        }
        Collections.shuffle(keys);
        
        // 插入性能测试
        System.out.println("  插入操作性能:");
        
        long startTime = System.nanoTime();
        Map<String, String> hashMap = new HashMap<>();
        for (int i = 0; i < testSize; i++) {
            hashMap.put(keys.get(i), values.get(i));
        }
        long hashMapTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        Map<String, String> linkedHashMap = new LinkedHashMap<>();
        for (int i = 0; i < testSize; i++) {
            linkedHashMap.put(keys.get(i), values.get(i));
        }
        long linkedHashMapTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        Map<String, String> treeMap = new TreeMap<>();
        for (int i = 0; i < testSize; i++) {
            treeMap.put(keys.get(i), values.get(i));
        }
        long treeMapTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        Map<String, String> hashtable = new Hashtable<>();
        for (int i = 0; i < testSize; i++) {
            hashtable.put(keys.get(i), values.get(i));
        }
        long hashtableTime = System.nanoTime() - startTime;
        
        System.out.printf("    HashMap: %,d ns%n", hashMapTime);
        System.out.printf("    LinkedHashMap: %,d ns (%.1fx)%n", 
                         linkedHashMapTime, (double)linkedHashMapTime / hashMapTime);
        System.out.printf("    TreeMap: %,d ns (%.1fx)%n", 
                         treeMapTime, (double)treeMapTime / hashMapTime);
        System.out.printf("    Hashtable: %,d ns (%.1fx)%n", 
                         hashtableTime, (double)hashtableTime / hashMapTime);
        
        // 查找性能测试
        System.out.println("  查找操作性能:");
        
        int lookupCount = testSize / 2;
        Random random = new Random(12345);
        
        startTime = System.nanoTime();
        for (int i = 0; i < lookupCount; i++) {
            hashMap.get("Key_" + random.nextInt(testSize));
        }
        long hashMapLookupTime = System.nanoTime() - startTime;
        
        random = new Random(12345);
        startTime = System.nanoTime();
        for (int i = 0; i < lookupCount; i++) {
            linkedHashMap.get("Key_" + random.nextInt(testSize));
        }
        long linkedHashMapLookupTime = System.nanoTime() - startTime;
        
        random = new Random(12345);
        startTime = System.nanoTime();
        for (int i = 0; i < lookupCount; i++) {
            treeMap.get("Key_" + random.nextInt(testSize));
        }
        long treeMapLookupTime = System.nanoTime() - startTime;
        
        System.out.printf("    HashMap查找: %,d ns (%,d 次)%n", hashMapLookupTime, lookupCount);
        System.out.printf("    LinkedHashMap查找: %,d ns (%.1fx)%n", 
                         linkedHashMapLookupTime, (double)linkedHashMapLookupTime / hashMapLookupTime);
        System.out.printf("    TreeMap查找: %,d ns (%.1fx)%n", 
                         treeMapLookupTime, (double)treeMapLookupTime / hashMapLookupTime);
    }

    /**
     * Queue性能测试
     */
    private static void queuePerformanceTest() {
        System.out.println("\n--- Queue性能测试 ---");
        
        int testSize = MEDIUM_SIZE;
        System.out.printf("数据规模: %,d 元素%n", testSize);
        
        // 准备测试数据
        List<String> testData = new ArrayList<>();
        for (int i = 0; i < testSize; i++) {
            testData.add("Item_" + i);
        }
        
        // 入队性能测试
        System.out.println("  入队操作性能:");
        
        long startTime = System.nanoTime();
        Queue<String> linkedListQueue = new LinkedList<>();
        for (String item : testData) {
            linkedListQueue.offer(item);
        }
        long linkedListTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        Queue<String> arrayDeque = new ArrayDeque<>();
        for (String item : testData) {
            arrayDeque.offer(item);
        }
        long arrayDequeTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        Queue<String> priorityQueue = new PriorityQueue<>();
        for (String item : testData) {
            priorityQueue.offer(item);
        }
        long priorityQueueTime = System.nanoTime() - startTime;
        
        System.out.printf("    LinkedList: %,d ns%n", linkedListTime);
        System.out.printf("    ArrayDeque: %,d ns (%.1fx faster)%n", 
                         arrayDequeTime, (double)linkedListTime / arrayDequeTime);
        System.out.printf("    PriorityQueue: %,d ns (%.1fx)%n", 
                         priorityQueueTime, (double)priorityQueueTime / linkedListTime);
        
        // 出队性能测试
        System.out.println("  出队操作性能:");
        
        startTime = System.nanoTime();
        while (!linkedListQueue.isEmpty()) {
            linkedListQueue.poll();
        }
        long linkedListDequeueTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        while (!arrayDeque.isEmpty()) {
            arrayDeque.poll();
        }
        long arrayDequeDequeueTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        while (!priorityQueue.isEmpty()) {
            priorityQueue.poll();
        }
        long priorityQueueDequeueTime = System.nanoTime() - startTime;
        
        System.out.printf("    LinkedList出队: %,d ns%n", linkedListDequeueTime);
        System.out.printf("    ArrayDeque出队: %,d ns (%.1fx faster)%n", 
                         arrayDequeDequeueTime, (double)linkedListDequeueTime / arrayDequeDequeueTime);
        System.out.printf("    PriorityQueue出队: %,d ns (%.1fx)%n", 
                         priorityQueueDequeueTime, (double)priorityQueueDequeueTime / linkedListDequeueTime);
    }

    /**
     * 内存使用测试
     */
    private static void memoryUsageTest() {
        System.out.println("\n--- 内存使用测试 ---");
        
        int testSize = LARGE_SIZE;
        System.out.printf("测试数据规模: %,d 元素%n", testSize);
        
        Runtime runtime = Runtime.getRuntime();
        
        // ArrayList内存测试
        System.gc();
        long beforeMemory = runtime.totalMemory() - runtime.freeMemory();
        List<Integer> arrayList = new ArrayList<>();
        for (int i = 0; i < testSize; i++) {
            arrayList.add(i);
        }
        long afterMemory = runtime.totalMemory() - runtime.freeMemory();
        long arrayListMemory = afterMemory - beforeMemory;
        
        // LinkedList内存测试
        arrayList = null; // 释放引用
        System.gc();
        beforeMemory = runtime.totalMemory() - runtime.freeMemory();
        List<Integer> linkedList = new LinkedList<>();
        for (int i = 0; i < testSize; i++) {
            linkedList.add(i);
        }
        afterMemory = runtime.totalMemory() - runtime.freeMemory();
        long linkedListMemory = afterMemory - beforeMemory;
        
        // HashMap内存测试
        linkedList = null; // 释放引用
        System.gc();
        beforeMemory = runtime.totalMemory() - runtime.freeMemory();
        Map<Integer, Integer> hashMap = new HashMap<>();
        for (int i = 0; i < testSize; i++) {
            hashMap.put(i, i);
        }
        afterMemory = runtime.totalMemory() - runtime.freeMemory();
        long hashMapMemory = afterMemory - beforeMemory;
        
        System.out.printf("ArrayList内存使用: %,d bytes (%.1f bytes/element)%n", 
                         arrayListMemory, (double)arrayListMemory / testSize);
        System.out.printf("LinkedList内存使用: %,d bytes (%.1f bytes/element, %.1fx)%n", 
                         linkedListMemory, (double)linkedListMemory / testSize, 
                         (double)linkedListMemory / arrayListMemory);
        System.out.printf("HashMap内存使用: %,d bytes (%.1f bytes/entry, %.1fx)%n", 
                         hashMapMemory, (double)hashMapMemory / testSize, 
                         (double)hashMapMemory / arrayListMemory);
        
        System.out.println("注意: 内存使用测试结果可能因JVM实现和GC策略而异");
    }

    /**
     * 并发性能测试
     */
    private static void concurrentPerformanceTest() {
        System.out.println("\n--- 并发性能测试 ---");
        
        int threadCount = 4;
        int operationsPerThread = 10000;
        System.out.printf("线程数: %d，每线程操作数: %,d%n", threadCount, operationsPerThread);
        
        // ConcurrentHashMap vs Hashtable vs synchronized HashMap
        testConcurrentMapPerformance(threadCount, operationsPerThread);
        
        // CopyOnWriteArrayList vs synchronized ArrayList
        testConcurrentListPerformance(threadCount, operationsPerThread);
        
        // BlockingQueue性能测试
        testBlockingQueuePerformance(threadCount, operationsPerThread);
    }

    private static void testConcurrentMapPerformance(int threadCount, int operationsPerThread) {
        System.out.println("  并发Map性能测试:");
        
        // ConcurrentHashMap测试
        Map<String, String> concurrentMap = new ConcurrentHashMap<>();
        long concurrentMapTime = measureConcurrentMapOperations(concurrentMap, threadCount, operationsPerThread);
        
        // Hashtable测试
        Map<String, String> hashtable = new Hashtable<>();
        long hashtableTime = measureConcurrentMapOperations(hashtable, threadCount, operationsPerThread);
        
        // synchronized HashMap测试
        Map<String, String> syncMap = Collections.synchronizedMap(new HashMap<>());
        long syncMapTime = measureConcurrentMapOperations(syncMap, threadCount, operationsPerThread);
        
        System.out.printf("    ConcurrentHashMap: %,d ms%n", concurrentMapTime);
        System.out.printf("    Hashtable: %,d ms (%.1fx slower)%n", 
                         hashtableTime, (double)hashtableTime / concurrentMapTime);
        System.out.printf("    synchronized HashMap: %,d ms (%.1fx slower)%n", 
                         syncMapTime, (double)syncMapTime / concurrentMapTime);
    }

    private static long measureConcurrentMapOperations(Map<String, String> map, int threadCount, int operationsPerThread) {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    Random random = new Random(threadId);
                    for (int j = 0; j < operationsPerThread; j++) {
                        String key = "Key_" + random.nextInt(operationsPerThread);
                        String value = "Value_" + j;
                        
                        // 50% 写操作，50% 读操作
                        if (j % 2 == 0) {
                            map.put(key, value);
                        } else {
                            map.get(key);
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long endTime = System.currentTimeMillis();
        executor.shutdown();
        
        return endTime - startTime;
    }

    private static void testConcurrentListPerformance(int threadCount, int operationsPerThread) {
        System.out.println("  并发List性能测试:");
        
        // CopyOnWriteArrayList测试
        List<String> cowList = new CopyOnWriteArrayList<>();
        long cowTime = measureConcurrentListOperations(cowList, threadCount, operationsPerThread);
        
        // synchronized ArrayList测试
        List<String> syncList = Collections.synchronizedList(new ArrayList<>());
        long syncTime = measureConcurrentListOperations(syncList, threadCount, operationsPerThread);
        
        System.out.printf("    CopyOnWriteArrayList: %,d ms%n", cowTime);
        System.out.printf("    synchronized ArrayList: %,d ms (%.1fx)%n", 
                         syncTime, (double)syncTime / cowTime);
    }

    private static long measureConcurrentListOperations(List<String> list, int threadCount, int operationsPerThread) {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        // 预填充一些数据
        for (int i = 0; i < 1000; i++) {
            list.add("Initial_" + i);
        }
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    Random random = new Random(threadId);
                    for (int j = 0; j < operationsPerThread; j++) {
                        // 80% 读操作，20% 写操作（符合读多写少的场景）
                        if (j % 5 == 0) {
                            list.add("New_" + threadId + "_" + j);
                        } else {
                            if (!list.isEmpty()) {
                                int index = random.nextInt(list.size());
                                list.get(index);
                            }
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long endTime = System.currentTimeMillis();
        executor.shutdown();
        
        return endTime - startTime;
    }

    private static void testBlockingQueuePerformance(int threadCount, int operationsPerThread) {
        System.out.println("  阻塞队列性能测试:");
        
        // ArrayBlockingQueue测试
        BlockingQueue<String> arrayQueue = new ArrayBlockingQueue<>(operationsPerThread * threadCount);
        long arrayQueueTime = measureBlockingQueueOperations(arrayQueue, threadCount, operationsPerThread);
        
        // LinkedBlockingQueue测试
        BlockingQueue<String> linkedQueue = new LinkedBlockingQueue<>();
        long linkedQueueTime = measureBlockingQueueOperations(linkedQueue, threadCount, operationsPerThread);
        
        System.out.printf("    ArrayBlockingQueue: %,d ms%n", arrayQueueTime);
        System.out.printf("    LinkedBlockingQueue: %,d ms (%.1fx)%n", 
                         linkedQueueTime, (double)linkedQueueTime / arrayQueueTime);
    }

    private static long measureBlockingQueueOperations(BlockingQueue<String> queue, int threadCount, int operationsPerThread) {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount * 2);
        CountDownLatch latch = new CountDownLatch(threadCount * 2);
        
        long startTime = System.currentTimeMillis();
        
        // 生产者线程
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        queue.offer("Item_" + threadId + "_" + j);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // 消费者线程
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        queue.poll(1, TimeUnit.SECONDS);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long endTime = System.currentTimeMillis();
        executor.shutdown();
        
        return endTime - startTime;
    }

    /**
     * 性能总结和建议
     */
    private static void performanceSummary() {
        System.out.println("\n--- 性能总结和选择建议 ---");
        
        System.out.println("1. List集合选择:");
        System.out.println("   - 随机访问密集: ArrayList (O(1)访问)");
        System.out.println("   - 频繁插入删除: LinkedList (O(1)头尾操作)");
        System.out.println("   - 内存敏感场景: ArrayList (更少内存开销)");
        System.out.println("   - 线程安全需求: CopyOnWriteArrayList或synchronized包装");
        
        System.out.println("\n2. Set集合选择:");
        System.out.println("   - 高性能查找: HashSet (O(1)平均复杂度)");
        System.out.println("   - 保持插入顺序: LinkedHashSet");
        System.out.println("   - 需要排序: TreeSet (O(log n)复杂度)");
        System.out.println("   - 并发场景: ConcurrentHashMap.newKeySet()");
        
        System.out.println("\n3. Map集合选择:");
        System.out.println("   - 一般用途: HashMap (最佳性能)");
        System.out.println("   - 保持顺序: LinkedHashMap");
        System.out.println("   - 需要排序: TreeMap");
        System.out.println("   - 高并发: ConcurrentHashMap");
        System.out.println("   - 避免Hashtable: 使用ConcurrentHashMap替代");
        
        System.out.println("\n4. Queue集合选择:");
        System.out.println("   - 一般队列: ArrayDeque (最佳性能)");
        System.out.println("   - 优先队列: PriorityQueue");
        System.out.println("   - 线程安全: BlockingQueue系列");
        System.out.println("   - 避免LinkedList: 作为队列性能较差");
        
        System.out.println("\n5. 并发场景建议:");
        System.out.println("   - 读多写少: CopyOnWriteArrayList, CopyOnWriteArraySet");
        System.out.println("   - 高并发读写: ConcurrentHashMap, ConcurrentSkipListMap");
        System.out.println("   - 生产者消费者: ArrayBlockingQueue, LinkedBlockingQueue");
        System.out.println("   - 避免Vector和Hashtable: 性能较差的遗留类");
        
        System.out.println("\n6. 内存优化建议:");
        System.out.println("   - 已知大小: 使用合适的初始容量");
        System.out.println("   - 内存敏感: 选择ArrayList而非LinkedList");
        System.out.println("   - 防止内存泄漏: 考虑使用WeakHashMap");
        System.out.println("   - 大量小对象: 考虑原始类型集合库(如TroveMap)");
        
        System.out.println("\n记住: 实际选择应基于具体使用场景的性能测试结果!");
    }
}

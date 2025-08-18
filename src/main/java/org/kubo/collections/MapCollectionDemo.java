package org.kubo.collections;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Map集合演示类
 * 演示HashMap、ConcurrentHashMap、TreeMap、LinkedHashMap、Hashtable、WeakHashMap、IdentityHashMap等的特性和性能差异
 */
@SuppressWarnings("unused")
public class MapCollectionDemo {

    /**
     * 运行Map集合演示
     */
    public static void run() {
        System.out.println("\n==== Map集合类型演示 ====");
        
        // 基本特性演示
        demonstrateBasicFeatures();
        
        // 排序和插入顺序演示
        orderingDemo();
        
        // 特殊Map演示
        specialMapsDemo();
        
        // 性能对比演示
        performanceComparison();
        
        // 线程安全性演示
        threadSafetyDemo();
        
        // 使用场景总结
        usageSummary();
    }

    /**
     * 演示各种Map的基本特性
     */
    private static void demonstrateBasicFeatures() {
        System.out.println("\n--- Map基本特性演示 ---");
        
        // HashMap - 基于哈希表实现，无序
        Map<String, Integer> hashMap = new HashMap<>();
        hashMap.put("苹果", 10);
        hashMap.put("香蕉", 20);
        hashMap.put("橙子", 15);
        hashMap.put("苹果", 25); // 覆盖原有值
        hashMap.put(null, 100);  // 允许null键
        hashMap.put("空值", null); // 允许null值
        System.out.println("HashMap (无序，允许null): " + hashMap);
        System.out.println("HashMap获取'苹果': " + hashMap.get("苹果"));
        
        // LinkedHashMap - 保持插入顺序或访问顺序
        Map<String, Integer> linkedHashMap = new LinkedHashMap<>();
        linkedHashMap.put("第一个", 1);
        linkedHashMap.put("第二个", 2);
        linkedHashMap.put("第三个", 3);
        linkedHashMap.put("第二个", 22); // 更新值，但保持位置
        System.out.println("LinkedHashMap (保持插入顺序): " + linkedHashMap);
        
        // LinkedHashMap with access order - 访问顺序
        Map<String, Integer> accessOrderMap = new LinkedHashMap<>(16, 0.75f, true);
        accessOrderMap.put("A", 1);
        accessOrderMap.put("B", 2);
        accessOrderMap.put("C", 3);
        accessOrderMap.get("A"); // 访问A，A会移到末尾
        System.out.println("LinkedHashMap (访问顺序): " + accessOrderMap);
        
        // TreeMap - 基于红黑树实现，有序
        Map<String, Integer> treeMap = new TreeMap<>();
        treeMap.put("banana", 10);
        treeMap.put("apple", 20);
        treeMap.put("orange", 15);
        treeMap.put("grape", 25);
        System.out.println("TreeMap (自然排序): " + treeMap);
        
        // TreeMap with custom comparator - 自定义排序
        Map<String, Integer> treeMapCustom = new TreeMap<>(String.CASE_INSENSITIVE_ORDER.reversed());
        treeMapCustom.put("banana", 10);
        treeMapCustom.put("Apple", 20);
        treeMapCustom.put("orange", 15);
        treeMapCustom.put("Grape", 25);
        System.out.println("TreeMap (自定义排序-忽略大小写倒序): " + treeMapCustom);
        
        // Hashtable - 线程安全的哈希表，不允许null
        Hashtable<String, Integer> hashtable = new Hashtable<>();
        hashtable.put("元素1", 100);
        hashtable.put("元素2", 200);
        hashtable.put("元素3", 300);
        // hashtable.put(null, 400); // 不允许null键，会抛出NullPointerException
        // hashtable.put("元素4", null); // 不允许null值，会抛出NullPointerException
        System.out.println("Hashtable (线程安全，不允许null): " + hashtable);
    }

    /**
     * 排序和插入顺序演示
     */
    private static void orderingDemo() {
        System.out.println("\n--- 排序和插入顺序演示 ---");
        
        String[] keys = {"zebra", "apple", "banana", "orange", "grape"};
        Integer[] values = {1, 2, 3, 4, 5};
        
        // HashMap - 无序
        Map<String, Integer> hashMap = new HashMap<>();
        for (int i = 0; i < keys.length; i++) {
            hashMap.put(keys[i], values[i]);
        }
        System.out.println("HashMap添加顺序: " + Arrays.toString(keys));
        System.out.println("HashMap实际顺序: " + hashMap.keySet());
        
        // LinkedHashMap - 保持插入顺序
        Map<String, Integer> linkedHashMap = new LinkedHashMap<>();
        for (int i = 0; i < keys.length; i++) {
            linkedHashMap.put(keys[i], values[i]);
        }
        System.out.println("LinkedHashMap添加顺序: " + Arrays.toString(keys));
        System.out.println("LinkedHashMap实际顺序: " + linkedHashMap.keySet());
        
        // TreeMap - 自然排序
        Map<String, Integer> treeMap = new TreeMap<>();
        for (int i = 0; i < keys.length; i++) {
            treeMap.put(keys[i], values[i]);
        }
        System.out.println("TreeMap添加顺序: " + Arrays.toString(keys));
        System.out.println("TreeMap排序结果: " + treeMap.keySet());
        
        // TreeMap导航方法演示
        TreeMap<Integer, String> numberMap = new TreeMap<>();
        numberMap.put(1, "一");
        numberMap.put(3, "三");
        numberMap.put(5, "五");
        numberMap.put(7, "七");
        numberMap.put(9, "九");
        numberMap.put(11, "十一");
        
        System.out.println("\nTreeMap导航方法演示:");
        System.out.println("原始映射: " + numberMap);
        System.out.println("第一个条目: " + numberMap.firstEntry());
        System.out.println("最后一个条目: " + numberMap.lastEntry());
        System.out.println("小于6的最大键: " + numberMap.lowerKey(6));
        System.out.println("大于等于6的最小键: " + numberMap.ceilingKey(6));
        System.out.println("小于等于6的最大键: " + numberMap.floorKey(6));
        System.out.println("大于6的最小键: " + numberMap.higherKey(6));
        System.out.println("子映射[3, 8): " + numberMap.subMap(3, 8));
        System.out.println("头部映射(<8): " + numberMap.headMap(8));
        System.out.println("尾部映射(>=8): " + numberMap.tailMap(8));
        
        // 演示LinkedHashMap的LRU缓存特性
        System.out.println("\nLinkedHashMap LRU缓存演示:");
        Map<String, String> lruCache = new LinkedHashMap<String, String>(3, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                return size() > 3; // 限制缓存大小为3
            }
        };
        
        lruCache.put("1", "第一个");
        lruCache.put("2", "第二个");
        lruCache.put("3", "第三个");
        System.out.println("初始缓存: " + lruCache);
        
        lruCache.get("1"); // 访问1，将其移到末尾
        System.out.println("访问'1'后: " + lruCache);
        
        lruCache.put("4", "第四个"); // 添加新元素，最老的元素被移除
        System.out.println("添加'4'后: " + lruCache);
    }

    /**
     * 特殊Map演示
     */
    private static void specialMapsDemo() {
        System.out.println("\n--- 特殊Map演示 ---");
        
        // WeakHashMap - 弱引用键的映射
        System.out.println("1. WeakHashMap演示:");
        Map<String, String> weakMap = new WeakHashMap<>();
        String key1 = new String("key1"); // 故意使用new String()
        String key2 = new String("key2");
        String key3 = "key3"; // 字符串常量池中的引用
        
        weakMap.put(key1, "value1");
        weakMap.put(key2, "value2");
        weakMap.put(key3, "value3");
        System.out.println("初始WeakHashMap: " + weakMap);
        
        key1 = null; // 移除强引用
        key2 = null;
        System.gc(); // 建议垃圾回收
        
        try {
            Thread.sleep(100); // 等待GC完成
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("GC后WeakHashMap: " + weakMap);
        System.out.println("说明: key1和key2可能被GC回收，key3仍然存在（字符串常量池引用）");
        
        // IdentityHashMap - 使用引用相等性而非对象相等性
        System.out.println("\n2. IdentityHashMap演示:");
        Map<String, String> identityMap = new IdentityHashMap<>();
        String str1 = new String("hello");
        String str2 = new String("hello");
        String str3 = "hello"; // 字符串常量池
        String str4 = "hello"; // 同一个常量池引用
        
        identityMap.put(str1, "str1");
        identityMap.put(str2, "str2");
        identityMap.put(str3, "str3");
        identityMap.put(str4, "str4"); // 会覆盖str3的值，因为str3和str4是同一个引用
        
        System.out.println("IdentityHashMap: " + identityMap);
        System.out.println("str1 == str2: " + (str1 == str2));
        System.out.println("str3 == str4: " + (str3 == str4));
        System.out.println("说明: IdentityHashMap使用==比较键，而不是equals()");
        
        // EnumMap - 专门用于枚举键的映射
        System.out.println("\n3. EnumMap演示:");
        enum Status {
            PENDING, RUNNING, COMPLETED, FAILED
        }
        
        Map<Status, String> enumMap = new EnumMap<>(Status.class);
        enumMap.put(Status.PENDING, "等待中");
        enumMap.put(Status.RUNNING, "运行中");
        enumMap.put(Status.COMPLETED, "已完成");
        enumMap.put(Status.FAILED, "失败");
        
        System.out.println("EnumMap: " + enumMap);
        System.out.println("EnumMap访问RUNNING: " + enumMap.get(Status.RUNNING));
        System.out.println("说明: EnumMap内部使用数组实现，性能极佳");
    }

    /**
     * 性能对比演示
     */
    private static void performanceComparison() {
        System.out.println("\n--- Map性能对比演示 ---");
        
        int elementCount = 100000;
        int lookupCount = 50000;
        
        // 准备测试数据
        List<String> testKeys = new ArrayList<>();
        List<String> testValues = new ArrayList<>();
        for (int i = 0; i < elementCount; i++) {
            testKeys.add("Key_" + i);
            testValues.add("Value_" + i);
        }
        Collections.shuffle(testKeys); // 打乱顺序
        
        // 准备查找测试数据
        List<String> lookupKeys = new ArrayList<>();
        Random random = new Random(12345);
        for (int i = 0; i < lookupCount; i++) {
            lookupKeys.add("Key_" + random.nextInt(elementCount));
        }
        
        System.out.printf("测试数据规模: %,d 元素，查找次数: %,d%n", elementCount, lookupCount);
        
        // 1. 插入性能测试
        System.out.println("\n1. 插入性能测试:");
        
        long startTime = System.nanoTime();
        Map<String, String> hashMap = new HashMap<>();
        for (int i = 0; i < elementCount; i++) {
            hashMap.put(testKeys.get(i), testValues.get(i));
        }
        long hashMapInsertTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        Map<String, String> linkedHashMap = new LinkedHashMap<>();
        for (int i = 0; i < elementCount; i++) {
            linkedHashMap.put(testKeys.get(i), testValues.get(i));
        }
        long linkedHashMapInsertTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        Map<String, String> treeMap = new TreeMap<>();
        for (int i = 0; i < elementCount; i++) {
            treeMap.put(testKeys.get(i), testValues.get(i));
        }
        long treeMapInsertTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        Map<String, String> hashtable = new Hashtable<>();
        for (int i = 0; i < elementCount; i++) {
            hashtable.put(testKeys.get(i), testValues.get(i));
        }
        long hashtableInsertTime = System.nanoTime() - startTime;
        
        System.out.printf("HashMap插入: %,d ns%n", hashMapInsertTime);
        System.out.printf("LinkedHashMap插入: %,d ns (%.1fx slower)%n", 
                         linkedHashMapInsertTime, (double)linkedHashMapInsertTime / hashMapInsertTime);
        System.out.printf("TreeMap插入: %,d ns (%.1fx slower)%n", 
                         treeMapInsertTime, (double)treeMapInsertTime / hashMapInsertTime);
        System.out.printf("Hashtable插入: %,d ns (%.1fx slower)%n", 
                         hashtableInsertTime, (double)hashtableInsertTime / hashMapInsertTime);
        
        // 2. 查找性能测试
        System.out.println("\n2. 查找性能测试:");
        
        startTime = System.nanoTime();
        int hashMapFound = 0;
        for (String key : lookupKeys) {
            if (hashMap.get(key) != null) hashMapFound++;
        }
        long hashMapLookupTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        int linkedHashMapFound = 0;
        for (String key : lookupKeys) {
            if (linkedHashMap.get(key) != null) linkedHashMapFound++;
        }
        long linkedHashMapLookupTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        int treeMapFound = 0;
        for (String key : lookupKeys) {
            if (treeMap.get(key) != null) treeMapFound++;
        }
        long treeMapLookupTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        int hashtableFound = 0;
        for (String key : lookupKeys) {
            if (hashtable.get(key) != null) hashtableFound++;
        }
        long hashtableLookupTime = System.nanoTime() - startTime;
        
        System.out.printf("HashMap查找: %,d ns (找到 %d 个)%n", hashMapLookupTime, hashMapFound);
        System.out.printf("LinkedHashMap查找: %,d ns (找到 %d 个, %.1fx slower)%n", 
                         linkedHashMapLookupTime, linkedHashMapFound, (double)linkedHashMapLookupTime / hashMapLookupTime);
        System.out.printf("TreeMap查找: %,d ns (找到 %d 个, %.1fx slower)%n", 
                         treeMapLookupTime, treeMapFound, (double)treeMapLookupTime / hashMapLookupTime);
        System.out.printf("Hashtable查找: %,d ns (找到 %d 个, %.1fx slower)%n", 
                         hashtableLookupTime, hashtableFound, (double)hashtableLookupTime / hashMapLookupTime);
        
        // 3. 遍历性能测试
        System.out.println("\n3. 遍历性能测试:");
        
        startTime = System.nanoTime();
        int hashMapSum = 0;
        for (Map.Entry<String, String> entry : hashMap.entrySet()) {
            hashMapSum += entry.getKey().hashCode() + entry.getValue().hashCode();
        }
        long hashMapIterateTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        int linkedHashMapSum = 0;
        for (Map.Entry<String, String> entry : linkedHashMap.entrySet()) {
            linkedHashMapSum += entry.getKey().hashCode() + entry.getValue().hashCode();
        }
        long linkedHashMapIterateTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        int treeMapSum = 0;
        for (Map.Entry<String, String> entry : treeMap.entrySet()) {
            treeMapSum += entry.getKey().hashCode() + entry.getValue().hashCode();
        }
        long treeMapIterateTime = System.nanoTime() - startTime;
        
        System.out.printf("HashMap遍历: %,d ns%n", hashMapIterateTime);
        System.out.printf("LinkedHashMap遍历: %,d ns (%.1fx slower)%n", 
                         linkedHashMapIterateTime, (double)linkedHashMapIterateTime / hashMapIterateTime);
        System.out.printf("TreeMap遍历: %,d ns (%.1fx slower)%n", 
                         treeMapIterateTime, (double)treeMapIterateTime / hashMapIterateTime);
    }

    /**
     * 线程安全性演示
     */
    private static void threadSafetyDemo() {
        System.out.println("\n--- 线程安全性演示 ---");
        
        // 非线程安全的HashMap
        Map<String, String> hashMap = new HashMap<>();
        
        // 线程安全的Hashtable
        Map<String, String> hashtable = new Hashtable<>();
        
        // 线程安全的ConcurrentHashMap
        Map<String, String> concurrentHashMap = new ConcurrentHashMap<>();
        
        // 线程安全的ConcurrentSkipListMap
        ConcurrentNavigableMap<String, String> skipListMap = new ConcurrentSkipListMap<>();
        
        // 通过Collections.synchronizedMap包装的HashMap
        Map<String, String> syncMap = Collections.synchronizedMap(new HashMap<>());
        
        System.out.println("HashMap - 非线程安全，多线程环境下可能出现死循环、数据丢失等问题");
        System.out.println("Hashtable - 线程安全，所有方法都使用synchronized修饰，性能较差");
        System.out.println("ConcurrentHashMap - 线程安全，使用分段锁或CAS，高并发性能好");
        System.out.println("ConcurrentSkipListMap - 线程安全，支持排序，基于跳表实现");
        System.out.println("Collections.synchronizedMap - 通过同步包装器实现线程安全");
        
        // 演示ConcurrentHashMap的并发特性
        System.out.println("\nConcurrentHashMap并发特性演示:");
        ConcurrentHashMap<String, Integer> concurrentMap = new ConcurrentHashMap<>();
        
        // 基本操作
        concurrentMap.put("A", 1);
        concurrentMap.put("B", 2);
        concurrentMap.put("C", 3);
        
        System.out.println("初始映射: " + concurrentMap);
        
        // 原子操作演示
        concurrentMap.putIfAbsent("D", 4); // 只有当键不存在时才插入
        concurrentMap.putIfAbsent("A", 10); // A已存在，不会被覆盖
        System.out.println("putIfAbsent后: " + concurrentMap);
        
        // 条件替换
        concurrentMap.replace("B", 2, 20); // 只有当前值为2时才替换为20
        concurrentMap.replace("C", 30);    // 无条件替换
        System.out.println("replace后: " + concurrentMap);
        
        // 计算操作
        concurrentMap.compute("E", (key, val) -> val == null ? 1 : val + 1);
        concurrentMap.compute("A", (key, val) -> val == null ? 1 : val + 1);
        System.out.println("compute后: " + concurrentMap);
        
        // 合并操作
        concurrentMap.merge("F", 5, Integer::sum); // 键不存在，直接插入5
        concurrentMap.merge("A", 10, Integer::sum); // 键存在，执行sum操作
        System.out.println("merge后: " + concurrentMap);
        
        // 演示ConcurrentSkipListMap的有序特性
        System.out.println("\nConcurrentSkipListMap有序特性演示:");
        ConcurrentSkipListMap<Integer, String> sortedConcurrentMap = new ConcurrentSkipListMap<>();
        
        // 乱序插入
        sortedConcurrentMap.put(3, "三");
        sortedConcurrentMap.put(1, "一");
        sortedConcurrentMap.put(4, "四");
        sortedConcurrentMap.put(2, "二");
        sortedConcurrentMap.put(5, "五");
        
        System.out.println("有序映射: " + sortedConcurrentMap);
        System.out.println("第一个条目: " + sortedConcurrentMap.firstEntry());
        System.out.println("最后一个条目: " + sortedConcurrentMap.lastEntry());
        System.out.println("子映射[2,4]: " + sortedConcurrentMap.subMap(2, 4));
        
        // 原子的导航操作
        System.out.println("移除并返回第一个: " + sortedConcurrentMap.pollFirstEntry());
        System.out.println("移除并返回最后一个: " + sortedConcurrentMap.pollLastEntry());
        System.out.println("操作后的映射: " + sortedConcurrentMap);
    }

    /**
     * 使用场景总结
     */
    private static void usageSummary() {
        System.out.println("\n--- Map集合使用场景总结 ---");
        
        System.out.println("1. HashMap:");
        System.out.println("   - 适用场景: 一般的键值对存储，缓存实现，不关心顺序");
        System.out.println("   - 优点: O(1)查找/插入/删除，内存效率高，允许null键值");
        System.out.println("   - 缺点: 无序，非线程安全，hash冲突时性能下降");
        
        System.out.println("\n2. LinkedHashMap:");
        System.out.println("   - 适用场景: 需要保持插入顺序或访问顺序，LRU缓存实现");
        System.out.println("   - 优点: 保持顺序，O(1)操作复杂度，支持LRU策略");
        System.out.println("   - 缺点: 额外的链表指针开销，非线程安全");
        
        System.out.println("\n3. TreeMap:");
        System.out.println("   - 适用场景: 需要排序的键值对，范围查询操作");
        System.out.println("   - 优点: 自动排序，支持范围操作，NavigableMap接口");
        System.out.println("   - 缺点: O(log n)操作复杂度，非线程安全，键必须可比较");
        
        System.out.println("\n4. Hashtable:");
        System.out.println("   - 适用场景: 遗留代码，需要线程安全的简单场景");
        System.out.println("   - 优点: 线程安全，不允许null键值");
        System.out.println("   - 缺点: 性能差，粗粒度同步，现代代码中建议避免");
        
        System.out.println("\n5. ConcurrentHashMap:");
        System.out.println("   - 适用场景: 高并发的键值对存储，缓存实现");
        System.out.println("   - 优点: 高并发性能，丰富的原子操作，线程安全");
        System.out.println("   - 缺点: 相对复杂，内存开销略大");
        
        System.out.println("\n6. WeakHashMap:");
        System.out.println("   - 适用场景: 临时缓存，避免内存泄漏的场景");
        System.out.println("   - 优点: 自动清理无强引用的键，防止内存泄漏");
        System.out.println("   - 缺点: 行为不可预测，不适合一般用途");
        
        System.out.println("\n7. IdentityHashMap:");
        System.out.println("   - 适用场景: 需要引用相等性比较，对象追踪");
        System.out.println("   - 优点: 使用==比较，可以区分内容相同但引用不同的对象");
        System.out.println("   - 缺点: 违反Map接口的一般契约，使用场景限制");
        
        System.out.println("\n8. EnumMap:");
        System.out.println("   - 适用场景: 枚举作为键的映射，状态机实现");
        System.out.println("   - 优点: 极高性能，内存效率，类型安全");
        System.out.println("   - 缺点: 只能用于枚举键");
        
        System.out.println("\n9. ConcurrentSkipListMap:");
        System.out.println("   - 适用场景: 需要排序的并发Map，高并发范围查询");
        System.out.println("   - 优点: 线程安全，支持排序，高并发性能");
        System.out.println("   - 缺点: 内存开销大，实现复杂");
        
        System.out.println("\n推荐选择原则:");
        System.out.println("- 一般用途: HashMap");
        System.out.println("- 保持顺序: LinkedHashMap");
        System.out.println("- 需要排序: TreeMap");
        System.out.println("- 高并发: ConcurrentHashMap");
        System.out.println("- 并发且排序: ConcurrentSkipListMap");
        System.out.println("- 枚举键: EnumMap");
        System.out.println("- 防内存泄漏: WeakHashMap");
        System.out.println("- LRU缓存: LinkedHashMap(accessOrder=true)");
    }
}

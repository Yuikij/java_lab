package org.kubo.collections;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Set集合演示类
 * 演示HashSet、TreeSet、LinkedHashSet、EnumSet、CopyOnWriteArraySet、ConcurrentSkipListSet的特性和性能差异
 */
@SuppressWarnings("unused")
public class SetCollectionDemo {

    /**
     * 测试用的枚举类型
     */
    enum Color {
        RED, GREEN, BLUE, YELLOW, BLACK, WHITE
    }

    /**
     * 运行Set集合演示
     */
    public static void run() {
        System.out.println("\n==== Set集合类型演示 ====");
        
        // 基本特性演示
        demonstrateBasicFeatures();
        
        // 排序和插入顺序演示
        orderingDemo();
        
        // 性能对比演示
        performanceComparison();
        
        // 线程安全性演示
        threadSafetyDemo();
        
        // 使用场景总结
        usageSummary();
    }

    /**
     * 演示各种Set的基本特性
     */
    private static void demonstrateBasicFeatures() {
        System.out.println("\n--- Set基本特性演示 ---");
        
        // HashSet - 基于哈希表实现，无序
        Set<String> hashSet = new HashSet<>();
        hashSet.add("苹果");
        hashSet.add("香蕉");
        hashSet.add("橙子");
        hashSet.add("苹果"); // 重复元素，不会被添加
        System.out.println("HashSet (无序，不重复): " + hashSet);
        System.out.println("HashSet包含'苹果': " + hashSet.contains("苹果"));
        
        // TreeSet - 基于红黑树实现，有序
        Set<String> treeSet = new TreeSet<>();
        treeSet.add("banana");
        treeSet.add("apple");
        treeSet.add("orange");
        treeSet.add("grape");
        System.out.println("TreeSet (自然排序): " + treeSet);
        
        // TreeSet with custom comparator - 自定义排序
        Set<String> treeSetCustom = new TreeSet<>(String.CASE_INSENSITIVE_ORDER.reversed());
        treeSetCustom.add("banana");
        treeSetCustom.add("Apple");
        treeSetCustom.add("orange");
        treeSetCustom.add("Grape");
        System.out.println("TreeSet (自定义排序-忽略大小写倒序): " + treeSetCustom);
        
        // LinkedHashSet - 保持插入顺序
        Set<String> linkedHashSet = new LinkedHashSet<>();
        linkedHashSet.add("第一个");
        linkedHashSet.add("第二个");
        linkedHashSet.add("第三个");
        linkedHashSet.add("第一个"); // 重复元素
        System.out.println("LinkedHashSet (保持插入顺序): " + linkedHashSet);
        
        // EnumSet - 专门用于枚举类型的Set
        Set<Color> enumSet = EnumSet.of(Color.RED, Color.GREEN, Color.BLUE);
        enumSet.add(Color.YELLOW);
        enumSet.add(Color.RED); // 重复元素
        System.out.println("EnumSet: " + enumSet);
        
        // EnumSet范围操作
        Set<Color> enumRange = EnumSet.range(Color.GREEN, Color.BLACK);
        System.out.println("EnumSet范围 (GREEN到BLACK): " + enumRange);
        
        // EnumSet补集操作
        EnumSet<Color> enumComplement = EnumSet.complementOf(EnumSet.copyOf(enumSet));
        System.out.println("EnumSet补集: " + enumComplement);
    }

    /**
     * 排序和插入顺序演示
     */
    private static void orderingDemo() {
        System.out.println("\n--- 排序和插入顺序演示 ---");
        
        String[] items = {"zebra", "apple", "banana", "orange", "grape"};
        
        // HashSet - 无序
        Set<String> hashSet = new HashSet<>();
        for (String item : items) {
            hashSet.add(item);
        }
        System.out.println("HashSet添加顺序: " + Arrays.toString(items));
        System.out.println("HashSet实际顺序: " + hashSet);
        
        // LinkedHashSet - 保持插入顺序
        Set<String> linkedHashSet = new LinkedHashSet<>();
        for (String item : items) {
            linkedHashSet.add(item);
        }
        System.out.println("LinkedHashSet添加顺序: " + Arrays.toString(items));
        System.out.println("LinkedHashSet实际顺序: " + linkedHashSet);
        
        // TreeSet - 自然排序
        Set<String> treeSet = new TreeSet<>();
        for (String item : items) {
            treeSet.add(item);
        }
        System.out.println("TreeSet添加顺序: " + Arrays.toString(items));
        System.out.println("TreeSet排序结果: " + treeSet);
        
        // TreeSet导航方法演示
        TreeSet<Integer> numberSet = new TreeSet<>();
        numberSet.addAll(Arrays.asList(1, 3, 5, 7, 9, 11, 13, 15));
        System.out.println("\nTreeSet导航方法演示:");
        System.out.println("原始集合: " + numberSet);
        System.out.println("第一个元素: " + numberSet.first());
        System.out.println("最后一个元素: " + numberSet.last());
        System.out.println("小于8的最大元素: " + numberSet.lower(8));
        System.out.println("大于等于8的最小元素: " + numberSet.ceiling(8));
        System.out.println("小于等于8的最大元素: " + numberSet.floor(8));
        System.out.println("大于8的最小元素: " + numberSet.higher(8));
        System.out.println("子集[5, 10): " + numberSet.subSet(5, 10));
        System.out.println("头部集合(<10): " + numberSet.headSet(10));
        System.out.println("尾部集合(>=10): " + numberSet.tailSet(10));
    }

    /**
     * 性能对比演示
     */
    private static void performanceComparison() {
        System.out.println("\n--- Set性能对比演示 ---");
        
        int elementCount = 100000;
        int lookupCount = 50000;
        
        // 准备测试数据
        List<String> testData = new ArrayList<>();
        for (int i = 0; i < elementCount; i++) {
            testData.add("Element_" + i);
        }
        Collections.shuffle(testData); // 打乱顺序
        
        // 测试查找的数据
        List<String> lookupData = new ArrayList<>();
        Random random = new Random(12345);
        for (int i = 0; i < lookupCount; i++) {
            lookupData.add("Element_" + random.nextInt(elementCount));
        }
        
        System.out.printf("测试数据规模: %,d 元素，查找次数: %,d%n", elementCount, lookupCount);
        
        // 1. 插入性能测试
        System.out.println("\n1. 插入性能测试:");
        
        long startTime = System.nanoTime();
        Set<String> hashSet = new HashSet<>();
        for (String item : testData) {
            hashSet.add(item);
        }
        long hashSetInsertTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        Set<String> treeSet = new TreeSet<>();
        for (String item : testData) {
            treeSet.add(item);
        }
        long treeSetInsertTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        Set<String> linkedHashSet = new LinkedHashSet<>();
        for (String item : testData) {
            linkedHashSet.add(item);
        }
        long linkedHashSetInsertTime = System.nanoTime() - startTime;
        
        System.out.printf("HashSet插入: %,d ns%n", hashSetInsertTime);
        System.out.printf("TreeSet插入: %,d ns (%.1fx slower)%n", 
                         treeSetInsertTime, (double)treeSetInsertTime / hashSetInsertTime);
        System.out.printf("LinkedHashSet插入: %,d ns (%.1fx slower)%n", 
                         linkedHashSetInsertTime, (double)linkedHashSetInsertTime / hashSetInsertTime);
        
        // 2. 查找性能测试
        System.out.println("\n2. 查找性能测试:");
        
        startTime = System.nanoTime();
        int hashSetFound = 0;
        for (String item : lookupData) {
            if (hashSet.contains(item)) hashSetFound++;
        }
        long hashSetLookupTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        int treeSetFound = 0;
        for (String item : lookupData) {
            if (treeSet.contains(item)) treeSetFound++;
        }
        long treeSetLookupTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        int linkedHashSetFound = 0;
        for (String item : lookupData) {
            if (linkedHashSet.contains(item)) linkedHashSetFound++;
        }
        long linkedHashSetLookupTime = System.nanoTime() - startTime;
        
        System.out.printf("HashSet查找: %,d ns (找到 %d 个)%n", hashSetLookupTime, hashSetFound);
        System.out.printf("TreeSet查找: %,d ns (找到 %d 个, %.1fx slower)%n", 
                         treeSetLookupTime, treeSetFound, (double)treeSetLookupTime / hashSetLookupTime);
        System.out.printf("LinkedHashSet查找: %,d ns (找到 %d 个, %.1fx slower)%n", 
                         linkedHashSetLookupTime, linkedHashSetFound, (double)linkedHashSetLookupTime / hashSetLookupTime);
        
        // 3. 遍历性能测试
        System.out.println("\n3. 遍历性能测试:");
        
        startTime = System.nanoTime();
        int hashSetSum = 0;
        for (String item : hashSet) {
            hashSetSum += item.hashCode();
        }
        long hashSetIterateTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        int treeSetSum = 0;
        for (String item : treeSet) {
            treeSetSum += item.hashCode();
        }
        long treeSetIterateTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        int linkedHashSetSum = 0;
        for (String item : linkedHashSet) {
            linkedHashSetSum += item.hashCode();
        }
        long linkedHashSetIterateTime = System.nanoTime() - startTime;
        
        System.out.printf("HashSet遍历: %,d ns%n", hashSetIterateTime);
        System.out.printf("TreeSet遍历: %,d ns (%.1fx slower)%n", 
                         treeSetIterateTime, (double)treeSetIterateTime / hashSetIterateTime);
        System.out.printf("LinkedHashSet遍历: %,d ns (%.1fx slower)%n", 
                         linkedHashSetIterateTime, (double)linkedHashSetIterateTime / hashSetIterateTime);
    }

    /**
     * 线程安全性演示
     */
    private static void threadSafetyDemo() {
        System.out.println("\n--- 线程安全性演示 ---");
        
        // 非线程安全的HashSet
        Set<String> hashSet = new HashSet<>();
        
        // 线程安全的CopyOnWriteArraySet
        Set<String> cowSet = new CopyOnWriteArraySet<>();
        
        // 线程安全的ConcurrentSkipListSet
        Set<String> skipListSet = new ConcurrentSkipListSet<>();
        
        // 通过Collections.synchronizedSet包装的HashSet
        Set<String> syncSet = Collections.synchronizedSet(new HashSet<>());
        
        System.out.println("HashSet - 非线程安全，多线程环境下可能出现数据不一致");
        System.out.println("CopyOnWriteArraySet - 线程安全，写时复制策略，适合读多写少场景");
        System.out.println("ConcurrentSkipListSet - 线程安全，基于跳表实现，支持排序");
        System.out.println("Collections.synchronizedSet - 通过同步包装器实现线程安全");
        
        // 演示ConcurrentSkipListSet的并发特性
        System.out.println("\nConcurrentSkipListSet并发演示:");
        ConcurrentSkipListSet<Integer> concurrentSet = new ConcurrentSkipListSet<>();
        
        // 添加一些元素
        for (int i = 0; i < 10; i++) {
            concurrentSet.add(i * 10);
        }
        
        System.out.println("初始集合: " + concurrentSet);
        System.out.println("第一个元素: " + concurrentSet.first());
        System.out.println("最后一个元素: " + concurrentSet.last());
        System.out.println("小于50的元素: " + concurrentSet.headSet(50));
        System.out.println("大于等于50的元素: " + concurrentSet.tailSet(50));
        
        // 演示原子操作
        System.out.println("移除并返回第一个元素: " + concurrentSet.pollFirst());
        System.out.println("移除并返回最后一个元素: " + concurrentSet.pollLast());
        System.out.println("操作后的集合: " + concurrentSet);
        
        // 演示CopyOnWriteArraySet的写时复制特性
        System.out.println("\nCopyOnWriteArraySet写时复制演示:");
        CopyOnWriteArraySet<String> cowDemo = new CopyOnWriteArraySet<>();
        cowDemo.add("元素1");
        cowDemo.add("元素2");
        cowDemo.add("元素3");
        
        Iterator<String> iterator = cowDemo.iterator();
        
        // 在迭代过程中修改集合
        cowDemo.add("新元素");
        
        System.out.print("迭代器遍历结果: ");
        while (iterator.hasNext()) {
            System.out.print(iterator.next() + " ");
        }
        System.out.println();
        System.out.println("集合当前内容: " + cowDemo);
        System.out.println("说明: 迭代器看到的是创建时的快照，新元素不在快照中");
    }

    /**
     * 使用场景总结
     */
    private static void usageSummary() {
        System.out.println("\n--- Set集合使用场景总结 ---");
        
        System.out.println("1. HashSet:");
        System.out.println("   - 适用场景: 需要快速查找、去重，不关心元素顺序");
        System.out.println("   - 优点: O(1)查找/插入/删除，内存效率高");
        System.out.println("   - 缺点: 无序，非线程安全，依赖hashCode()和equals()实现");
        
        System.out.println("\n2. LinkedHashSet:");
        System.out.println("   - 适用场景: 需要保持插入顺序的去重操作");
        System.out.println("   - 优点: 保持插入顺序，O(1)查找/插入/删除");
        System.out.println("   - 缺点: 额外的链表指针开销，非线程安全");
        
        System.out.println("\n3. TreeSet:");
        System.out.println("   - 适用场景: 需要排序的去重集合，范围查询操作");
        System.out.println("   - 优点: 自动排序，支持范围操作，O(log n)操作复杂度");
        System.out.println("   - 缺点: 性能相对较慢，非线程安全，元素必须可比较");
        
        System.out.println("\n4. EnumSet:");
        System.out.println("   - 适用场景: 枚举类型的集合操作，位掩码替代");
        System.out.println("   - 优点: 极高的性能，内存效率，丰富的集合操作");
        System.out.println("   - 缺点: 只能用于枚举类型，枚举常量不能超过64个(通常)");
        
        System.out.println("\n5. CopyOnWriteArraySet:");
        System.out.println("   - 适用场景: 读多写少的并发场景，如监听器集合");
        System.out.println("   - 优点: 读操作无锁，线程安全，迭代器不会fail-fast");
        System.out.println("   - 缺点: 写操作开销大，内存占用大，只保证最终一致性");
        
        System.out.println("\n6. ConcurrentSkipListSet:");
        System.out.println("   - 适用场景: 需要排序的并发Set，高并发查找操作");
        System.out.println("   - 优点: 线程安全，支持排序，高并发性能好");
        System.out.println("   - 缺点: 内存开销较大，复杂度相对较高");
        
        System.out.println("\n推荐选择原则:");
        System.out.println("- 一般去重: HashSet");
        System.out.println("- 保持插入顺序: LinkedHashSet");
        System.out.println("- 需要排序: TreeSet");
        System.out.println("- 枚举类型: EnumSet");
        System.out.println("- 并发读多写少: CopyOnWriteArraySet");
        System.out.println("- 并发且需要排序: ConcurrentSkipListSet");
        System.out.println("- 一般并发场景: Collections.synchronizedSet() 或 ConcurrentHashMap.newKeySet()");
    }
}

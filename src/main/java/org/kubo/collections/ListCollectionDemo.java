package org.kubo.collections;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * List集合演示类
 * 演示ArrayList、LinkedList、Vector、Stack、CopyOnWriteArrayList的特性和性能差异
 */
@SuppressWarnings("unused")
public class ListCollectionDemo {

    /**
     * 运行List集合演示
     */
    public static void run() {
        System.out.println("\n==== List集合类型演示 ====");
        
        // 基本特性演示
        demonstrateBasicFeatures();
        
        // 性能对比演示
        performanceComparison();
        
        // 线程安全性演示
        threadSafetyDemo();
        
        // 使用场景总结
        usageSummary();
    }

    /**
     * 演示各种List的基本特性
     */
    private static void demonstrateBasicFeatures() {
        System.out.println("\n--- List基本特性演示 ---");
        
        // ArrayList - 基于动态数组实现
        List<String> arrayList = new ArrayList<>();
        arrayList.add("苹果");
        arrayList.add("香蕉");
        arrayList.add("橙子");
        arrayList.add(1, "草莓"); // 在指定位置插入
        System.out.println("ArrayList: " + arrayList);
        System.out.println("ArrayList访问索引1: " + arrayList.get(1));
        
        // LinkedList - 基于双向链表实现
        LinkedList<String> linkedList = new LinkedList<>();
        linkedList.add("第一个");
        linkedList.add("第二个");
        linkedList.addFirst("头部元素"); // 在头部添加
        linkedList.addLast("尾部元素");  // 在尾部添加
        System.out.println("LinkedList: " + linkedList);
        System.out.println("LinkedList头部元素: " + linkedList.getFirst());
        System.out.println("LinkedList尾部元素: " + linkedList.getLast());
        
        // Vector - 线程安全的动态数组（同步）
        Vector<String> vector = new Vector<>();
        vector.add("Vector元素1");
        vector.add("Vector元素2");
        vector.insertElementAt("插入元素", 1);
        System.out.println("Vector: " + vector);
        System.out.println("Vector容量: " + vector.capacity());
        
        // Stack - 继承自Vector的栈结构
        Stack<String> stack = new Stack<>();
        stack.push("栈底");
        stack.push("中间");
        stack.push("栈顶");
        System.out.println("Stack: " + stack);
        System.out.println("Stack弹出元素: " + stack.pop());
        System.out.println("Stack查看栈顶: " + stack.peek());
        
        // CopyOnWriteArrayList - 线程安全的ArrayList变体
        CopyOnWriteArrayList<String> cowList = new CopyOnWriteArrayList<>();
        cowList.add("COW元素1");
        cowList.add("COW元素2");
        cowList.add("COW元素3");
        System.out.println("CopyOnWriteArrayList: " + cowList);
    }

    /**
     * 性能对比演示
     */
    private static void performanceComparison() {
        System.out.println("\n--- List性能对比演示 ---");
        
        int elementCount = 100000;
        int operationCount = 10000;
        
        // 初始化测试数据
        List<Integer> arrayList = new ArrayList<>();
        List<Integer> linkedList = new LinkedList<>();
        List<Integer> vector = new Vector<>();
        
        // 预填充数据
        for (int i = 0; i < elementCount; i++) {
            arrayList.add(i);
            linkedList.add(i);
            vector.add(i);
        }
        
        System.out.printf("测试数据规模: %,d 元素，操作次数: %,d%n", elementCount, operationCount);
        
        // 1. 随机访问性能测试
        System.out.println("\n1. 随机访问性能测试:");
        Random random = new Random(12345);
        
        long startTime = System.nanoTime();
        for (int i = 0; i < operationCount; i++) {
            int index = random.nextInt(elementCount);
            arrayList.get(index);
        }
        long arrayListTime = System.nanoTime() - startTime;
        
        random = new Random(12345); // 重置随机种子保证公平
        startTime = System.nanoTime();
        for (int i = 0; i < operationCount; i++) {
            int index = random.nextInt(elementCount);
            linkedList.get(index);
        }
        long linkedListTime = System.nanoTime() - startTime;
        
        random = new Random(12345);
        startTime = System.nanoTime();
        for (int i = 0; i < operationCount; i++) {
            int index = random.nextInt(elementCount);
            vector.get(index);
        }
        long vectorTime = System.nanoTime() - startTime;
        
        System.out.printf("ArrayList随机访问: %,d ns%n", arrayListTime);
        System.out.printf("LinkedList随机访问: %,d ns (%.1fx slower)%n", 
                         linkedListTime, (double)linkedListTime / arrayListTime);
        System.out.printf("Vector随机访问: %,d ns (%.1fx slower)%n", 
                         vectorTime, (double)vectorTime / arrayListTime);
        
        // 2. 头部插入性能测试
        System.out.println("\n2. 头部插入性能测试:");
        List<Integer> arrayListInsert = new ArrayList<>();
        LinkedList<Integer> linkedListInsert = new LinkedList<>();
        List<Integer> vectorInsert = new Vector<>();
        
        int insertOperations = 20000;
        
        startTime = System.nanoTime();
        for (int i = 0; i < insertOperations; i++) {
            arrayListInsert.add(0, i);
        }
        long arrayListInsertTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        for (int i = 0; i < insertOperations; i++) {
            linkedListInsert.addFirst(i);
        }
        long linkedListInsertTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        for (int i = 0; i < insertOperations; i++) {
            vectorInsert.add(0, i);
        }
        long vectorInsertTime = System.nanoTime() - startTime;
        
        System.out.printf("ArrayList头部插入: %,d ns%n", arrayListInsertTime);
        System.out.printf("LinkedList头部插入: %,d ns (%.1fx faster)%n", 
                         linkedListInsertTime, (double)arrayListInsertTime / linkedListInsertTime);
        System.out.printf("Vector头部插入: %,d ns (%.1fx slower)%n", 
                         vectorInsertTime, (double)vectorInsertTime / arrayListInsertTime);
        
        // 3. 顺序遍历性能测试
        System.out.println("\n3. 顺序遍历性能测试:");
        
        startTime = System.nanoTime();
        long sum = 0;
        for (Integer value : arrayList) {
            sum += value;
        }
        long arrayListIterateTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        sum = 0;
        for (Integer value : linkedList) {
            sum += value;
        }
        long linkedListIterateTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        sum = 0;
        for (Integer value : vector) {
            sum += value;
        }
        long vectorIterateTime = System.nanoTime() - startTime;
        
        System.out.printf("ArrayList顺序遍历: %,d ns%n", arrayListIterateTime);
        System.out.printf("LinkedList顺序遍历: %,d ns (%.1fx slower)%n", 
                         linkedListIterateTime, (double)linkedListIterateTime / arrayListIterateTime);
        System.out.printf("Vector顺序遍历: %,d ns (%.1fx slower)%n", 
                         vectorIterateTime, (double)vectorIterateTime / arrayListIterateTime);
    }

    /**
     * 线程安全性演示
     */
    private static void threadSafetyDemo() {
        System.out.println("\n--- 线程安全性演示 ---");
        
        // 非线程安全的ArrayList
        List<Integer> arrayList = new ArrayList<>();
        
        // 线程安全的Vector
        List<Integer> vector = new Vector<>();
        
        // 线程安全的CopyOnWriteArrayList
        List<Integer> cowList = new CopyOnWriteArrayList<>();
        
        // 通过Collections.synchronizedList包装的ArrayList
        List<Integer> syncList = Collections.synchronizedList(new ArrayList<>());
        
        System.out.println("ArrayList - 非线程安全，多线程环境下可能出现数据不一致");
        System.out.println("Vector - 线程安全，所有方法都使用synchronized修饰");
        System.out.println("CopyOnWriteArrayList - 线程安全，写时复制策略，适合读多写少场景");
        System.out.println("Collections.synchronizedList - 通过同步包装器实现线程安全");
        
        // 演示CopyOnWriteArrayList的写时复制特性
        System.out.println("\nCopyOnWriteArrayList写时复制演示:");
        CopyOnWriteArrayList<String> cowDemo = new CopyOnWriteArrayList<>();
        cowDemo.add("元素1");
        cowDemo.add("元素2");
        cowDemo.add("元素3");
        
        // 获取迭代器
        Iterator<String> iterator = cowDemo.iterator();
        
        // 在迭代过程中修改列表
        cowDemo.add("新元素");
        
        System.out.print("迭代器遍历结果: ");
        while (iterator.hasNext()) {
            System.out.print(iterator.next() + " ");
        }
        System.out.println();
        System.out.println("列表当前内容: " + cowDemo);
        System.out.println("说明: 迭代器创建时的快照不包含新添加的元素，这就是写时复制的特性");
    }

    /**
     * 使用场景总结
     */
    private static void usageSummary() {
        System.out.println("\n--- List集合使用场景总结 ---");
        
        System.out.println("1. ArrayList:");
        System.out.println("   - 适用场景: 需要频繁随机访问元素，读操作多于写操作");
        System.out.println("   - 优点: O(1)随机访问，内存占用相对较小，遍历性能好");
        System.out.println("   - 缺点: 中间插入/删除性能差O(n)，非线程安全");
        
        System.out.println("\n2. LinkedList:");
        System.out.println("   - 适用场景: 需要频繁在头尾插入/删除元素，作为队列或栈使用");
        System.out.println("   - 优点: O(1)头尾插入/删除，实现了Deque接口");
        System.out.println("   - 缺点: O(n)随机访问，额外的指针开销，缓存性能差");
        
        System.out.println("\n3. Vector:");
        System.out.println("   - 适用场景: 需要线程安全的动态数组，遗留代码兼容");
        System.out.println("   - 优点: 线程安全，API与ArrayList类似");
        System.out.println("   - 缺点: 性能开销大，粗粒度同步，现代代码中建议避免使用");
        
        System.out.println("\n4. Stack:");
        System.out.println("   - 适用场景: 需要栈结构的后进先出操作");
        System.out.println("   - 优点: 提供push/pop/peek等栈操作");
        System.out.println("   - 缺点: 继承自Vector，性能不佳，建议使用ArrayDeque替代");
        
        System.out.println("\n5. CopyOnWriteArrayList:");
        System.out.println("   - 适用场景: 读多写少的并发场景，如缓存、监听器列表");
        System.out.println("   - 优点: 读操作无锁，线程安全，迭代器不会抛出ConcurrentModificationException");
        System.out.println("   - 缺点: 写操作开销大，内存占用大，数据一致性是最终一致性");
        
        System.out.println("\n推荐选择原则:");
        System.out.println("- 单线程环境: ArrayList(随机访问) 或 LinkedList(频繁插入删除)");
        System.out.println("- 多线程环境: ConcurrentHashMap + List 或 Collections.synchronizedList()");
        System.out.println("- 读多写少的并发场景: CopyOnWriteArrayList");
        System.out.println("- 栈结构需求: ArrayDeque 替代 Stack");
    }
}

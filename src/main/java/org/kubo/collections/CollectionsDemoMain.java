package org.kubo.collections;

import java.util.Scanner;

/**
 * Java集合类型演示主程序
 * 提供交互式菜单来运行各种集合类型的演示
 */
public class CollectionsDemoMain {

    public static void main(String[] args) {
        System.out.println("====================================================");
        System.out.println("          Java集合类型综合演示实验室");
        System.out.println("====================================================");
        System.out.println("本实验室演示Java中主要集合类型的特性、性能和使用场景");
        System.out.println();
        
        Scanner scanner = new Scanner(System.in);
        boolean continueRunning = true;
        
        while (continueRunning) {
            printMenu();
            
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                
                switch (choice) {
                    case 1:
                        System.out.println("\n正在运行List集合演示...");
                        ListCollectionDemo.run();
                        break;
                        
                    case 2:
                        System.out.println("\n正在运行Set集合演示...");
                        SetCollectionDemo.run();
                        break;
                        
                    case 3:
                        System.out.println("\n正在运行Map集合演示...");
                        MapCollectionDemo.run();
                        break;
                        
                    case 4:
                        System.out.println("\n正在运行Queue集合演示...");
                        QueueCollectionDemo.run();
                        break;
                        
                    case 5:
                        System.out.println("\n正在运行ArrayList vs LinkedList对比演示...");
                        ArrayVsLinkedListDemo.run();
                        break;
                        
                    case 6:
                        System.out.println("\n正在运行集合性能综合测试...");
                        CollectionPerformanceTest.run();
                        break;
                        
                    case 7:
                        System.out.println("\n正在运行所有演示...");
                        runAllDemos();
                        break;
                        
                    case 8:
                        printCollectionSummary();
                        break;
                        
                    case 0:
                        continueRunning = false;
                        System.out.println("\n感谢使用Java集合演示实验室！");
                        break;
                        
                    default:
                        System.out.println("\n无效选择，请重新输入！");
                        break;
                }
                
                if (continueRunning && choice != 8) {
                    System.out.println("\n按回车键继续...");
                    scanner.nextLine();
                }
                
            } catch (NumberFormatException e) {
                System.out.println("\n请输入有效的数字！");
            }
        }
        
        scanner.close();
    }

    /**
     * 打印主菜单
     */
    private static void printMenu() {
        System.out.println("\n====== 主菜单 ======");
        System.out.println("1. List集合演示 (ArrayList, LinkedList, Vector, Stack, CopyOnWriteArrayList)");
        System.out.println("2. Set集合演示 (HashSet, TreeSet, LinkedHashSet, EnumSet, ConcurrentSkipListSet)");
        System.out.println("3. Map集合演示 (HashMap, TreeMap, LinkedHashMap, Hashtable, ConcurrentHashMap)");
        System.out.println("4. Queue集合演示 (LinkedList, ArrayDeque, PriorityQueue, BlockingQueue)");
        System.out.println("5. ArrayList vs LinkedList 详细对比");
        System.out.println("6. 集合性能综合测试");
        System.out.println("7. 运行所有演示");
        System.out.println("8. 集合选择总结");
        System.out.println("0. 退出");
        System.out.print("\n请选择要运行的演示 (0-8): ");
    }

    /**
     * 运行所有演示
     */
    private static void runAllDemos() {
        System.out.println("\n开始运行所有集合演示...");
        
        // 运行所有演示，但跳过性能测试（太耗时）
        ListCollectionDemo.run();
        SetCollectionDemo.run();
        MapCollectionDemo.run();
        QueueCollectionDemo.run();
        ArrayVsLinkedListDemo.run();
        
        System.out.println("\n====================================================");
        System.out.println("          所有基础演示已完成！");
        System.out.println("====================================================");
        System.out.println("注意: 集合性能测试因为耗时较长，请单独运行（选项6）");
        
        printCollectionSummary();
    }

    /**
     * 打印集合选择总结
     */
    private static void printCollectionSummary() {
        System.out.println("\n==== Java集合选择速查表 ====");
        
        System.out.println("\n【List 线性表】");
        System.out.println("├─ ArrayList        → 动态数组，随机访问O(1)，适合读多写少");
        System.out.println("├─ LinkedList       → 双向链表，插入删除O(1)，适合频繁增删");
        System.out.println("├─ Vector           → 线程安全ArrayList，性能较差，不推荐");
        System.out.println("├─ Stack            → 栈结构，继承Vector，推荐用ArrayDeque替代");
        System.out.println("└─ CopyOnWriteArrayList → 线程安全，读多写少并发场景");
        
        System.out.println("\n【Set 集合】");
        System.out.println("├─ HashSet          → 哈希表，无序，O(1)操作，一般用途");
        System.out.println("├─ LinkedHashSet    → 保持插入顺序的HashSet");
        System.out.println("├─ TreeSet          → 红黑树，有序，O(log n)操作，支持范围查询");
        System.out.println("├─ EnumSet          → 枚举专用，位向量实现，极高性能");
        System.out.println("├─ CopyOnWriteArraySet → 线程安全，读多写少场景");
        System.out.println("└─ ConcurrentSkipListSet → 线程安全有序Set，高并发");
        
        System.out.println("\n【Map 映射】");
        System.out.println("├─ HashMap          → 哈希表，无序，O(1)操作，最常用");
        System.out.println("├─ LinkedHashMap    → 保持插入/访问顺序，可实现LRU缓存");
        System.out.println("├─ TreeMap          → 红黑树，有序，O(log n)操作，支持范围操作");
        System.out.println("├─ Hashtable        → 线程安全HashMap，性能差，不推荐");
        System.out.println("├─ ConcurrentHashMap → 线程安全，高并发性能，推荐");
        System.out.println("├─ WeakHashMap      → 弱引用键，防内存泄漏");
        System.out.println("├─ IdentityHashMap  → 引用相等性比较，特殊用途");
        System.out.println("├─ EnumMap          → 枚举键专用，数组实现，极高性能");
        System.out.println("└─ ConcurrentSkipListMap → 线程安全有序Map，高并发");
        
        System.out.println("\n【Queue 队列】");
        System.out.println("├─ ArrayDeque       → 数组双端队列，高性能，推荐替代Stack");
        System.out.println("├─ LinkedList       → 也实现Queue，但性能不如ArrayDeque");
        System.out.println("├─ PriorityQueue    → 优先队列，堆实现，无界");
        System.out.println("├─ ArrayBlockingQueue → 有界阻塞队列，数组实现");
        System.out.println("├─ LinkedBlockingQueue → 可选有界阻塞队列，链表实现");
        System.out.println("├─ PriorityBlockingQueue → 无界优先阻塞队列");
        System.out.println("├─ DelayQueue       → 延迟队列，用于定时任务");
        System.out.println("└─ SynchronousQueue → 同步队列，直接传递");
        
        System.out.println("\n【选择建议】");
        System.out.println("📋 一般用途: ArrayList, HashSet, HashMap, ArrayDeque");
        System.out.println("🔄 需要顺序: LinkedHashMap, LinkedHashSet, TreeMap, TreeSet");
        System.out.println("⚡ 高性能: ArrayList, HashSet, HashMap, ArrayDeque");
        System.out.println("🔒 线程安全: ConcurrentHashMap, CopyOnWriteArrayList, BlockingQueue");
        System.out.println("🚫 避免使用: Vector, Hashtable, Stack (有更好的替代品)");
        
        System.out.println("\n【记忆口诀】");
        System.out.println("Array快查找，Linked好插删");
        System.out.println("Hash无序快，Tree有序全");
        System.out.println("Concurrent并发强，Copy读多安");
        System.out.println("Priority有优先，Blocking会等待");
    }

    /**
     * 运行特定演示的静态方法（供其他类调用）
     */
    public static void runDemo() {
        System.out.println("运行Java集合演示...");
        runAllDemos();
    }
}

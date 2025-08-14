package org.kubo.collections;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public final class ArrayVsLinkedListDemo {

    private ArrayVsLinkedListDemo() {}

    public static void run() {
        System.out.println("\n==== 数组 / ArrayList 与 LinkedList 对比演示 ====");

        int elementCount = 100_000;
        int operationCount = 50_000;
        System.out.printf("数据规模：%,d，操作次数：%,d%n", elementCount, operationCount);

        // 预热，触发 JIT 编译
        warmup();

        // 1）随机访问：数组 vs 链表
        int[] intArray = createIntArray(elementCount);
        LinkedList<Integer> intLinkedList = createLinkedList(elementCount);
        int[] randomIndexes = createRandomIndexes(operationCount, elementCount);

        long tArrayRandom = measureMillis(() -> {
            long sum = 0;
            for (int idx : randomIndexes) {
                sum += intArray[idx];
            }
            blackhole(sum);
        });
        long tLinkedListRandom = measureMillis(() -> {
            long sum = 0;
            for (int idx : randomIndexes) {
                sum += getByIndex(intLinkedList, idx);
            }
            blackhole(sum);
        });
        System.out.printf("随机访问耗时：int[]=%dms，LinkedList=%dms%n", tArrayRandom, tLinkedListRandom);

        // 2）顺序遍历：数组 vs 链表
        long tArrayIter = measureMillis(() -> {
            long sum = 0;
            for (int v : intArray) {
                sum += v;
            }
            blackhole(sum);
        });
        long tLinkedListIter = measureMillis(() -> {
            long sum = 0;
            for (Integer v : intLinkedList) {
                sum += v;
            }
            blackhole(sum);
        });
        System.out.printf("顺序遍历耗时：int[]=%dms，LinkedList=%dms%n", tArrayIter, tLinkedListIter);

        // 3）中间插入：ArrayList vs LinkedList
        int inserts = 10_000; // 保持适中以便快速完成
        List<Integer> arrayList = new ArrayList<>();
        List<Integer> linkedList = new LinkedList<>();
        preload(arrayList, elementCount / 10);
        preload(linkedList, elementCount / 10);

        long tArrayListMiddleInsert = measureMillis(() -> {
            for (int i = 0; i < inserts; i++) {
                arrayList.add(arrayList.size() / 2, i);
            }
        });
        long tLinkedListMiddleInsert = measureMillis(() -> {
            for (int i = 0; i < inserts; i++) {
                linkedList.add(linkedList.size() / 2, i);
            }
        });
        System.out.printf("中间插入（%,d 次）：ArrayList=%dms，LinkedList=%dms%n", inserts, tArrayListMiddleInsert, tLinkedListMiddleInsert);

        // 4）头部操作（头部添加/删除）：ArrayList vs LinkedList
        int headOps = 20_000;
        List<Integer> arrayListHead = new ArrayList<>();
        List<Integer> linkedListHead = new LinkedList<>();

        long tArrayListHeadOps = measureMillis(() -> {
            for (int i = 0; i < headOps; i++) arrayListHead.add(0, i);
            for (int i = 0; i < headOps; i++) arrayListHead.remove(0);
        });
        long tLinkedListHeadOps = measureMillis(() -> {
            LinkedList<Integer> ll = (LinkedList<Integer>) linkedListHead;
            for (int i = 0; i < headOps; i++) ll.addFirst(i);
            for (int i = 0; i < headOps; i++) ll.removeFirst();
        });
        System.out.printf("头部增删（%,d 次）：ArrayList=%dms，LinkedList=%dms%n", headOps, tArrayListHeadOps, tLinkedListHeadOps);

        // 5）中间删除：ArrayList vs LinkedList
        List<Integer> arrayListRemove = new ArrayList<>();
        List<Integer> linkedListRemove = new LinkedList<>();
        preload(arrayListRemove, elementCount / 5);
        preload(linkedListRemove, elementCount / 5);
        int removeOps = 5_000;
        long tArrayListMiddleRemove = measureMillis(() -> {
            for (int i = 0; i < removeOps; i++) {
                arrayListRemove.remove(arrayListRemove.size() / 2);
            }
        });
        long tLinkedListMiddleRemove = measureMillis(() -> {
            for (int i = 0; i < removeOps; i++) {
                linkedListRemove.remove(linkedListRemove.size() / 2);
            }
        });
        System.out.printf("中间删除（%,d 次）：ArrayList=%dms，LinkedList=%dms%n", removeOps, tArrayListMiddleRemove, tLinkedListMiddleRemove);

        System.out.println("说明：");
        System.out.println("- 数组（int[]）随机访问为 O(1)，局部性好；LinkedList 随机访问为 O(n)。");
        System.out.println("- ArrayList 的中间插入/删除需要搬移元素为 O(n)；LinkedList 避免搬移，但定位到指定索引本身需要 O(n)。");
        System.out.println("- LinkedList 在头部插入/删除为 O(1)；ArrayList 在头部操作一般为 O(n)。");
        System.out.println("- 遍历数组/ArrayList 更利于缓存，通常更快；LinkedList 存在指针跳转开销。");
    }

    private static void warmup() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 10_000; i++) list.add(i);
        long s = 0;
        for (int i = 0; i < 10_000; i++) s += list.get(i);
        blackhole(s);
    }

    private static int[] createIntArray(int size) {
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) arr[i] = i;
        return arr;
    }

    private static LinkedList<Integer> createLinkedList(int size) {
        LinkedList<Integer> list = new LinkedList<>();
        for (int i = 0; i < size; i++) list.add(i);
        return list;
    }

    private static int[] createRandomIndexes(int count, int boundExclusive) {
        Random random = new Random(2025);
        int[] idx = new int[count];
        for (int i = 0; i < count; i++) idx[i] = random.nextInt(boundExclusive);
        return idx;
    }

    private static long measureMillis(Runnable runnable) {
        long start = System.nanoTime();
        runnable.run();
        return (System.nanoTime() - start) / 1_000_000L;
    }

    private static int getByIndex(LinkedList<Integer> list, int index) {
        // LinkedList.get(index) 为 O(n)；直接调用用于展示其代价
        return list.get(index);
    }

    private static void preload(List<Integer> list, int size) {
        for (int i = 0; i < size; i++) list.add(i);
    }

    private static void blackhole(long value) {
        if (value == Long.MIN_VALUE) {
            System.out.print("");
        }
    }
}



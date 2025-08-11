package org.kubo.collections;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public final class ArrayVsLinkedListDemo {

    private ArrayVsLinkedListDemo() {}

    public static void run() {
        System.out.println("\n==== Array / ArrayList vs LinkedList demo ====");

        int elementCount = 100_000;
        int operationCount = 50_000;
        System.out.printf("Data size: %,d, Operations: %,d%n", elementCount, operationCount);

        // Warmup to trigger JIT
        warmup();

        // 1) Random access: array vs LinkedList
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
        System.out.printf("Random access sum: array=%dms, linkedList=%dms%n", tArrayRandom, tLinkedListRandom);

        // 2) Sequential iteration: array vs LinkedList
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
        System.out.printf("Sequential iteration sum: array=%dms, linkedList=%dms%n", tArrayIter, tLinkedListIter);

        // 3) Insert in the middle: ArrayList vs LinkedList
        int inserts = 10_000; // keep moderate to finish quickly
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
        System.out.printf("Middle insert (%,d): arrayList=%dms, linkedList=%dms%n", inserts, tArrayListMiddleInsert, tLinkedListMiddleInsert);

        // 4) Head operations (add/remove at head): ArrayList vs LinkedList
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
        System.out.printf("Head add/remove (%,d): arrayList=%dms, linkedList=%dms%n", headOps, tArrayListHeadOps, tLinkedListHeadOps);

        // 5) Remove from middle: ArrayList vs LinkedList
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
        System.out.printf("Middle remove (%,d): arrayList=%dms, linkedList=%dms%n", removeOps, tArrayListMiddleRemove, tLinkedListMiddleRemove);

        System.out.println("Notes:");
        System.out.println("- Array (int[]) has O(1) random access and great locality; LinkedList random access is O(n).");
        System.out.println("- ArrayList middle ops cost O(n) due to shifting; LinkedList middle ops avoid shifting but locating index is O(n).");
        System.out.println("- LinkedList excels at head insert/remove (O(1)); ArrayList head ops are O(n).");
        System.out.println("- Iteration over arrays/ArrayList is cache-friendly; LinkedList has pointer chasing overhead.");
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
        // LinkedList.get(index) is O(n); calling directly to demonstrate cost
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



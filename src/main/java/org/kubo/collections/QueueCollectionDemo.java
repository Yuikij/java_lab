package org.kubo.collections;

import java.util.*;
import java.util.concurrent.*;

/**
 * Queue集合演示类
 * 演示LinkedList、PriorityQueue、ArrayDeque、DelayQueue、BlockingQueue等的特性和性能差异
 */
public class QueueCollectionDemo {

    /**
     * 用于演示的任务类
     */
    static class Task implements Comparable<Task> {
        private final String name;
        private final int priority;
        private final long timestamp;

        public Task(String name, int priority) {
            this.name = name;
            this.priority = priority;
            this.timestamp = System.currentTimeMillis();
        }

        @Override
        public int compareTo(Task other) {
            // 优先级高的排在前面（数字小的优先级高）
            return Integer.compare(this.priority, other.priority);
        }

        @Override
        public String toString() {
            return String.format("%s(优先级:%d)", name, priority);
        }

        public String getName() { return name; }
        public int getPriority() { return priority; }
        public long getTimestamp() { return timestamp; }
    }

    /**
     * 延迟任务类
     */
    static class DelayedTask implements Delayed {
        private final String name;
        private final long delayTime;
        private final long executeTime;

        public DelayedTask(String name, long delaySeconds) {
            this.name = name;
            this.delayTime = delaySeconds;
            this.executeTime = System.currentTimeMillis() + delaySeconds * 1000;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            long remaining = executeTime - System.currentTimeMillis();
            return unit.convert(remaining, TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed other) {
            return Long.compare(this.getDelay(TimeUnit.MILLISECONDS), 
                               other.getDelay(TimeUnit.MILLISECONDS));
        }

        @Override
        public String toString() {
            return String.format("%s(延迟%ds)", name, delayTime);
        }

        public String getName() { return name; }
    }

    /**
     * 运行Queue集合演示
     */
    public static void run() {
        System.out.println("\n==== Queue集合类型演示 ====");
        
        // 基本特性演示
        demonstrateBasicFeatures();
        
        // 双端队列演示
        dequeDemo();
        
        // 优先队列演示
        priorityQueueDemo();
        
        // 阻塞队列演示
        blockingQueueDemo();
        
        // 性能对比演示
        performanceComparison();
        
        // 使用场景总结
        usageSummary();
    }

    /**
     * 演示各种Queue的基本特性
     */
    private static void demonstrateBasicFeatures() {
        System.out.println("\n--- Queue基本特性演示 ---");
        
        // LinkedList作为Queue使用 - FIFO队列
        Queue<String> linkedListQueue = new LinkedList<>();
        linkedListQueue.offer("第一个");
        linkedListQueue.offer("第二个");
        linkedListQueue.offer("第三个");
        System.out.println("LinkedList队列: " + linkedListQueue);
        System.out.println("队列头部: " + linkedListQueue.peek());
        System.out.println("出队: " + linkedListQueue.poll());
        System.out.println("出队后队列: " + linkedListQueue);
        
        // ArrayDeque - 双端队列，性能更好
        Queue<String> arrayDeque = new ArrayDeque<>();
        arrayDeque.offer("元素A");
        arrayDeque.offer("元素B");
        arrayDeque.offer("元素C");
        System.out.println("ArrayDeque队列: " + arrayDeque);
        System.out.println("队列头部: " + arrayDeque.peek());
        System.out.println("出队: " + arrayDeque.poll());
        System.out.println("出队后队列: " + arrayDeque);
        
        // PriorityQueue - 优先队列，基于堆实现
        Queue<Integer> priorityQueue = new PriorityQueue<>();
        priorityQueue.offer(30);
        priorityQueue.offer(10);
        priorityQueue.offer(50);
        priorityQueue.offer(20);
        priorityQueue.offer(40);
        System.out.println("PriorityQueue: " + priorityQueue);
        System.out.println("注意: PriorityQueue的toString()不保证顺序");
        
        System.out.print("按优先级出队: ");
        while (!priorityQueue.isEmpty()) {
            System.out.print(priorityQueue.poll() + " ");
        }
        System.out.println();
        
        // Queue操作方法对比
        System.out.println("\nQueue操作方法对比:");
        Queue<String> testQueue = new ArrayDeque<>();
        testQueue.offer("测试元素");
        
        System.out.println("插入操作:");
        System.out.println("- add(): 插入元素，失败时抛出异常");
        System.out.println("- offer(): 插入元素，失败时返回false");
        
        System.out.println("删除操作:");
        System.out.println("- remove(): 删除并返回队列头，队列为空时抛出异常");
        System.out.println("- poll(): 删除并返回队列头，队列为空时返回null");
        System.out.println("poll()结果: " + testQueue.poll());
        System.out.println("再次poll()结果: " + testQueue.poll());
        
        System.out.println("检查操作:");
        System.out.println("- element(): 返回队列头，队列为空时抛出异常");
        System.out.println("- peek(): 返回队列头，队列为空时返回null");
        System.out.println("peek()结果: " + testQueue.peek());
    }

    /**
     * 双端队列演示
     */
    private static void dequeDemo() {
        System.out.println("\n--- Deque (双端队列) 演示 ---");
        
        // ArrayDeque作为双端队列
        Deque<String> deque = new ArrayDeque<>();
        
        // 在两端添加元素
        deque.addFirst("头部1");
        deque.addLast("尾部1");
        deque.addFirst("头部2");
        deque.addLast("尾部2");
        System.out.println("双端队列: " + deque);
        
        // 查看两端元素
        System.out.println("头部元素: " + deque.peekFirst());
        System.out.println("尾部元素: " + deque.peekLast());
        
        // 从两端移除元素
        System.out.println("从头部移除: " + deque.removeFirst());
        System.out.println("从尾部移除: " + deque.removeLast());
        System.out.println("移除后队列: " + deque);
        
        // LinkedList作为双端队列
        Deque<String> linkedDeque = new LinkedList<>();
        linkedDeque.push("栈底");   // 作为栈使用
        linkedDeque.push("中间");
        linkedDeque.push("栈顶");
        System.out.println("LinkedList作为栈: " + linkedDeque);
        System.out.println("弹出: " + linkedDeque.pop());
        System.out.println("弹出后: " + linkedDeque);
        
        // 演示Deque的不同使用模式
        System.out.println("\nDeque的使用模式:");
        Deque<String> multiUseDeque = new ArrayDeque<>();
        
        // 作为队列使用 (FIFO)
        multiUseDeque.addLast("队列1");
        multiUseDeque.addLast("队列2");
        multiUseDeque.addLast("队列3");
        System.out.println("作为队列: " + multiUseDeque);
        System.out.println("队列出队: " + multiUseDeque.removeFirst());
        
        // 作为栈使用 (LIFO)
        multiUseDeque.addFirst("栈1");
        multiUseDeque.addFirst("栈2");
        System.out.println("作为栈: " + multiUseDeque);
        System.out.println("栈弹出: " + multiUseDeque.removeFirst());
        
        System.out.println("最终状态: " + multiUseDeque);
    }

    /**
     * 优先队列演示
     */
    private static void priorityQueueDemo() {
        System.out.println("\n--- PriorityQueue (优先队列) 演示 ---");
        
        // 基本优先队列 - 自然排序
        PriorityQueue<Integer> numberPQ = new PriorityQueue<>();
        int[] numbers = {50, 20, 80, 10, 30, 70, 40};
        for (int num : numbers) {
            numberPQ.offer(num);
        }
        System.out.println("插入顺序: " + Arrays.toString(numbers));
        System.out.print("优先队列出队顺序: ");
        while (!numberPQ.isEmpty()) {
            System.out.print(numberPQ.poll() + " ");
        }
        System.out.println();
        
        // 自定义比较器的优先队列
        PriorityQueue<String> stringPQ = new PriorityQueue<>(
            (a, b) -> Integer.compare(b.length(), a.length()) // 按长度降序
        );
        String[] words = {"java", "python", "c", "javascript", "go", "rust"};
        for (String word : words) {
            stringPQ.offer(word);
        }
        System.out.println("字符串插入顺序: " + Arrays.toString(words));
        System.out.print("按长度降序出队: ");
        while (!stringPQ.isEmpty()) {
            System.out.print(stringPQ.poll() + " ");
        }
        System.out.println();
        
        // 任务优先队列演示
        System.out.println("\n任务优先队列演示:");
        PriorityQueue<Task> taskQueue = new PriorityQueue<>();
        taskQueue.offer(new Task("普通任务", 5));
        taskQueue.offer(new Task("紧急任务", 1));
        taskQueue.offer(new Task("低优先级任务", 9));
        taskQueue.offer(new Task("高优先级任务", 2));
        taskQueue.offer(new Task("中等任务", 5));
        
        System.out.println("任务队列大小: " + taskQueue.size());
        System.out.println("按优先级执行任务:");
        while (!taskQueue.isEmpty()) {
            Task task = taskQueue.poll();
            System.out.println("执行: " + task);
        }
        
        // 堆操作演示
        System.out.println("\n堆结构特性演示:");
        PriorityQueue<Integer> heap = new PriorityQueue<>();
        Collections.addAll(heap, 4, 2, 8, 1, 9, 3, 7, 5);
        
        System.out.println("堆内容: " + heap);
        System.out.println("堆顶元素(最小): " + heap.peek());
        
        // 注意：PriorityQueue不保证除堆顶外的元素顺序
        System.out.println("迭代器遍历顺序(不保证有序): ");
        for (Integer num : heap) {
            System.out.print(num + " ");
        }
        System.out.println();
        
        // 转换为有序数组
        Integer[] sortedArray = heap.toArray(new Integer[0]);
        Arrays.sort(sortedArray);
        System.out.println("排序后的数组: " + Arrays.toString(sortedArray));
    }

    /**
     * 阻塞队列演示
     */
    private static void blockingQueueDemo() {
        System.out.println("\n--- BlockingQueue (阻塞队列) 演示 ---");
        
        // ArrayBlockingQueue - 有界阻塞队列
        System.out.println("1. ArrayBlockingQueue演示:");
        BlockingQueue<String> arrayBlockingQueue = new ArrayBlockingQueue<>(3);
        
        try {
            arrayBlockingQueue.put("元素1");
            arrayBlockingQueue.put("元素2");
            arrayBlockingQueue.put("元素3");
            System.out.println("ArrayBlockingQueue: " + arrayBlockingQueue);
            System.out.println("队列已满，size: " + arrayBlockingQueue.size());
            
            // 尝试非阻塞添加
            boolean added = arrayBlockingQueue.offer("元素4");
            System.out.println("尝试添加第4个元素: " + added);
            
            // 尝试超时添加
            boolean addedWithTimeout = arrayBlockingQueue.offer("元素4", 100, TimeUnit.MILLISECONDS);
            System.out.println("100ms超时添加: " + addedWithTimeout);
            
            // 取出元素
            String taken = arrayBlockingQueue.take();
            System.out.println("取出元素: " + taken);
            System.out.println("取出后队列: " + arrayBlockingQueue);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // LinkedBlockingQueue - 可选有界阻塞队列
        System.out.println("\n2. LinkedBlockingQueue演示:");
        BlockingQueue<String> linkedBlockingQueue = new LinkedBlockingQueue<>(2);
        linkedBlockingQueue.offer("任务A");
        linkedBlockingQueue.offer("任务B");
        System.out.println("LinkedBlockingQueue: " + linkedBlockingQueue);
        System.out.println("队列大小: " + linkedBlockingQueue.size());
        System.out.println("剩余容量: " + linkedBlockingQueue.remainingCapacity());
        
        // PriorityBlockingQueue - 无界优先阻塞队列
        System.out.println("\n3. PriorityBlockingQueue演示:");
        BlockingQueue<Task> priorityBlockingQueue = new PriorityBlockingQueue<>();
        priorityBlockingQueue.offer(new Task("普通任务", 5));
        priorityBlockingQueue.offer(new Task("紧急任务", 1));
        priorityBlockingQueue.offer(new Task("低优先级任务", 8));
        
        System.out.println("PriorityBlockingQueue大小: " + priorityBlockingQueue.size());
        try {
            System.out.println("取出最高优先级任务: " + priorityBlockingQueue.take());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // DelayQueue - 延迟队列
        System.out.println("\n4. DelayQueue演示:");
        DelayQueue<DelayedTask> delayQueue = new DelayQueue<>();
        
        long startTime = System.currentTimeMillis();
        delayQueue.offer(new DelayedTask("1秒后执行", 1));
        delayQueue.offer(new DelayedTask("3秒后执行", 3));
        delayQueue.offer(new DelayedTask("2秒后执行", 2));
        
        System.out.println("DelayQueue大小: " + delayQueue.size());
        System.out.println("开始处理延迟任务...");
        
        try {
            // 模拟处理延迟任务
            for (int i = 0; i < 3; i++) {
                DelayedTask task = delayQueue.take(); // 阻塞直到任务可以执行
                long elapsed = System.currentTimeMillis() - startTime;
                System.out.printf("执行任务: %s (实际延迟: %dms)%n", 
                                task.getName(), elapsed);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // SynchronousQueue - 同步队列
        System.out.println("\n5. SynchronousQueue演示:");
        BlockingQueue<String> synchronousQueue = new SynchronousQueue<>();
        System.out.println("SynchronousQueue容量: " + synchronousQueue.remainingCapacity());
        System.out.println("SynchronousQueue是否为空: " + synchronousQueue.isEmpty());
        
        // 创建一个生产者线程
        Thread producer = new Thread(() -> {
            try {
                System.out.println("生产者: 准备发送数据...");
                synchronousQueue.put("同步数据");
                System.out.println("生产者: 数据已发送");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        // 创建一个消费者线程
        Thread consumer = new Thread(() -> {
            try {
                Thread.sleep(1000); // 延迟1秒再接收
                System.out.println("消费者: 准备接收数据...");
                String data = synchronousQueue.take();
                System.out.println("消费者: 接收到数据: " + data);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        producer.start();
        consumer.start();
        
        try {
            producer.join();
            consumer.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 性能对比演示
     */
    private static void performanceComparison() {
        System.out.println("\n--- Queue性能对比演示 ---");
        
        int elementCount = 100000;
        
        // 准备测试数据
        List<String> testData = new ArrayList<>();
        for (int i = 0; i < elementCount; i++) {
            testData.add("Element_" + i);
        }
        
        System.out.printf("测试数据规模: %,d 元素%n", elementCount);
        
        // 1. 入队性能测试
        System.out.println("\n1. 入队性能测试:");
        
        long startTime = System.nanoTime();
        Queue<String> linkedListQueue = new LinkedList<>();
        for (String item : testData) {
            linkedListQueue.offer(item);
        }
        long linkedListEnqueueTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        Queue<String> arrayDeque = new ArrayDeque<>();
        for (String item : testData) {
            arrayDeque.offer(item);
        }
        long arrayDequeEnqueueTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        Queue<String> priorityQueue = new PriorityQueue<>();
        for (String item : testData) {
            priorityQueue.offer(item);
        }
        long priorityQueueEnqueueTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        BlockingQueue<String> arrayBlockingQueue = new ArrayBlockingQueue<>(elementCount);
        for (String item : testData) {
            arrayBlockingQueue.offer(item);
        }
        long arrayBlockingQueueEnqueueTime = System.nanoTime() - startTime;
        
        System.out.printf("LinkedList入队: %,d ns%n", linkedListEnqueueTime);
        System.out.printf("ArrayDeque入队: %,d ns (%.1fx faster)%n", 
                         arrayDequeEnqueueTime, (double)linkedListEnqueueTime / arrayDequeEnqueueTime);
        System.out.printf("PriorityQueue入队: %,d ns (%.1fx slower)%n", 
                         priorityQueueEnqueueTime, (double)priorityQueueEnqueueTime / linkedListEnqueueTime);
        System.out.printf("ArrayBlockingQueue入队: %,d ns (%.1fx slower)%n", 
                         arrayBlockingQueueEnqueueTime, (double)arrayBlockingQueueEnqueueTime / linkedListEnqueueTime);
        
        // 2. 出队性能测试
        System.out.println("\n2. 出队性能测试:");
        
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
        
        startTime = System.nanoTime();
        while (!arrayBlockingQueue.isEmpty()) {
            arrayBlockingQueue.poll();
        }
        long arrayBlockingQueueDequeueTime = System.nanoTime() - startTime;
        
        System.out.printf("LinkedList出队: %,d ns%n", linkedListDequeueTime);
        System.out.printf("ArrayDeque出队: %,d ns (%.1fx faster)%n", 
                         arrayDequeDequeueTime, (double)linkedListDequeueTime / arrayDequeDequeueTime);
        System.out.printf("PriorityQueue出队: %,d ns (%.1fx slower)%n", 
                         priorityQueueDequeueTime, (double)priorityQueueDequeueTime / linkedListDequeueTime);
        System.out.printf("ArrayBlockingQueue出队: %,d ns (%.1fx slower)%n", 
                         arrayBlockingQueueDequeueTime, (double)arrayBlockingQueueDequeueTime / linkedListDequeueTime);
        
        // 3. 随机访问性能测试（仅对支持的队列）
        System.out.println("\n3. 队列查看性能测试:");
        
        // 重新填充队列
        for (String item : testData) {
            linkedListQueue.offer(item);
            arrayDeque.offer(item);
        }
        
        startTime = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            linkedListQueue.peek();
        }
        long linkedListPeekTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            arrayDeque.peek();
        }
        long arrayDequePeekTime = System.nanoTime() - startTime;
        
        System.out.printf("LinkedList peek: %,d ns%n", linkedListPeekTime);
        System.out.printf("ArrayDeque peek: %,d ns (%.1fx faster)%n", 
                         arrayDequePeekTime, (double)linkedListPeekTime / arrayDequePeekTime);
    }

    /**
     * 使用场景总结
     */
    private static void usageSummary() {
        System.out.println("\n--- Queue集合使用场景总结 ---");
        
        System.out.println("1. LinkedList (作为Queue):");
        System.out.println("   - 适用场景: 简单的FIFO队列，双端队列需求");
        System.out.println("   - 优点: 实现了Deque接口，支持两端操作");
        System.out.println("   - 缺点: 性能相对较差，内存开销大");
        
        System.out.println("\n2. ArrayDeque:");
        System.out.println("   - 适用场景: 高性能的双端队列，栈的替代实现");
        System.out.println("   - 优点: 高性能，内存效率好，支持两端操作");
        System.out.println("   - 缺点: 非线程安全，不支持null元素");
        
        System.out.println("\n3. PriorityQueue:");
        System.out.println("   - 适用场景: 需要按优先级处理的任务队列，堆排序");
        System.out.println("   - 优点: 自动按优先级排序，O(log n)插入删除");
        System.out.println("   - 缺点: 无界队列，非线程安全，不保证同优先级顺序");
        
        System.out.println("\n4. ArrayBlockingQueue:");
        System.out.println("   - 适用场景: 生产者消费者模式，有界缓冲区");
        System.out.println("   - 优点: 线程安全，有界控制，支持阻塞操作");
        System.out.println("   - 缺点: 固定容量，可能发生阻塞");
        
        System.out.println("\n5. LinkedBlockingQueue:");
        System.out.println("   - 适用场景: 生产者消费者模式，可选有界队列");
        System.out.println("   - 优点: 线程安全，可选容量限制，两个锁分离");
        System.out.println("   - 缺点: 内存开销较大");
        
        System.out.println("\n6. PriorityBlockingQueue:");
        System.out.println("   - 适用场景: 多线程环境的优先级任务处理");
        System.out.println("   - 优点: 线程安全，无界，自动排序");
        System.out.println("   - 缺点: 无界可能导致内存问题，性能相对较差");
        
        System.out.println("\n7. DelayQueue:");
        System.out.println("   - 适用场景: 定时任务，缓存过期，延迟处理");
        System.out.println("   - 优点: 线程安全，自动延迟处理");
        System.out.println("   - 缺点: 无界，只能存储Delayed元素");
        
        System.out.println("\n8. SynchronousQueue:");
        System.out.println("   - 适用场景: 线程间直接传递数据，没有缓冲需求");
        System.out.println("   - 优点: 线程安全，零容量，直接传递");
        System.out.println("   - 缺点: 必须有对应的生产者/消费者");
        
        System.out.println("\n推荐选择原则:");
        System.out.println("- 一般队列: ArrayDeque");
        System.out.println("- 双端队列: ArrayDeque");
        System.out.println("- 栈结构: ArrayDeque (替代Stack)");
        System.out.println("- 优先队列: PriorityQueue");
        System.out.println("- 线程安全队列: BlockingQueue系列");
        System.out.println("- 生产者消费者: ArrayBlockingQueue 或 LinkedBlockingQueue");
        System.out.println("- 延迟处理: DelayQueue");
        System.out.println("- 直接传递: SynchronousQueue");
    }
}

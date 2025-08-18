package org.kubo.concurrent.memory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Happens-Before规则演示
 * 
 * Happens-Before是Java内存模型中的核心概念，定义了操作间的偏序关系。
 * 如果操作A happens-before 操作B，那么A的结果对B可见，且A的执行顺序在B之前。
 * 
 * Java内存模型定义的Happens-Before规则：
 * 1. 程序顺序规则：同一线程内，按照程序代码顺序
 * 2. 监视器锁规则：unlock操作 happens-before 后续对同一锁的lock操作  
 * 3. volatile变量规则：对volatile变量的写 happens-before 后续对该变量的读
 * 4. 线程启动规则：Thread.start() happens-before 该线程的所有操作
 * 5. 线程终止规则：线程的所有操作 happens-before 其他线程检测到该线程终止
 * 6. 线程中断规则：interrupt() happens-before 检测到中断
 * 7. 对象终结规则：对象构造完成 happens-before finalize()方法
 * 8. 传递性规则：A hb B, B hb C => A hb C
 * 
 * @author kubo
 */
public class HappensBeforeDemo {
    
    public static void main(String[] args) {
        System.out.println("===============================================");
        System.out.println("         Happens-Before规则演示");
        System.out.println("===============================================\n");
        
        // 1. 程序顺序规则
        demonstrateProgramOrderRule();
        sleep(1000);
        
        // 2. 监视器锁规则
        demonstrateMonitorLockRule();
        sleep(1000);
        
        // 3. volatile变量规则
        demonstrateVolatileVariableRule();
        sleep(1000);
        
        // 4. 线程启动规则
        demonstrateThreadStartRule();
        sleep(1000);
        
        // 5. 线程终止规则
        demonstrateThreadTerminationRule();
        sleep(1000);
        
        // 6. 线程中断规则
        demonstrateThreadInterruptRule();
        sleep(1000);
        
        // 7. 传递性规则
        demonstrateTransitivityRule();
        sleep(1000);
        
        // 综合示例：生产者-消费者模式中的Happens-Before
        demonstrateProducerConsumerHappensBeforeee();
    }
    
    /**
     * 程序顺序规则演示
     * 同一线程内，按照程序代码顺序，前面的操作 happens-before 后面的操作
     */
    private static void demonstrateProgramOrderRule() {
        System.out.println("1️⃣ 程序顺序规则演示");
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("同一线程内的操作按程序顺序执行");
        System.out.println();
        
        ProgramOrderExample example = new ProgramOrderExample();
        
        Thread thread = new Thread(() -> {
            System.out.println("执行操作序列...");
            
            // 这些操作在同一线程内，具有happens-before关系
            example.operation1();  // A
            example.operation2();  // B: A happens-before B
            example.operation3();  // C: B happens-before C
            
            System.out.println("最终状态: " + example.getState());
        }, "ProgramOrder-Thread");
        
        thread.start();
        
        try {
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("程序顺序规则演示完成\n");
    }
    
    /**
     * 监视器锁规则演示
     * unlock操作 happens-before 后续对同一锁的lock操作
     */
    private static void demonstrateMonitorLockRule() {
        System.out.println("2️⃣ 监视器锁规则演示");
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("unlock操作 happens-before 后续的lock操作");
        System.out.println();
        
        MonitorLockExample example = new MonitorLockExample();
        
        // 第一个线程：获取锁，修改数据，释放锁
        Thread thread1 = new Thread(() -> {
            example.updateData("Thread-1", 100);
            System.out.println("Thread-1: 数据更新完成，释放锁");
        }, "Thread-1");
        
        // 第二个线程：获取锁，读取数据
        Thread thread2 = new Thread(() -> {
            sleep(100); // 确保Thread-1先执行
            String data = example.readData();
            System.out.println("Thread-2: 读取到数据 = " + data);
        }, "Thread-2");
        
        thread1.start();
        thread2.start();
        
        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("监视器锁规则演示完成\n");
    }
    
    /**
     * volatile变量规则演示
     * 对volatile变量的写操作 happens-before 后续对该变量的读操作
     */
    private static void demonstrateVolatileVariableRule() {
        System.out.println("3️⃣ volatile变量规则演示");
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("对volatile变量的写 happens-before 后续对该变量的读");
        System.out.println();
        
        VolatileVariableExample example = new VolatileVariableExample();
        
        // 写线程
        Thread writer = new Thread(() -> {
            example.prepareData();
            System.out.println("Writer: 数据准备完成");
            example.setReady(true); // volatile写操作
            System.out.println("Writer: 设置ready标志为true");
        }, "Writer");
        
        // 读线程
        Thread reader = new Thread(() -> {
            while (!example.isReady()) { // volatile读操作
                // 等待写线程完成
                Thread.yield();
            }
            System.out.println("Reader: 检测到ready为true");
            String data = example.getData();
            System.out.println("Reader: 读取到数据 = " + data);
        }, "Reader");
        
        reader.start();
        sleep(100);
        writer.start();
        
        try {
            writer.join();
            reader.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("volatile变量规则演示完成\n");
    }
    
    /**
     * 线程启动规则演示
     * Thread.start()的调用 happens-before 该线程内的所有操作
     */
    private static void demonstrateThreadStartRule() {
        System.out.println("4️⃣ 线程启动规则演示");
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("Thread.start()调用 happens-before 线程内的所有操作");
        System.out.println();
        
        ThreadStartExample example = new ThreadStartExample();
        
        // 主线程准备数据
        example.prepareDataInMainThread();
        System.out.println("主线程: 数据准备完成");
        
        Thread workerThread = new Thread(() -> {
            // 这里能看到主线程在start()之前的所有操作结果
            String data = example.getDataFromWorkerThread();
            System.out.println("工作线程: 看到主线程准备的数据 = " + data);
        }, "Worker");
        
        System.out.println("主线程: 启动工作线程");
        workerThread.start(); // start()操作 happens-before 工作线程的所有操作
        
        try {
            workerThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("线程启动规则演示完成\n");
    }
    
    /**
     * 线程终止规则演示
     * 线程的所有操作 happens-before 其他线程检测到该线程终止
     */
    private static void demonstrateThreadTerminationRule() {
        System.out.println("5️⃣ 线程终止规则演示");
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("线程的所有操作 happens-before 其他线程检测到该线程终止");
        System.out.println();
        
        ThreadTerminationExample example = new ThreadTerminationExample();
        
        Thread workerThread = new Thread(() -> {
            example.doWork();
            System.out.println("工作线程: 工作完成");
        }, "Worker");
        
        System.out.println("主线程: 启动工作线程");
        workerThread.start();
        
        try {
            workerThread.join(); // 等待线程终止
            // join()返回后，能看到工作线程的所有操作结果
            String result = example.getResult();
            System.out.println("主线程: 工作线程终止后，获取结果 = " + result);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("线程终止规则演示完成\n");
    }
    
    /**
     * 线程中断规则演示
     * interrupt()操作 happens-before 检测到中断事件
     */
    private static void demonstrateThreadInterruptRule() {
        System.out.println("6️⃣ 线程中断规则演示");
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("interrupt()调用 happens-before 检测到中断");
        System.out.println();
        
        ThreadInterruptExample example = new ThreadInterruptExample();
        
        Thread workerThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    example.doWork();
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                System.out.println("工作线程: 检测到中断，准备结束");
                String finalState = example.getFinalState();
                System.out.println("工作线程: 最终状态 = " + finalState);
            }
        }, "Worker");
        
        workerThread.start();
        
        sleep(500);
        
        example.prepareForInterrupt();
        System.out.println("主线程: 准备中断工作线程");
        workerThread.interrupt(); // interrupt() happens-before 工作线程检测到中断
        
        try {
            workerThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("线程中断规则演示完成\n");
    }
    
    /**
     * 传递性规则演示
     * 如果A happens-before B，B happens-before C，则A happens-before C
     */
    private static void demonstrateTransitivityRule() {
        System.out.println("7️⃣ 传递性规则演示");
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("A hb B, B hb C => A hb C");
        System.out.println();
        
        TransitivityExample example = new TransitivityExample();
        
        Thread thread1 = new Thread(() -> {
            example.operationA();
            System.out.println("Thread-1: 执行操作A完成");
        }, "Thread-1");
        
        Thread thread2 = new Thread(() -> {
            example.waitForA();
            example.operationB();
            System.out.println("Thread-2: 执行操作B完成");
        }, "Thread-2");
        
        Thread thread3 = new Thread(() -> {
            example.waitForB();
            example.operationC();
            System.out.println("Thread-3: 执行操作C完成，能看到A的结果");
        }, "Thread-3");
        
        thread1.start();
        sleep(100);
        thread2.start();
        sleep(100);
        thread3.start();
        
        try {
            thread1.join();
            thread2.join();
            thread3.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("传递性规则演示完成\n");
    }
    
    /**
     * 生产者-消费者模式中的Happens-Before综合演示
     */
    private static void demonstrateProducerConsumerHappensBeforeee() {
        System.out.println("8️⃣ 生产者-消费者 Happens-Before综合演示");
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("综合运用多种Happens-Before规则");
        System.out.println();
        
        ProducerConsumerExample example = new ProducerConsumerExample();
        
        // 启动消费者线程
        Thread consumer = new Thread(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    String item = example.consume();
                    System.out.println("消费者: 消费了 " + item);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Consumer");
        
        // 启动生产者线程
        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    example.produce("Item-" + i);
                    System.out.println("生产者: 生产了 Item-" + i);
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Producer");
        
        consumer.start();
        producer.start();
        
        try {
            producer.join();
            consumer.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("生产者-消费者 Happens-Before演示完成\n");
    }
    
    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

/**
 * 程序顺序规则示例
 */
class ProgramOrderExample {
    private int state = 0;
    
    public void operation1() {
        state += 10;
        System.out.println("  操作1: state = " + state);
    }
    
    public void operation2() {
        state *= 2;
        System.out.println("  操作2: state = " + state);
    }
    
    public void operation3() {
        state -= 5;
        System.out.println("  操作3: state = " + state);
    }
    
    public int getState() {
        return state;
    }
}

/**
 * 监视器锁规则示例
 */
class MonitorLockExample {
    private final Object lock = new Object();
    private String data = "initial";
    
    public void updateData(String newData, int value) {
        synchronized (lock) { // 获取锁
            data = newData + "-" + value;
            System.out.println("  更新数据为: " + data);
        } // 释放锁
    }
    
    public String readData() {
        synchronized (lock) { // 获取锁（happens-before前面的unlock）
            System.out.println("  读取数据: " + data);
            return data;
        } // 释放锁
    }
}

/**
 * volatile变量规则示例
 */
class VolatileVariableExample {
    private String data = "uninitialized";
    private volatile boolean ready = false;
    
    public void prepareData() {
        data = "prepared-data-" + System.currentTimeMillis();
        System.out.println("  准备数据: " + data);
    }
    
    public void setReady(boolean ready) {
        this.ready = ready; // volatile写
    }
    
    public boolean isReady() {
        return ready; // volatile读
    }
    
    public String getData() {
        return data;
    }
}

/**
 * 线程启动规则示例
 */
class ThreadStartExample {
    private String data = "uninitialized";
    
    public void prepareDataInMainThread() {
        data = "main-thread-data-" + System.currentTimeMillis();
    }
    
    public String getDataFromWorkerThread() {
        return data;
    }
}

/**
 * 线程终止规则示例
 */
class ThreadTerminationExample {
    private String result = "unfinished";
    
    public void doWork() {
        System.out.println("  执行复杂计算...");
        long sum = 0;
        for (int i = 0; i < 1000000; i++) {
            sum += i;
        }
        result = "completed-" + sum;
    }
    
    public String getResult() {
        return result;
    }
}

/**
 * 线程中断规则示例
 */
class ThreadInterruptExample {
    private String state = "working";
    private int workCount = 0;
    
    public void doWork() {
        workCount++;
        if (workCount % 10 == 0) {
            System.out.println("  完成工作单元: " + workCount);
        }
    }
    
    public void prepareForInterrupt() {
        state = "prepare-for-interrupt";
    }
    
    public String getFinalState() {
        return state + "-count:" + workCount;
    }
}

/**
 * 传递性规则示例
 */
class TransitivityExample {
    private volatile boolean aCompleted = false;
    private volatile boolean bCompleted = false;
    private String resultA = "unfinished";
    
    public void operationA() {
        resultA = "A-completed-" + System.currentTimeMillis();
        System.out.println("  操作A完成，结果: " + resultA);
        aCompleted = true; // A完成的写操作 happens-before 后续读取aCompleted
    }
    
    public void waitForA() {
        while (!aCompleted) { // 读取aCompleted
            Thread.yield();
        }
        System.out.println("  检测到A完成");
    }
    
    public void operationB() {
        System.out.println("  操作B开始，能看到A的结果: " + resultA);
        bCompleted = true; // B完成的写操作 happens-before 后续读取bCompleted
    }
    
    public void waitForB() {
        while (!bCompleted) { // 读取bCompleted
            Thread.yield();
        }
        System.out.println("  检测到B完成");
    }
    
    public void operationC() {
        // 由于传递性：A hb B, B hb C => A hb C
        // 所以C能看到A的结果
        System.out.println("  操作C开始，通过传递性能看到A的结果: " + resultA);
    }
}

/**
 * 生产者-消费者Happens-Before综合示例
 */
class ProducerConsumerExample {
    private final BlockingQueue<String> queue = new ArrayBlockingQueue<>(10);
    private final AtomicInteger produceCount = new AtomicInteger(0);
    private final AtomicInteger consumeCount = new AtomicInteger(0);
    
    public void produce(String item) throws InterruptedException {
        queue.put(item); // BlockingQueue内部使用锁，保证happens-before
        int count = produceCount.incrementAndGet();
        System.out.println("  生产计数: " + count);
    }
    
    public String consume() throws InterruptedException {
        String item = queue.take(); // BlockingQueue内部使用锁，保证happens-before
        int count = consumeCount.incrementAndGet();
        System.out.println("  消费计数: " + count);
        return item;
    }
}

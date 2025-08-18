package org.kubo.concurrent.memory;



/**
 * 可见性演示类
 * 
 * 可见性是指当多个线程访问同一个变量时，一个线程修改了这个变量的值，
 * 其他线程能够立即看得到修改的值。
 * 
 * Java内存模型(JMM)中，每个线程都有自己的工作内存，线程对变量的操作
 * 都是在工作内存中进行的，然后再同步到主内存中。
 * 
 * @author kubo
 */
public class VisibilityDemo {
    
    // 普通变量，没有可见性保证
    private boolean normalFlag = false;
    private int normalValue = 0;
    
    // volatile变量，保证可见性
    private volatile boolean volatileFlag = false;
    private volatile int volatileValue = 0;
    
    // 使用synchronized的变量，保证可见性
    private boolean synchronizedFlag = false;
    private int synchronizedValue = 0;
    
    public void runVisibilityTest() {
        System.out.println("可见性测试说明：");
        System.out.println("- 启动两个线程，一个负责修改变量，另一个负责读取变量");
        System.out.println("- 观察读取线程能否及时看到写入线程的修改");
        System.out.println();
        
        // 测试普通变量的可见性问题
        testNormalVariableVisibility();
        
        // 等待一段时间再进行下一个测试
        sleep(2000);
        
        // 测试volatile变量的可见性
        testVolatileVariableVisibility();
        
        // 等待一段时间再进行下一个测试
        sleep(2000);
        
        // 测试synchronized的可见性
        testSynchronizedVariableVisibility();
    }
    
    /**
     * 测试普通变量的可见性问题
     * 在某些情况下，读取线程可能永远看不到写入线程的修改
     */
    private void testNormalVariableVisibility() {
        System.out.println("【普通变量可见性测试】");
        
        // 重置变量
        normalFlag = false;
        normalValue = 0;
        
        // 创建读取线程
        Thread readerThread = new Thread(() -> {
            System.out.println("  读取线程启动，等待normalFlag变为true...");
            
            // 循环等待flag变为true
            while (!normalFlag) {
                // 空循环，可能因为JIT优化而看不到flag的变化
                // JIT编译器可能会将这个循环优化为死循环
            }
            
            System.out.println("  读取线程：检测到normalFlag = true");
            System.out.println("  读取线程：normalValue = " + normalValue);
            System.out.println("  💡 如果看到这条消息，说明读取线程成功检测到了变化");
        }, "普通变量读取线程");
        
        // 创建写入线程
        Thread writerThread = new Thread(() -> {
            sleep(1000); // 等待1秒
            System.out.println("  写入线程：设置normalValue = 100");
            normalValue = 100;
            
            System.out.println("  写入线程：设置normalFlag = true");
            normalFlag = true;
            System.out.println("  写入线程：修改完成");
        }, "普通变量写入线程");
        
        // 启动线程
        readerThread.start();
        writerThread.start();
        
        // 等待最多3秒
        try {
            readerThread.join(3000);
            writerThread.join(1000);
            
            if (readerThread.isAlive()) {
                System.out.println("  ❌ 读取线程在3秒内未检测到变化，可能存在可见性问题");
                System.out.println("  💡 分析：普通变量缺乏可见性保证，读取线程可能一直使用缓存值");
                readerThread.interrupt();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println();
    }
    
    /**
     * 测试volatile变量的可见性
     * volatile关键字保证变量的可见性
     */
    private void testVolatileVariableVisibility() {
        System.out.println("【volatile变量可见性测试】");
        
        // 重置变量
        volatileFlag = false;
        volatileValue = 0;
        
        // 创建读取线程
        Thread readerThread = new Thread(() -> {
            System.out.println("  读取线程启动，等待volatileFlag变为true...");
            
            // 循环等待flag变为true
            while (!volatileFlag) {
                // volatile保证每次都从主内存读取最新值
            }
            
            System.out.println("  读取线程：检测到volatileFlag = true");
            System.out.println("  读取线程：volatileValue = " + volatileValue);
            System.out.println("  ✓ volatile确保了可见性");
        }, "volatile变量读取线程");
        
        // 创建写入线程
        Thread writerThread = new Thread(() -> {
            sleep(500); // 等待0.5秒
            System.out.println("  写入线程：设置volatileValue = 200");
            volatileValue = 200;
            
            System.out.println("  写入线程：设置volatileFlag = true");
            volatileFlag = true;
            System.out.println("  写入线程：修改完成");
        }, "volatile变量写入线程");
        
        // 启动线程
        readerThread.start();
        writerThread.start();
        
        // 等待线程完成
        try {
            readerThread.join(2000);
            writerThread.join(1000);
            
            if (readerThread.isAlive()) {
                System.out.println("  ❌ 意外：volatile变量测试超时");
                readerThread.interrupt();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println();
    }
    
    /**
     * 测试synchronized的可见性
     * synchronized关键字也能保证可见性
     */
    private void testSynchronizedVariableVisibility() {
        System.out.println("【synchronized变量可见性测试】");
        
        // 重置变量
        synchronizedFlag = false;
        synchronizedValue = 0;
        
        final Object lock = new Object();
        
        // 创建读取线程
        Thread readerThread = new Thread(() -> {
            System.out.println("  读取线程启动，等待synchronizedFlag变为true...");
            
            boolean flag;
            do {
                synchronized (lock) {
                    flag = synchronizedFlag; // 在同步块中读取，保证可见性
                }
                if (!flag) {
                    sleep(10); // 短暂休眠，避免过度占用CPU
                }
            } while (!flag);
            
            int value;
            synchronized (lock) {
                value = synchronizedValue;
            }
            
            System.out.println("  读取线程：检测到synchronizedFlag = true");
            System.out.println("  读取线程：synchronizedValue = " + value);
            System.out.println("  ✓ synchronized确保了可见性");
        }, "synchronized变量读取线程");
        
        // 创建写入线程
        Thread writerThread = new Thread(() -> {
            sleep(500); // 等待0.5秒
            
            synchronized (lock) {
                System.out.println("  写入线程：设置synchronizedValue = 300");
                synchronizedValue = 300;
                
                System.out.println("  写入线程：设置synchronizedFlag = true");
                synchronizedFlag = true;
                System.out.println("  写入线程：修改完成");
            }
        }, "synchronized变量写入线程");
        
        // 启动线程
        readerThread.start();
        writerThread.start();
        
        // 等待线程完成
        try {
            readerThread.join(2000);
            writerThread.join(1000);
            
            if (readerThread.isAlive()) {
                System.out.println("  ❌ 意外：synchronized变量测试超时");
                readerThread.interrupt();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println();
    }
    
    /**
     * 工具方法：线程休眠
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

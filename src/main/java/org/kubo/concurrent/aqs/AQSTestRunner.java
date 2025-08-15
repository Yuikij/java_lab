package org.kubo.concurrent.aqs;

/**
 * AQS测试运行器 - 统一运行所有AQS相关的演示
 */
public class AQSTestRunner {
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("    AQS (AbstractQueuedSynchronizer)");
        System.out.println("         实现原理和使用演示");
        System.out.println("========================================\n");
        
        try {
            // 1. AQS基本演示
            System.out.println("【1. AQS基本实现演示】");
            System.out.println("运行AQSDemo...\n");
            AQSDemo.main(args);
            
            System.out.println("\n" + "=".repeat(50) + "\n");
            
            // 2. AQS使用示例
            System.out.println("【2. AQS标准类使用示例】");
            System.out.println("运行AQSUsageExamples...\n");
            AQSUsageExamples.main(args);
            
            System.out.println("\n" + "=".repeat(50) + "\n");
            
            // 3. AQS原理分析
            System.out.println("【3. AQS原理深度分析】");
            System.out.println("运行AQSPrincipleAnalysis...\n");
            AQSPrincipleAnalysis.main(args);
            
            System.out.println("\n" + "=".repeat(50) + "\n");
            
            // 4. AQS性能测试
            System.out.println("【4. AQS性能测试】");
            System.out.println("运行AQSPerformanceTest...\n");
            AQSPerformanceTest.main(args);
            
        } catch (Exception e) {
            System.err.println("运行AQS演示时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n========================================");
        System.out.println("         AQS演示完成");
        System.out.println("========================================");
        
        printAQSSummary();
    }
    
    /**
     * 打印AQS知识点总结
     */
    private static void printAQSSummary() {
        System.out.println("\n【AQS核心知识点总结】");
        System.out.println();
        
        System.out.println("1. AQS是什么？");
        System.out.println("   AbstractQueuedSynchronizer是Java并发包的核心基础框架");
        System.out.println("   为实现同步器提供了通用的基础设施");
        System.out.println();
        
        System.out.println("2. AQS核心组件：");
        System.out.println("   • state: volatile int类型，表示同步状态");
        System.out.println("   • 等待队列: FIFO双向链表，管理等待线程");
        System.out.println("   • 条件队列: 支持条件变量的单向链表");
        System.out.println();
        
        System.out.println("3. AQS支持的模式：");
        System.out.println("   • 独占模式(Exclusive): 如ReentrantLock");
        System.out.println("   • 共享模式(Shared): 如CountDownLatch、Semaphore");
        System.out.println();
        
        System.out.println("4. AQS基于的同步器：");
        System.out.println("   • ReentrantLock: 可重入独占锁");
        System.out.println("   • ReentrantReadWriteLock: 读写锁");
        System.out.println("   • CountDownLatch: 倒计时门闩");
        System.out.println("   • Semaphore: 信号量");
        System.out.println("   • CyclicBarrier: 循环屏障");
        System.out.println();
        
        System.out.println("5. AQS核心方法：");
        System.out.println("   • tryAcquire/tryRelease: 独占模式的获取/释放");
        System.out.println("   • tryAcquireShared/tryReleaseShared: 共享模式的获取/释放");
        System.out.println("   • isHeldExclusively: 检查是否被当前线程独占");
        System.out.println();
        
        System.out.println("6. AQS核心算法：");
        System.out.println("   获取: tryAcquire → 失败则入队 → park等待 → 唤醒后重试");
        System.out.println("   释放: tryRelease → 成功则unpark后继节点");
        System.out.println();
        
        System.out.println("7. AQS优势：");
        System.out.println("   • 高性能: 基于CAS和LockSupport");
        System.out.println("   • 可扩展: 模板方法模式，易于定制");
        System.out.println("   • 功能丰富: 支持中断、超时、公平性等");
        System.out.println("   • 资源管理: 自动处理线程阻塞和唤醒");
    }
}

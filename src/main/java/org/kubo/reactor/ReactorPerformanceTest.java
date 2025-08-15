package org.kubo.reactor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Reactor线程模型性能对比测试
 * 
 * 通过相同的负载测试，对比三种Reactor模型的性能表现：
 * 1. 吞吐量（TPS - Transactions Per Second）
 * 2. 响应时间
 * 3. 资源利用率
 * 
 * 测试场景：
 * - 模拟大量并发连接
 * - 每个连接进行多次读写操作
 * - 统计处理时间和吞吐量
 */
public class ReactorPerformanceTest {
    private static final Logger logger = Logger.getLogger(ReactorPerformanceTest.class.getName());
    
    // 测试参数
    private static final int TEST_CLIENTS = 100;        // 模拟客户端数量
    private static final int OPERATIONS_PER_CLIENT = 50; // 每个客户端的操作次数
    private static final int TOTAL_OPERATIONS = TEST_CLIENTS * OPERATIONS_PER_CLIENT;
    
    public static void main(String[] args) {
        logger.info("==========================================");
        logger.info("        Reactor性能对比测试开始");
        logger.info("==========================================");
        logger.info(String.format("测试参数：客户端数=%d，每客户端操作数=%d，总操作数=%d", 
                TEST_CLIENTS, OPERATIONS_PER_CLIENT, TOTAL_OPERATIONS));
        
        try {
            // 测试单Reactor单线程模型
            testSingleThreadReactorPerformance();
            
            Thread.sleep(3000); // 间隔
            
            // 测试单Reactor多线程模型
            testMultiThreadReactorPerformance();
            
            Thread.sleep(3000); // 间隔
            
            // 测试主从Reactor多线程模型
            testMasterSlaveReactorPerformance();
            
        } catch (InterruptedException e) {
            logger.warning("测试被中断");
            Thread.currentThread().interrupt();
        }
        
        logger.info("==========================================");
        logger.info("        Reactor性能对比测试结束");
        logger.info("==========================================");
    }
    
    /**
     * 测试单Reactor单线程模型性能
     */
    private static void testSingleThreadReactorPerformance() throws InterruptedException {
        logger.info("\n=== 单Reactor单线程模型性能测试 ===");
        
        SingleThreadReactor reactor = new SingleThreadReactor("Performance-SingleThread");
        
        // 在独立线程中启动reactor
        Thread reactorThread = new Thread(reactor::start);
        reactorThread.start();
        Thread.sleep(500); // 等待启动
        
        long startTime = System.currentTimeMillis();
        
        // 提交所有测试事件
        for (int clientId = 1; clientId <= TEST_CLIENTS; clientId++) {
            String client = "Client-" + clientId;
            
            // 连接事件
            reactor.submitEvent(new Event(Event.EventType.ACCEPT, "连接", client));
            
            // 读写事件
            for (int op = 1; op <= OPERATIONS_PER_CLIENT; op++) {
                reactor.submitEvent(new Event(Event.EventType.READ, "数据-" + op, client));
                reactor.submitEvent(new Event(Event.EventType.WRITE, "响应-" + op, client));
            }
        }
        
        // 等待所有事件处理完成（简单等待，实际应该用更精确的方法）
        while (reactor.getPendingEventCount() > 0) {
            Thread.sleep(100);
        }
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        reactor.stop();
        reactorThread.interrupt();
        
        // 计算性能指标
        double tps = (double) TOTAL_OPERATIONS / (totalTime / 1000.0);
        
        logger.info("--- 单Reactor单线程模型测试结果 ---");
        logger.info(String.format("总耗时: %d ms", totalTime));
        logger.info(String.format("平均TPS: %.2f operations/sec", tps));
        logger.info(String.format("平均响应时间: %.2f ms/operation", (double) totalTime / TOTAL_OPERATIONS));
        logger.info("特点：顺序处理，无并发开销，但无法利用多核");
    }
    
    /**
     * 测试单Reactor多线程模型性能
     */
    private static void testMultiThreadReactorPerformance() throws InterruptedException {
        logger.info("\n=== 单Reactor多线程模型性能测试 ===");
        
        MultiThreadReactor reactor = new MultiThreadReactor("Performance-MultiThread", 8);
        reactor.start();
        Thread.sleep(500); // 等待启动
        
        long startTime = System.currentTimeMillis();
        
        // 提交所有测试事件
        for (int clientId = 1; clientId <= TEST_CLIENTS; clientId++) {
            String client = "Client-" + clientId;
            
            // 连接事件
            reactor.submitEvent(new Event(Event.EventType.ACCEPT, "连接", client));
            
            // 读写事件
            for (int op = 1; op <= OPERATIONS_PER_CLIENT; op++) {
                reactor.submitEvent(new Event(Event.EventType.READ, "数据-" + op, client));
                reactor.submitEvent(new Event(Event.EventType.WRITE, "响应-" + op, client));
            }
        }
        
        // 等待所有事件处理完成
        while (reactor.getPendingEventCount() > 0) {
            Thread.sleep(100);
        }
        
        // 额外等待，确保工作线程处理完成
        Thread.sleep(2000);
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        reactor.stop();
        
        // 计算性能指标
        double tps = (double) TOTAL_OPERATIONS / (totalTime / 1000.0);
        
        logger.info("--- 单Reactor多线程模型测试结果 ---");
        logger.info(String.format("总耗时: %d ms", totalTime));
        logger.info(String.format("平均TPS: %.2f operations/sec", tps));
        logger.info(String.format("平均响应时间: %.2f ms/operation", (double) totalTime / TOTAL_OPERATIONS));
        logger.info("特点：并行处理业务逻辑，充分利用多核，但Reactor可能成为瓶颈");
    }
    
    /**
     * 测试主从Reactor多线程模型性能
     */
    private static void testMasterSlaveReactorPerformance() throws InterruptedException {
        logger.info("\n=== 主从Reactor多线程模型性能测试 ===");
        
        MasterSlaveReactor reactor = new MasterSlaveReactor("Performance-MasterSlave", 4, 8);
        reactor.start();
        Thread.sleep(500); // 等待启动
        
        long startTime = System.currentTimeMillis();
        
        // 使用线程池模拟并发客户端
        ExecutorService clientSimulator = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(TEST_CLIENTS);
        
        for (int clientId = 1; clientId <= TEST_CLIENTS; clientId++) {
            final String client = "Client-" + clientId;
            
            clientSimulator.submit(() -> {
                try {
                    // 模拟客户端连接
                    reactor.simulateClientConnection(client);
                    Thread.sleep(10); // 连接延迟
                    
                    // 模拟数据交互
                    for (int op = 1; op <= OPERATIONS_PER_CLIENT; op++) {
                        reactor.simulateDataRead(client, "数据-" + op);
                        reactor.simulateDataWrite(client, "响应-" + op);
                        Thread.sleep(5); // 操作间隔
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // 等待所有客户端模拟完成
        latch.await(30, TimeUnit.SECONDS);
        clientSimulator.shutdown();
        
        // 额外等待，确保所有事件处理完成
        Thread.sleep(3000);
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        reactor.stop();
        
        // 计算性能指标
        double tps = (double) TOTAL_OPERATIONS / (totalTime / 1000.0);
        
        logger.info("--- 主从Reactor多线程模型测试结果 ---");
        logger.info(String.format("总耗时: %d ms", totalTime));
        logger.info(String.format("平均TPS: %.2f operations/sec", tps));
        logger.info(String.format("平均响应时间: %.2f ms/operation", (double) totalTime / TOTAL_OPERATIONS));
        logger.info("特点：最高并发能力，连接和I/O分离处理，最适合高并发场景");
        
        // 性能对比总结
        printPerformanceSummary();
    }
    
    /**
     * 打印性能对比总结
     */
    private static void printPerformanceSummary() {
        logger.info("\n========== Reactor模型性能对比总结 ==========");
        logger.info("1. 单Reactor单线程模型：");
        logger.info("   - 适用场景：连接数少、业务逻辑简单");
        logger.info("   - 优点：实现简单、无并发问题、内存占用少");
        logger.info("   - 缺点：无法利用多核、容易被阻塞");
        
        logger.info("\n2. 单Reactor多线程模型：");
        logger.info("   - 适用场景：中等并发、业务逻辑复杂");
        logger.info("   - 优点：充分利用多核、I/O与业务分离");
        logger.info("   - 缺点：单Reactor可能成为瓶颈");
        
        logger.info("\n3. 主从Reactor多线程模型：");
        logger.info("   - 适用场景：高并发、大量连接");
        logger.info("   - 优点：最高性能、良好扩展性、负载均衡");
        logger.info("   - 缺点：实现复杂、调优困难");
        
        logger.info("\n推荐选择：");
        logger.info("- 小型应用：单Reactor单线程");
        logger.info("- 中型应用：单Reactor多线程");
        logger.info("- 大型高并发应用：主从Reactor多线程");
        logger.info("============================================");
    }
}

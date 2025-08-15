package org.kubo.reactor;

import java.util.logging.Logger;

/**
 * Reactor线程模型综合演示
 * 
 * 本演示展示了三种主要的Reactor线程模型：
 * 1. 单Reactor单线程模型
 * 2. 单Reactor多线程模型  
 * 3. 主从Reactor多线程模型
 * 
 * 通过对比演示，可以清楚地看到每种模型的特点和适用场景
 */
public class ReactorDemo {
    private static final Logger logger = Logger.getLogger(ReactorDemo.class.getName());
    
    public static void main(String[] args) {
        logger.info("========================================");
        logger.info("      Reactor线程模型演示开始");
        logger.info("========================================");
        
        try {
            // 演示单Reactor单线程模型
            demonstrateSingleThreadReactor();
            
            Thread.sleep(2000); // 间隔
            
            // 演示单Reactor多线程模型
            demonstrateMultiThreadReactor();
            
            Thread.sleep(2000); // 间隔
            
            // 演示主从Reactor多线程模型
            demonstrateMasterSlaveReactor();
            
        } catch (InterruptedException e) {
            logger.warning("演示被中断");
            Thread.currentThread().interrupt();
        }
        
        logger.info("========================================");
        logger.info("      Reactor线程模型演示结束");
        logger.info("========================================");
    }
    
    /**
     * 演示单Reactor单线程模型
     */
    private static void demonstrateSingleThreadReactor() throws InterruptedException {
        logger.info("\n=== 单Reactor单线程模型演示 ===");
        logger.info("特点：所有事件在一个线程中顺序处理");
        logger.info("优点：简单、无并发问题");
        logger.info("缺点：无法利用多核、可能阻塞");
        
        SingleThreadReactor reactor = new SingleThreadReactor("Demo-SingleThread");
        
        // 在新线程中启动reactor，避免阻塞主线程
        Thread reactorThread = new Thread(() -> reactor.start());
        reactorThread.start();
        
        // 等待reactor启动
        Thread.sleep(500);
        
        // 提交一些测试事件
        logger.info("提交测试事件...");
        reactor.submitEvent(new Event(Event.EventType.ACCEPT, "客户端-001连接", "Client-001"));
        reactor.submitEvent(new Event(Event.EventType.READ, "HELLO from Client-001", "Client-001"));
        reactor.submitEvent(new Event(Event.EventType.WRITE, "Welcome Client-001", "Client-001"));
        reactor.submitEvent(new Event(Event.EventType.ACCEPT, "客户端-002连接", "Client-002"));
        reactor.submitEvent(new Event(Event.EventType.READ, "DATA from Client-002", "Client-002"));
        
        // 让事件处理一段时间
        Thread.sleep(3000);
        
        // 停止reactor
        reactor.stop();
        reactorThread.interrupt();
        
        // 打印统计信息
        reactor.printStatistics();
        
        logger.info("=== 单Reactor单线程模型演示完成 ===\n");
    }
    
    /**
     * 演示单Reactor多线程模型
     */
    private static void demonstrateMultiThreadReactor() throws InterruptedException {
        logger.info("=== 单Reactor多线程模型演示 ===");
        logger.info("特点：Reactor线程负责I/O，工作线程池负责业务处理");
        logger.info("优点：充分利用多核、I/O和业务处理分离");
        logger.info("缺点：单Reactor可能成为瓶颈");
        
        MultiThreadReactor reactor = new MultiThreadReactor("Demo-MultiThread", 4);
        
        // 启动reactor
        reactor.start();
        
        // 等待启动完成
        Thread.sleep(500);
        
        // 提交更多测试事件，展示并发处理能力
        logger.info("提交测试事件，展示并发处理...");
        
        for (int i = 1; i <= 8; i++) {
            String clientId = "Client-" + String.format("%03d", i);
            reactor.submitEvent(new Event(Event.EventType.ACCEPT, clientId + "连接", clientId));
            reactor.submitEvent(new Event(Event.EventType.READ, "PING from " + clientId, clientId));
            reactor.submitEvent(new Event(Event.EventType.WRITE, "PONG to " + clientId, clientId));
            
            // 稍微间隔一下，避免事件过于密集
            Thread.sleep(100);
        }
        
        // 让事件处理一段时间
        Thread.sleep(4000);
        
        // 停止reactor
        reactor.stop();
        
        // 打印统计信息
        reactor.printStatistics();
        
        logger.info("=== 单Reactor多线程模型演示完成 ===\n");
    }
    
    /**
     * 演示主从Reactor多线程模型
     */
    private static void demonstrateMasterSlaveReactor() throws InterruptedException {
        logger.info("=== 主从Reactor多线程模型演示 ===");
        logger.info("特点：主Reactor处理连接，从Reactor处理I/O，工作线程处理业务");
        logger.info("优点：最高的并发处理能力，良好的负载均衡");
        logger.info("缺点：实现复杂，调优困难");
        
        MasterSlaveReactor reactor = new MasterSlaveReactor("Demo-MasterSlave", 3, 6);
        
        // 启动reactor系统
        reactor.start();
        
        // 等待启动完成
        Thread.sleep(500);
        
        // 模拟大量客户端连接和数据交互
        logger.info("模拟大量客户端连接和数据交互...");
        
        // 模拟客户端连接
        for (int i = 1; i <= 10; i++) {
            String clientId = "Client-" + String.format("%03d", i);
            reactor.simulateClientConnection(clientId);
            Thread.sleep(50); // 连接间隔
        }
        
        Thread.sleep(1000); // 等待连接处理完成
        
        // 模拟数据读写
        for (int round = 1; round <= 3; round++) {
            logger.info(String.format("第 %d 轮数据交互", round));
            
            for (int i = 1; i <= 10; i++) {
                String clientId = "Client-" + String.format("%03d", i);
                
                // 模拟客户端发送数据
                reactor.simulateDataRead(clientId, String.format("DATA-R%d from %s", round, clientId));
                
                // 模拟服务器发送响应
                reactor.simulateDataWrite(clientId, String.format("RESPONSE-R%d to %s", round, clientId));
                
                Thread.sleep(30); // 数据交互间隔
            }
            
            Thread.sleep(1000); // 轮次间隔
        }
        
        // 让事件处理完成
        Thread.sleep(3000);
        
        // 停止reactor系统
        reactor.stop();
        
        // 打印统计信息
        reactor.printStatistics();
        
        logger.info("=== 主从Reactor多线程模型演示完成 ===\n");
    }
}

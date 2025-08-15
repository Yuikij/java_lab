package org.kubo.reactor;

import org.kubo.reactor.handlers.AcceptHandler;
import org.kubo.reactor.handlers.ReadHandler;
import org.kubo.reactor.handlers.WriteHandler;

import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * 单Reactor多线程模型演示
 * 
 * 特性和原理：
 * 1. 一个线程运行事件循环（Reactor线程）
 * 2. 事件处理在独立的线程池中进行
 * 3. Reactor线程负责I/O事件的监听和分发
 * 4. Worker线程池负责具体的业务逻辑处理
 * 
 * 优点：
 * - 能够充分利用多核CPU进行业务处理
 * - Reactor线程不会被业务逻辑阻塞
 * - 可以处理更多的并发连接
 * - 业务处理和I/O处理分离
 * 
 * 缺点：
 * - 实现相对复杂
 * - 需要考虑线程安全问题
 * - 线程间通信开销
 * - 单个Reactor仍然可能成为瓶颈
 */
public class MultiThreadReactor {
    private static final Logger logger = Logger.getLogger(MultiThreadReactor.class.getName());
    
    private final EventDemultiplexer demultiplexer;
    private final ThreadSafeDispatcher dispatcher;
    private final ExecutorService workerThreadPool;
    private final Thread reactorThread;
    private volatile boolean running;
    private final String reactorName;
    private final int workerThreadCount;
    
    public MultiThreadReactor(String name, int workerThreadCount) {
        this.reactorName = name;
        this.workerThreadCount = workerThreadCount;
        this.demultiplexer = new EventDemultiplexer(name + "-Demultiplexer");
        this.dispatcher = new ThreadSafeDispatcher(name + "-Dispatcher");
        
        // 创建工作线程池
        this.workerThreadPool = Executors.newFixedThreadPool(
                workerThreadCount,
                r -> {
                    Thread t = new Thread(r, name + "-Worker-" + System.currentTimeMillis());
                    t.setDaemon(false); // 非守护线程
                    return t;
                }
        );
        
        // 创建Reactor线程
        this.reactorThread = new Thread(this::eventLoop, name + "-Reactor");
        this.running = false;
        
        // 初始化事件处理器
        initializeHandlers();
        
        logger.info(String.format("[%s] 单Reactor多线程模型初始化完成，工作线程数: %d", 
                reactorName, workerThreadCount));
    }
    
    /**
     * 初始化各种事件处理器
     */
    private void initializeHandlers() {
        // 注册连接接受处理器
        AcceptHandler acceptHandler = new AcceptHandler(reactorName + "-AcceptHandler");
        dispatcher.registerHandler(Event.EventType.ACCEPT, acceptHandler);
        
        // 注册读取处理器
        ReadHandler readHandler = new ReadHandler(reactorName + "-ReadHandler");
        dispatcher.registerHandler(Event.EventType.READ, readHandler);
        
        // 注册写入处理器
        WriteHandler writeHandler = new WriteHandler(reactorName + "-WriteHandler");
        dispatcher.registerHandler(Event.EventType.WRITE, writeHandler);
        
        logger.info(String.format("[%s] 事件处理器注册完成，共注册 %d 个处理器", 
                reactorName, dispatcher.getHandlerCount()));
    }
    
    /**
     * 启动Reactor
     */
    public void start() {
        if (running) {
            logger.warning(String.format("[%s] Reactor已经在运行中", reactorName));
            return;
        }
        
        running = true;
        demultiplexer.start();
        
        logger.info(String.format("[%s] 启动单Reactor多线程模型", reactorName));
        logger.info(String.format("[%s] Reactor线程负责事件监听和分发", reactorName));
        logger.info(String.format("[%s] 工作线程池负责业务处理，线程数: %d", reactorName, workerThreadCount));
        
        // 启动Reactor线程
        reactorThread.start();
        
        logger.info(String.format("[%s] Reactor线程已启动: %s", reactorName, reactorThread.getName()));
    }
    
    /**
     * 停止Reactor
     */
    public void stop() {
        if (!running) {
            logger.warning(String.format("[%s] Reactor未在运行", reactorName));
            return;
        }
        
        logger.info(String.format("[%s] 开始停止单Reactor多线程模型", reactorName));
        
        running = false;
        demultiplexer.stop();
        
        // 中断Reactor线程
        reactorThread.interrupt();
        
        // 关闭工作线程池
        workerThreadPool.shutdown();
        try {
            if (!workerThreadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                logger.warning(String.format("[%s] 工作线程池未能在5秒内正常关闭，强制关闭", reactorName));
                workerThreadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.warning(String.format("[%s] 等待线程池关闭时被中断", reactorName));
            workerThreadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        // 等待Reactor线程结束
        try {
            reactorThread.join(3000);
            logger.info(String.format("[%s] Reactor线程已结束", reactorName));
        } catch (InterruptedException e) {
            logger.warning(String.format("[%s] 等待Reactor线程结束时被中断", reactorName));
            Thread.currentThread().interrupt();
        }
        
        logger.info(String.format("[%s] 单Reactor多线程模型已停止", reactorName));
    }
    
    /**
     * 事件循环 - 运行在独立的Reactor线程中
     */
    private void eventLoop() {
        logger.info(String.format("[%s] Reactor线程开始事件循环: %s", 
                reactorName, Thread.currentThread().getName()));
        
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                // 1. 等待事件发生
                logger.fine(String.format("[%s] [Reactor线程] 等待事件发生...", reactorName));
                Event event = demultiplexer.waitForEvent(1, TimeUnit.SECONDS);
                
                if (event != null) {
                    // 2. 将事件提交到工作线程池进行处理
                    logger.info(String.format("[%s] [Reactor线程] 接收到事件: %s，提交到工作线程池", 
                            reactorName, event.getType()));
                    
                    // 将事件处理任务提交到线程池
                    CompletableFuture<Void> processingFuture = CompletableFuture.runAsync(
                            () -> processEventInWorkerThread(event),
                            workerThreadPool
                    );
                    
                    // 可以选择等待处理完成或异步处理
                    processingFuture.whenComplete((result, throwable) -> {
                        if (throwable != null) {
                            logger.severe(String.format("[%s] 工作线程处理事件时发生异常: %s", 
                                    reactorName, throwable.getMessage()));
                        } else {
                            logger.fine(String.format("[%s] 事件处理完成: %s", 
                                    reactorName, event.getType()));
                        }
                    });
                    
                } else {
                    // 超时，继续下一轮循环
                    logger.finest(String.format("[%s] [Reactor线程] 等待事件超时", reactorName));
                }
                
            } catch (InterruptedException e) {
                logger.info(String.format("[%s] [Reactor线程] 事件循环被中断，准备退出", reactorName));
                Thread.currentThread().interrupt();
                break;
                
            } catch (Exception e) {
                logger.severe(String.format("[%s] [Reactor线程] 事件循环中发生异常: %s", 
                        reactorName, e.getMessage()));
                e.printStackTrace();
            }
        }
        
        logger.info(String.format("[%s] [Reactor线程] 事件循环结束", reactorName));
    }
    
    /**
     * 在工作线程中处理事件
     */
    private void processEventInWorkerThread(Event event) {
        String currentThread = Thread.currentThread().getName();
        logger.info(String.format("[%s] [%s] 开始处理事件: %s", 
                reactorName, currentThread, event.getType()));
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 使用线程安全的分发器处理事件
            dispatcher.dispatch(event);
            
        } catch (Exception e) {
            logger.severe(String.format("[%s] [%s] 处理事件时发生异常: %s", 
                    reactorName, currentThread, e.getMessage()));
            e.printStackTrace();
        } finally {
            long endTime = System.currentTimeMillis();
            logger.info(String.format("[%s] [%s] 事件处理完成，耗时: %d ms", 
                    reactorName, currentThread, endTime - startTime));
        }
    }
    
    /**
     * 提交事件到Reactor进行处理
     */
    public void submitEvent(Event event) {
        if (running) {
            demultiplexer.addEvent(event);
            logger.info(String.format("[%s] 事件已提交: %s", reactorName, event.getType()));
        } else {
            logger.warning(String.format("[%s] Reactor未运行，无法提交事件: %s", 
                    reactorName, event.getType()));
        }
    }
    
    /**
     * 线程安全的分发器
     */
    private static class ThreadSafeDispatcher extends Dispatcher {
        public ThreadSafeDispatcher(String name) {
            super(name);
        }
        
        @Override
        public synchronized void dispatch(Event event) {
            super.dispatch(event);
        }
    }
    
    /**
     * 获取统计信息
     */
    public void printStatistics() {
        logger.info(String.format("[%s] === Reactor统计信息 ===", reactorName));
        logger.info(String.format("[%s] 运行状态: %s", reactorName, running ? "运行中" : "已停止"));
        logger.info(String.format("[%s] Reactor线程: %s", reactorName, reactorThread.getName()));
        logger.info(String.format("[%s] 工作线程数: %d", reactorName, workerThreadCount));
        logger.info(String.format("[%s] 待处理事件数: %d", reactorName, demultiplexer.getEventCount()));
        logger.info(String.format("[%s] 线程池状态: %s", reactorName, 
                workerThreadPool.isShutdown() ? "已关闭" : "运行中"));
        logger.info(String.format("[%s] ========================", reactorName));
    }
    
    /**
     * 获取当前待处理事件数量
     */
    public int getPendingEventCount() {
        return demultiplexer.getEventCount();
    }
    
    /**
     * 检查Reactor是否正在运行
     */
    public boolean isRunning() {
        return running;
    }
    
    public String getReactorName() {
        return reactorName;
    }
}

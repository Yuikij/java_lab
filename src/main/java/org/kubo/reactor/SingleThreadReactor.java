package org.kubo.reactor;

import org.kubo.reactor.handlers.AcceptHandler;
import org.kubo.reactor.handlers.ReadHandler;
import org.kubo.reactor.handlers.WriteHandler;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * 单Reactor单线程模型演示
 * 
 * 特性和原理：
 * 1. 只有一个线程运行事件循环
 * 2. 所有I/O操作都在同一个线程中处理
 * 3. 简单易理解，没有并发问题
 * 4. 适用于连接数较少、处理逻辑简单的场景
 * 
 * 优点：
 * - 没有多线程竞争问题，无需考虑线程安全
 * - 实现简单，调试容易
 * - 内存消耗少
 * 
 * 缺点：
 * - 无法充分利用多核CPU
 * - 如果某个处理器阻塞，整个系统都会阻塞
 * - 不适合CPU密集型任务
 * - 并发处理能力有限
 */
public class SingleThreadReactor {
    private static final Logger logger = Logger.getLogger(SingleThreadReactor.class.getName());
    
    private final EventDemultiplexer demultiplexer;
    private final Dispatcher dispatcher;
    private volatile boolean running;
    private final String reactorName;
    
    public SingleThreadReactor(String name) {
        this.reactorName = name;
        this.demultiplexer = new EventDemultiplexer(name + "-Demultiplexer");
        this.dispatcher = new Dispatcher(name + "-Dispatcher");
        this.running = false;
        
        // 初始化事件处理器
        initializeHandlers();
        
        logger.info(String.format("[%s] 单Reactor单线程模型初始化完成", reactorName));
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
     * 启动Reactor - 开始事件循环
     */
    public void start() {
        if (running) {
            logger.warning(String.format("[%s] Reactor已经在运行中", reactorName));
            return;
        }
        
        running = true;
        demultiplexer.start();
        
        logger.info(String.format("[%s] 启动单Reactor单线程模型", reactorName));
        logger.info(String.format("[%s] 开始事件循环 - 在主线程中处理所有事件", reactorName));
        
        // 事件循环 - 这是Reactor模式的核心
        eventLoop();
    }
    
    /**
     * 停止Reactor
     */
    public void stop() {
        if (!running) {
            logger.warning(String.format("[%s] Reactor未在运行", reactorName));
            return;
        }
        
        running = false;
        demultiplexer.stop();
        
        logger.info(String.format("[%s] 单Reactor单线程模型已停止", reactorName));
    }
    
    /**
     * 事件循环 - Reactor模式的核心逻辑
     * 在单线程中循环处理所有事件
     */
    private void eventLoop() {
        logger.info(String.format("[%s] 进入事件循环", reactorName));
        
        while (running) {
            try {
                // 1. 等待事件发生（阻塞等待）
                logger.info(String.format("[%s] 等待事件发生...", reactorName));
                Event event = demultiplexer.waitForEvent(1, TimeUnit.SECONDS);
                
                if (event != null) {
                    // 2. 分发事件到对应的处理器
                    logger.info(String.format("[%s] 在主线程中处理事件: %s", reactorName, event.getType()));
                    dispatcher.dispatch(event);
                    
                } else {
                    // 超时，检查是否需要继续运行
                    logger.fine(String.format("[%s] 等待事件超时，继续下一轮循环", reactorName));
                }
                
            } catch (InterruptedException e) {
                // 被中断，优雅退出
                logger.info(String.format("[%s] 事件循环被中断，准备退出", reactorName));
                Thread.currentThread().interrupt();
                break;
                
            } catch (Exception e) {
                // 处理其他异常，避免事件循环意外退出
                logger.severe(String.format("[%s] 事件循环中发生异常: %s", reactorName, e.getMessage()));
                e.printStackTrace();
            }
        }
        
        logger.info(String.format("[%s] 事件循环结束", reactorName));
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
    
    /**
     * 获取统计信息
     */
    public void printStatistics() {
        logger.info(String.format("[%s] === Reactor统计信息 ===", reactorName));
        logger.info(String.format("[%s] 运行状态: %s", reactorName, running ? "运行中" : "已停止"));
        logger.info(String.format("[%s] 待处理事件数: %d", reactorName, getPendingEventCount()));
        logger.info(String.format("[%s] 注册的处理器数: %d", reactorName, dispatcher.getHandlerCount()));
        logger.info(String.format("[%s] ========================", reactorName));
    }
}

package org.kubo.reactor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * 事件分离器 - Reactor模式的核心组件
 * 负责监听和分离各种I/O事件，将事件放入队列中等待处理
 * 在真实的网络编程中，这通常对应于select、poll、epoll等系统调用
 */
public class EventDemultiplexer {
    private static final Logger logger = Logger.getLogger(EventDemultiplexer.class.getName());
    
    // 事件队列，用于存储待处理的事件
    private final BlockingQueue<Event> eventQueue;
    private volatile boolean running;
    private final String demultiplexerName;
    
    public EventDemultiplexer(String name) {
        this.demultiplexerName = name;
        this.eventQueue = new LinkedBlockingQueue<>();
        this.running = false;
        logger.info(String.format("[%s] 事件分离器初始化完成", demultiplexerName));
    }
    
    /**
     * 启动事件分离器
     */
    public void start() {
        this.running = true;
        logger.info(String.format("[%s] 事件分离器启动", demultiplexerName));
    }
    
    /**
     * 停止事件分离器
     */
    public void stop() {
        this.running = false;
        logger.info(String.format("[%s] 事件分离器停止", demultiplexerName));
    }
    
    /**
     * 添加事件到队列中
     * 模拟外部事件的到来
     */
    public void addEvent(Event event) {
        if (running) {
            try {
                eventQueue.put(event);
                logger.info(String.format("[%s] 接收到新事件: %s", demultiplexerName, event));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warning(String.format("[%s] 添加事件时被中断: %s", demultiplexerName, e.getMessage()));
            }
        }
    }
    
    /**
     * 等待事件发生 - 阻塞式获取事件
     * 这是Reactor模式的核心：等待事件发生
     */
    public Event waitForEvent() throws InterruptedException {
        if (!running) {
            return null;
        }
        
        Event event = eventQueue.take(); // 阻塞等待事件
        logger.info(String.format("[%s] 分离出事件: %s", demultiplexerName, event));
        return event;
    }
    
    /**
     * 等待事件发生 - 带超时的获取事件
     */
    public Event waitForEvent(long timeout, TimeUnit unit) throws InterruptedException {
        if (!running) {
            return null;
        }
        
        Event event = eventQueue.poll(timeout, unit);
        if (event != null) {
            logger.info(String.format("[%s] 分离出事件: %s", demultiplexerName, event));
        }
        return event;
    }
    
    /**
     * 获取当前队列中的事件数量
     */
    public int getEventCount() {
        return eventQueue.size();
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public String getDemultiplexerName() {
        return demultiplexerName;
    }
}

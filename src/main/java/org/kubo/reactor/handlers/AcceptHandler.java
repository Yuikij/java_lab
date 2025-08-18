package org.kubo.reactor.handlers;

import org.kubo.reactor.Event;
import org.kubo.reactor.EventHandler;
import java.util.logging.Logger;

/**
 * 连接接受处理器
 * 处理客户端连接请求事件
 * 在真实的网络编程中，这对应于accept()系统调用
 */
public class AcceptHandler implements EventHandler {
    private static final Logger logger = Logger.getLogger(AcceptHandler.class.getName());
    
    private final String handlerName;
    private int connectionCount = 0;
    
    public AcceptHandler(String name) {
        this.handlerName = name;
    }
    
    @Override
    public void handleEvent(Event event) {
        if (event.getType() != Event.EventType.ACCEPT) {
            logger.warning(String.format("[%s] 收到不支持的事件类型: %s", handlerName, event.getType()));
            return;
        }
        
        connectionCount++;
        
        logger.info(String.format("[%s] 开始处理连接接受事件", handlerName));
        logger.info(String.format("[%s] 客户端连接信息: %s", handlerName, event.getData()));
        logger.info(String.format("[%s] 连接来源: %s", handlerName, event.getSourceId()));
        
        // 模拟连接处理的耗时操作
        try {
            Thread.sleep(50); // 模拟accept处理时间
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warning(String.format("[%s] 处理连接时被中断", handlerName));
            return;
        }
        
        logger.info(String.format("[%s] 成功接受客户端连接，当前总连接数: %d", handlerName, connectionCount));
        logger.info(String.format("[%s] 连接处理完成，可以开始数据传输", handlerName));
    }
    
    @Override
    public Event.EventType getSupportedEventType() {
        return Event.EventType.ACCEPT;
    }
    
    @Override
    public String getHandlerName() {
        return handlerName;
    }
    
    /**
     * 获取当前连接数
     */
    public int getConnectionCount() {
        return connectionCount;
    }
    
    /**
     * 重置连接计数器
     */
    public void resetConnectionCount() {
        this.connectionCount = 0;
        logger.info(String.format("[%s] 连接计数器已重置", handlerName));
    }
}



package org.kubo.reactor.handlers;

import org.kubo.reactor.Event;
import org.kubo.reactor.EventHandler;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * 数据读取处理器
 * 处理客户端数据读取事件
 * 在真实的网络编程中，这对应于read()系统调用
 */
public class ReadHandler implements EventHandler {
    private static final Logger logger = Logger.getLogger(ReadHandler.class.getName());
    
    private final String handlerName;
    private final AtomicLong totalBytesRead = new AtomicLong(0);
    private final AtomicLong readOperationCount = new AtomicLong(0);
    
    public ReadHandler(String name) {
        this.handlerName = name;
    }
    
    @Override
    public void handleEvent(Event event) {
        if (event.getType() != Event.EventType.READ) {
            logger.warning(String.format("[%s] 收到不支持的事件类型: %s", handlerName, event.getType()));
            return;
        }
        
        long operationId = readOperationCount.incrementAndGet();
        
        logger.info(String.format("[%s] 开始处理读取事件 #%d", handlerName, operationId));
        logger.info(String.format("[%s] 数据源: %s", handlerName, event.getSourceId()));
        
        // 模拟数据读取
        String data = (String) event.getData();
        if (data != null) {
            int dataLength = data.length();
            totalBytesRead.addAndGet(dataLength);
            
            logger.info(String.format("[%s] 读取到数据: \"%s\"", handlerName, data));
            logger.info(String.format("[%s] 数据长度: %d 字节", handlerName, dataLength));
            
            // 模拟数据处理的耗时操作
            try {
                Thread.sleep(30); // 模拟读取和处理时间
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warning(String.format("[%s] 数据处理被中断", handlerName));
                return;
            }
            
            // 模拟数据解析和业务逻辑处理
            processBusinessLogic(data, event.getSourceId());
            
        } else {
            logger.warning(String.format("[%s] 接收到空数据", handlerName));
        }
        
        logger.info(String.format("[%s] 读取事件处理完成 #%d，累计读取: %d 字节", 
                handlerName, operationId, totalBytesRead.get()));
    }
    
    /**
     * 模拟业务逻辑处理
     */
    private void processBusinessLogic(String data, String sourceId) {
        logger.info(String.format("[%s] 开始业务逻辑处理，来源: %s", handlerName, sourceId));
        
        // 模拟不同类型的数据处理
        if (data.startsWith("HELLO")) {
            logger.info(String.format("[%s] 处理握手消息: %s", handlerName, data));
        } else if (data.startsWith("DATA")) {
            logger.info(String.format("[%s] 处理业务数据: %s", handlerName, data));
        } else if (data.startsWith("PING")) {
            logger.info(String.format("[%s] 处理心跳包: %s", handlerName, data));
        } else {
            logger.info(String.format("[%s] 处理普通消息: %s", handlerName, data));
        }
        
        logger.info(String.format("[%s] 业务逻辑处理完成", handlerName));
    }
    
    @Override
    public Event.EventType getSupportedEventType() {
        return Event.EventType.READ;
    }
    
    @Override
    public String getHandlerName() {
        return handlerName;
    }
    
    /**
     * 获取累计读取的字节数
     */
    public long getTotalBytesRead() {
        return totalBytesRead.get();
    }
    
    /**
     * 获取读取操作次数
     */
    public long getReadOperationCount() {
        return readOperationCount.get();
    }
    
    /**
     * 重置统计信息
     */
    public void resetStatistics() {
        totalBytesRead.set(0);
        readOperationCount.set(0);
        logger.info(String.format("[%s] 统计信息已重置", handlerName));
    }
}



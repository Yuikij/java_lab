package org.kubo.reactor.handlers;

import org.kubo.reactor.Event;
import org.kubo.reactor.EventHandler;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * 数据写入处理器
 * 处理向客户端写入数据的事件
 * 在真实的网络编程中，这对应于write()系统调用
 */
public class WriteHandler implements EventHandler {
    private static final Logger logger = Logger.getLogger(WriteHandler.class.getName());
    
    private final String handlerName;
    private final AtomicLong totalBytesWritten = new AtomicLong(0);
    private final AtomicLong writeOperationCount = new AtomicLong(0);
    
    public WriteHandler(String name) {
        this.handlerName = name;
    }
    
    @Override
    public void handleEvent(Event event) {
        if (event.getType() != Event.EventType.WRITE) {
            logger.warning(String.format("[%s] 收到不支持的事件类型: %s", handlerName, event.getType()));
            return;
        }
        
        long operationId = writeOperationCount.incrementAndGet();
        
        logger.info(String.format("[%s] 开始处理写入事件 #%d", handlerName, operationId));
        logger.info(String.format("[%s] 目标: %s", handlerName, event.getSourceId()));
        
        // 模拟数据写入
        String data = (String) event.getData();
        if (data != null) {
            int dataLength = data.length();
            
            logger.info(String.format("[%s] 准备写入数据: \"%s\"", handlerName, data));
            logger.info(String.format("[%s] 数据长度: %d 字节", handlerName, dataLength));
            
            // 模拟写入前的数据编码和格式化
            String formattedData = formatDataForTransmission(data);
            
            // 模拟数据写入的耗时操作
            try {
                Thread.sleep(25); // 模拟网络写入时间
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warning(String.format("[%s] 数据写入被中断", handlerName));
                return;
            }
            
            // 模拟写入操作
            boolean writeSuccess = performWrite(formattedData, event.getSourceId());
            
            if (writeSuccess) {
                totalBytesWritten.addAndGet(dataLength);
                logger.info(String.format("[%s] 数据写入成功", handlerName));
            } else {
                logger.warning(String.format("[%s] 数据写入失败", handlerName));
            }
            
        } else {
            logger.warning(String.format("[%s] 尝试写入空数据", handlerName));
        }
        
        logger.info(String.format("[%s] 写入事件处理完成 #%d，累计写入: %d 字节", 
                handlerName, operationId, totalBytesWritten.get()));
    }
    
    /**
     * 格式化传输数据
     */
    private String formatDataForTransmission(String rawData) {
        // 模拟数据格式化：添加协议头、时间戳等
        String timestamp = String.valueOf(System.currentTimeMillis());
        String formattedData = String.format("[%s] %s", timestamp, rawData);
        
        logger.info(String.format("[%s] 数据格式化完成: %s", handlerName, formattedData));
        return formattedData;
    }
    
    /**
     * 执行实际的写入操作
     */
    private boolean performWrite(String data, String target) {
        logger.info(String.format("[%s] 执行写入操作到目标: %s", handlerName, target));
        logger.info(String.format("[%s] 写入内容: %s", handlerName, data));
        
        // 模拟写入操作，这里总是返回成功
        // 在真实场景中，这里会进行实际的socket写入操作
        try {
            Thread.sleep(10); // 模拟底层写入时间
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        
        logger.info(String.format("[%s] 底层写入操作完成", handlerName));
        return true;
    }
    
    @Override
    public Event.EventType getSupportedEventType() {
        return Event.EventType.WRITE;
    }
    
    @Override
    public String getHandlerName() {
        return handlerName;
    }
    
    /**
     * 获取累计写入的字节数
     */
    public long getTotalBytesWritten() {
        return totalBytesWritten.get();
    }
    
    /**
     * 获取写入操作次数
     */
    public long getWriteOperationCount() {
        return writeOperationCount.get();
    }
    
    /**
     * 重置统计信息
     */
    public void resetStatistics() {
        totalBytesWritten.set(0);
        writeOperationCount.set(0);
        logger.info(String.format("[%s] 统计信息已重置", handlerName));
    }
}

package org.kubo.reactor;

/**
 * 事件类 - Reactor模式的核心组件之一
 * 表示系统中发生的各种事件，包括连接事件、读取事件、写入事件等
 */
public class Event {
    /**
     * 事件类型枚举
     */
    public enum EventType {
        ACCEPT,     // 客户端连接事件
        READ,       // 数据读取事件
        WRITE,      // 数据写入事件
        CLOSE       // 连接关闭事件
    }
    
    private final EventType type;           // 事件类型
    private final Object data;              // 事件携带的数据
    private final long timestamp;           // 事件发生时间戳
    private final String sourceId;          // 事件源标识
    
    public Event(EventType type, Object data, String sourceId) {
        this.type = type;
        this.data = data;
        this.sourceId = sourceId;
        this.timestamp = System.currentTimeMillis();
    }
    
    public EventType getType() {
        return type;
    }
    
    public Object getData() {
        return data;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public String getSourceId() {
        return sourceId;
    }
    
    @Override
    public String toString() {
        return String.format("Event{type=%s, sourceId=%s, timestamp=%d, data=%s}", 
                type, sourceId, timestamp, data);
    }
}

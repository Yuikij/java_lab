package org.kubo.reactor;

/**
 * 事件处理器接口 - Reactor模式的核心组件
 * 定义了处理特定事件的方法，实现了策略模式
 * 每种类型的事件都有对应的处理器实现
 */
public interface EventHandler {
    
    /**
     * 处理事件的核心方法
     * @param event 要处理的事件
     */
    void handleEvent(Event event);
    
    /**
     * 获取处理器能够处理的事件类型
     * @return 支持的事件类型
     */
    Event.EventType getSupportedEventType();
    
    /**
     * 获取处理器的名称，用于日志和调试
     * @return 处理器名称
     */
    String getHandlerName();
}

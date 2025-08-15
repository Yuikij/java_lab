package org.kubo.reactor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * 事件分发器 - Reactor模式的核心组件
 * 负责将事件分发给相应的事件处理器
 * 维护事件类型与处理器的映射关系
 */
public class Dispatcher {
    private static final Logger logger = Logger.getLogger(Dispatcher.class.getName());
    
    // 事件类型与处理器的映射表
    private final ConcurrentHashMap<Event.EventType, EventHandler> handlerMap;
    private final String dispatcherName;
    
    public Dispatcher(String name) {
        this.dispatcherName = name;
        this.handlerMap = new ConcurrentHashMap<>();
        logger.info(String.format("[%s] 事件分发器初始化完成", dispatcherName));
    }
    
    /**
     * 注册事件处理器
     * @param eventType 事件类型
     * @param handler 对应的处理器
     */
    public void registerHandler(Event.EventType eventType, EventHandler handler) {
        handlerMap.put(eventType, handler);
        logger.info(String.format("[%s] 注册事件处理器: %s -> %s", 
                dispatcherName, eventType, handler.getHandlerName()));
    }
    
    /**
     * 移除事件处理器
     * @param eventType 事件类型
     */
    public void removeHandler(Event.EventType eventType) {
        EventHandler removed = handlerMap.remove(eventType);
        if (removed != null) {
            logger.info(String.format("[%s] 移除事件处理器: %s -> %s", 
                    dispatcherName, eventType, removed.getHandlerName()));
        }
    }
    
    /**
     * 分发事件到对应的处理器
     * 这是分发器的核心功能
     * @param event 要分发的事件
     */
    public void dispatch(Event event) {
        if (event == null) {
            logger.warning(String.format("[%s] 尝试分发空事件", dispatcherName));
            return;
        }
        
        EventHandler handler = handlerMap.get(event.getType());
        if (handler != null) {
            try {
                logger.info(String.format("[%s] 分发事件 %s 到处理器 %s", 
                        dispatcherName, event.getType(), handler.getHandlerName()));
                
                long startTime = System.currentTimeMillis();
                handler.handleEvent(event);
                long endTime = System.currentTimeMillis();
                
                logger.info(String.format("[%s] 事件处理完成，耗时: %d ms", 
                        dispatcherName, endTime - startTime));
                        
            } catch (Exception e) {
                logger.severe(String.format("[%s] 处理事件时发生异常: %s, 事件: %s", 
                        dispatcherName, e.getMessage(), event));
                e.printStackTrace();
            }
        } else {
            logger.warning(String.format("[%s] 没有找到处理事件类型 %s 的处理器", 
                    dispatcherName, event.getType()));
        }
    }
    
    /**
     * 获取已注册的处理器数量
     */
    public int getHandlerCount() {
        return handlerMap.size();
    }
    
    /**
     * 检查是否有特定类型事件的处理器
     */
    public boolean hasHandler(Event.EventType eventType) {
        return handlerMap.containsKey(eventType);
    }
    
    public String getDispatcherName() {
        return dispatcherName;
    }
}

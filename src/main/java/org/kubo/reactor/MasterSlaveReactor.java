package org.kubo.reactor;

import org.kubo.reactor.handlers.AcceptHandler;
import org.kubo.reactor.handlers.ReadHandler;
import org.kubo.reactor.handlers.WriteHandler;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.ArrayList;

/**
 * 主从Reactor多线程模型演示
 * 
 * 特性和原理：
 * 1. 主Reactor（MainReactor）：负责监听和处理客户端连接请求
 * 2. 从Reactor（SubReactor）：负责处理已建立连接的I/O事件
 * 3. 工作线程池：负责具体的业务逻辑处理
 * 4. 负载均衡：新连接会被分配到不同的SubReactor
 * 
 * 这是目前最主流的高性能网络服务器架构，被Netty、Nginx等广泛采用
 * 
 * 优点：
 * - 主Reactor专门处理连接，避免连接建立成为瓶颈
 * - 多个SubReactor并行处理I/O，充分利用多核CPU
 * - 可以处理大量并发连接
 * - 良好的负载均衡和扩展性
 * 
 * 缺点：
 * - 实现最为复杂
 * - 需要合理调优各个组件的线程数
 * - 调试和问题排查相对困难
 */
public class MasterSlaveReactor {
    private static final Logger logger = Logger.getLogger(MasterSlaveReactor.class.getName());
    
    private final String reactorName;
    private final MainReactor mainReactor;
    private final List<SubReactor> subReactors;
    private final ExecutorService workerThreadPool;
    private final AtomicInteger subReactorSelector;
    private volatile boolean running;
    private final int subReactorCount;
    private final int workerThreadCount;
    
    public MasterSlaveReactor(String name, int subReactorCount, int workerThreadCount) {
        this.reactorName = name;
        this.subReactorCount = subReactorCount;
        this.workerThreadCount = workerThreadCount;
        this.subReactorSelector = new AtomicInteger(0);
        this.running = false;
        
        // 创建主Reactor
        this.mainReactor = new MainReactor(name + "-MainReactor");
        
        // 创建多个从Reactor
        this.subReactors = new ArrayList<>();
        for (int i = 0; i < subReactorCount; i++) {
            SubReactor subReactor = new SubReactor(name + "-SubReactor-" + i);
            subReactors.add(subReactor);
        }
        
        // 创建工作线程池
        this.workerThreadPool = Executors.newFixedThreadPool(
                workerThreadCount,
                r -> {
                    Thread t = new Thread(r, name + "-Worker-" + System.currentTimeMillis());
                    t.setDaemon(false);
                    return t;
                }
        );
        
        logger.info(String.format("[%s] 主从Reactor多线程模型初始化完成", reactorName));
        logger.info(String.format("[%s] 主Reactor数量: 1, 从Reactor数量: %d, 工作线程数: %d", 
                reactorName, subReactorCount, workerThreadCount));
    }
    
    /**
     * 启动主从Reactor系统
     */
    public void start() {
        if (running) {
            logger.warning(String.format("[%s] 主从Reactor已经在运行中", reactorName));
            return;
        }
        
        running = true;
        
        logger.info(String.format("[%s] 启动主从Reactor多线程模型", reactorName));
        
        // 启动主Reactor
        mainReactor.start();
        
        // 启动所有从Reactor
        for (SubReactor subReactor : subReactors) {
            subReactor.start();
        }
        
        logger.info(String.format("[%s] 主从Reactor系统启动完成", reactorName));
    }
    
    /**
     * 停止主从Reactor系统
     */
    public void stop() {
        if (!running) {
            logger.warning(String.format("[%s] 主从Reactor未在运行", reactorName));
            return;
        }
        
        logger.info(String.format("[%s] 开始停止主从Reactor系统", reactorName));
        
        running = false;
        
        // 停止主Reactor
        mainReactor.stop();
        
        // 停止所有从Reactor
        for (SubReactor subReactor : subReactors) {
            subReactor.stop();
        }
        
        // 关闭工作线程池
        workerThreadPool.shutdown();
        try {
            if (!workerThreadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                logger.warning(String.format("[%s] 工作线程池未能正常关闭，强制关闭", reactorName));
                workerThreadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.warning(String.format("[%s] 等待线程池关闭时被中断", reactorName));
            workerThreadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        logger.info(String.format("[%s] 主从Reactor系统已停止", reactorName));
    }
    
    /**
     * 模拟客户端连接请求
     */
    public void simulateClientConnection(String clientId) {
        if (running) {
            Event connectEvent = new Event(Event.EventType.ACCEPT, 
                    "客户端连接: " + clientId, clientId);
            mainReactor.submitEvent(connectEvent);
        } else {
            logger.warning(String.format("[%s] 系统未运行，无法处理连接请求", reactorName));
        }
    }
    
    /**
     * 模拟客户端数据读取
     */
    public void simulateDataRead(String clientId, String data) {
        if (running && !subReactors.isEmpty()) {
            // 使用负载均衡选择SubReactor
            SubReactor selectedSubReactor = selectSubReactor();
            Event readEvent = new Event(Event.EventType.READ, data, clientId);
            selectedSubReactor.submitEvent(readEvent);
        } else {
            logger.warning(String.format("[%s] 系统未运行或无可用SubReactor", reactorName));
        }
    }
    
    /**
     * 模拟数据写入
     */
    public void simulateDataWrite(String clientId, String data) {
        if (running && !subReactors.isEmpty()) {
            // 使用负载均衡选择SubReactor
            SubReactor selectedSubReactor = selectSubReactor();
            Event writeEvent = new Event(Event.EventType.WRITE, data, clientId);
            selectedSubReactor.submitEvent(writeEvent);
        } else {
            logger.warning(String.format("[%s] 系统未运行或无可用SubReactor", reactorName));
        }
    }
    
    /**
     * 负载均衡选择SubReactor
     */
    private SubReactor selectSubReactor() {
        int index = subReactorSelector.getAndIncrement() % subReactors.size();
        SubReactor selected = subReactors.get(index);
        logger.info(String.format("[%s] 负载均衡选择SubReactor: %s", 
                reactorName, selected.getReactorName()));
        return selected;
    }
    
    /**
     * 获取统计信息
     */
    public void printStatistics() {
        logger.info(String.format("[%s] === 主从Reactor统计信息 ===", reactorName));
        logger.info(String.format("[%s] 运行状态: %s", reactorName, running ? "运行中" : "已停止"));
        logger.info(String.format("[%s] 主Reactor: %s", reactorName, mainReactor.getReactorName()));
        logger.info(String.format("[%s] 从Reactor数量: %d", reactorName, subReactors.size()));
        logger.info(String.format("[%s] 工作线程数: %d", reactorName, workerThreadCount));
        
        // 打印主Reactor统计
        mainReactor.printStatistics();
        
        // 打印每个SubReactor统计
        for (SubReactor subReactor : subReactors) {
            subReactor.printStatistics();
        }
        
        logger.info(String.format("[%s] ================================", reactorName));
    }
    
    /**
     * 主Reactor - 专门处理连接事件
     */
    private class MainReactor {
        private final EventDemultiplexer demultiplexer;
        private final Dispatcher dispatcher;
        private final Thread reactorThread;
        private volatile boolean running;
        private final String reactorName;
        
        public MainReactor(String name) {
            this.reactorName = name;
            this.demultiplexer = new EventDemultiplexer(name + "-Demux");
            this.dispatcher = new Dispatcher(name + "-Dispatcher");
            this.reactorThread = new Thread(this::eventLoop, name + "-Thread");
            this.running = false;
            
            // 只注册连接处理器
            AcceptHandler acceptHandler = new AcceptHandler(name + "-AcceptHandler");
            dispatcher.registerHandler(Event.EventType.ACCEPT, acceptHandler);
            
            logger.info(String.format("[%s] 主Reactor初始化完成", reactorName));
        }
        
        public void start() {
            if (running) return;
            
            running = true;
            demultiplexer.start();
            reactorThread.start();
            
            logger.info(String.format("[%s] 主Reactor启动", reactorName));
        }
        
        public void stop() {
            if (!running) return;
            
            running = false;
            demultiplexer.stop();
            reactorThread.interrupt();
            
            try {
                reactorThread.join(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            logger.info(String.format("[%s] 主Reactor停止", reactorName));
        }
        
        private void eventLoop() {
            logger.info(String.format("[%s] 主Reactor事件循环开始", reactorName));
            
            while (running && !Thread.currentThread().isInterrupted()) {
                try {
                    Event event = demultiplexer.waitForEvent(1, TimeUnit.SECONDS);
                    if (event != null) {
                        logger.info(String.format("[%s] 处理连接事件: %s", reactorName, event.getSourceId()));
                        dispatcher.dispatch(event);
                        
                        // 连接建立后，后续的I/O事件由SubReactor处理
                        logger.info(String.format("[%s] 连接已建立，后续I/O事件将由SubReactor处理", reactorName));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.severe(String.format("[%s] 事件循环异常: %s", reactorName, e.getMessage()));
                }
            }
            
            logger.info(String.format("[%s] 主Reactor事件循环结束", reactorName));
        }
        
        public void submitEvent(Event event) {
            if (running) {
                demultiplexer.addEvent(event);
            }
        }
        
        public String getReactorName() {
            return reactorName;
        }
        
        public void printStatistics() {
            logger.info(String.format("[%s] 主Reactor - 待处理事件: %d", 
                    reactorName, demultiplexer.getEventCount()));
        }
    }
    
    /**
     * 从Reactor - 处理I/O事件
     */
    private class SubReactor {
        private final EventDemultiplexer demultiplexer;
        private final Dispatcher dispatcher;
        private final Thread reactorThread;
        private volatile boolean running;
        private final String reactorName;
        
        public SubReactor(String name) {
            this.reactorName = name;
            this.demultiplexer = new EventDemultiplexer(name + "-Demux");
            this.dispatcher = new Dispatcher(name + "-Dispatcher");
            this.reactorThread = new Thread(this::eventLoop, name + "-Thread");
            this.running = false;
            
            // 注册读写处理器
            ReadHandler readHandler = new ReadHandler(name + "-ReadHandler");
            WriteHandler writeHandler = new WriteHandler(name + "-WriteHandler");
            
            dispatcher.registerHandler(Event.EventType.READ, readHandler);
            dispatcher.registerHandler(Event.EventType.WRITE, writeHandler);
            
            logger.info(String.format("[%s] 从Reactor初始化完成", reactorName));
        }
        
        public void start() {
            if (running) return;
            
            running = true;
            demultiplexer.start();
            reactorThread.start();
            
            logger.info(String.format("[%s] 从Reactor启动", reactorName));
        }
        
        public void stop() {
            if (!running) return;
            
            running = false;
            demultiplexer.stop();
            reactorThread.interrupt();
            
            try {
                reactorThread.join(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            logger.info(String.format("[%s] 从Reactor停止", reactorName));
        }
        
        private void eventLoop() {
            logger.info(String.format("[%s] 从Reactor事件循环开始", reactorName));
            
            while (running && !Thread.currentThread().isInterrupted()) {
                try {
                    Event event = demultiplexer.waitForEvent(1, TimeUnit.SECONDS);
                    if (event != null) {
                        logger.info(String.format("[%s] 接收到I/O事件: %s", reactorName, event.getType()));
                        
                        // 将业务处理提交到工作线程池
                        CompletableFuture.runAsync(() -> {
                            String workerThread = Thread.currentThread().getName();
                            logger.info(String.format("[%s] 工作线程 %s 开始处理业务逻辑", 
                                    reactorName, workerThread));
                            dispatcher.dispatch(event);
                        }, workerThreadPool);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.severe(String.format("[%s] 事件循环异常: %s", reactorName, e.getMessage()));
                }
            }
            
            logger.info(String.format("[%s] 从Reactor事件循环结束", reactorName));
        }
        
        public void submitEvent(Event event) {
            if (running) {
                demultiplexer.addEvent(event);
            }
        }
        
        public String getReactorName() {
            return reactorName;
        }
        
        public void printStatistics() {
            logger.info(String.format("[%s] 从Reactor - 待处理事件: %d", 
                    reactorName, demultiplexer.getEventCount()));
        }
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public String getReactorName() {
        return reactorName;
    }
}

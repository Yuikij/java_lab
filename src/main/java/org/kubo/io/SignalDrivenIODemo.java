package org.kubo.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 信号驱动I/O (Signal-Driven I/O) 模型演示
 * 
 * 注意：Java本身不直接支持信号驱动I/O，这里使用回调机制模拟其行为
 * 
 * 特性：
 * 1. 应用程序安装一个信号处理函数，当I/O操作就绪时，内核发送SIGIO信号
 * 2. 应用程序在信号处理函数中处理I/O操作
 * 3. 主程序可以继续执行其他工作，不会被I/O操作阻塞
 * 4. 实际应用较少，因为信号处理相对复杂
 */
public class SignalDrivenIODemo {
    
    private static final int PORT = 8083;
    private static final int BUFFER_SIZE = 1024;
    private static final AtomicLong eventCounter = new AtomicLong(0);
    
    // 模拟信号处理器的线程池
    private static final ExecutorService signalHandler = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "SignalHandler-" + eventCounter.incrementAndGet());
        t.setDaemon(true);
        return t;
    });
    
    public static void main(String[] args) {
        System.out.println("=== 信号驱动I/O模型演示 ===");
        
        // 启动服务器
        new Thread(SignalDrivenIODemo::startServer).start();
        
        // 等待服务器启动
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 启动客户端
        startClients();
        
        // 主程序继续执行其他工作（模拟信号驱动的特点）
        simulateMainWork();
    }
    
    /**
     * 模拟信号驱动I/O服务器
     * 使用回调机制模拟信号处理
     */
    public static void startServer() {
        try {
            Selector selector = Selector.open();
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.bind(new InetSocketAddress(PORT));
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            
            System.out.println("信号驱动I/O服务器启动，监听端口: " + PORT);
            
            // 主I/O线程：只负责检测I/O事件，不处理具体业务
            while (true) {
                int readyChannels = selector.select(100); // 非阻塞选择
                
                if (readyChannels > 0) {
                    // 模拟收到SIGIO信号，异步处理I/O事件
                    var selectedKeys = selector.selectedKeys();
                    for (SelectionKey key : selectedKeys) {
                        // 异步处理每个I/O事件（模拟信号处理函数）
                        signalHandler.submit(() -> handleIOEvent(key, selector));
                    }
                    selectedKeys.clear();
                }
                
                // 主线程可以继续做其他工作
                doOtherWork();
            }
            
        } catch (IOException e) {
            System.err.println("服务器错误: " + e.getMessage());
        }
    }
    
    /**
     * 信号处理函数：处理I/O事件
     */
    private static void handleIOEvent(SelectionKey key, Selector selector) {
        try {
            if (!key.isValid()) {
                return;
            }
            
            long eventId = eventCounter.incrementAndGet();
            String threadName = Thread.currentThread().getName();
            
            if (key.isAcceptable()) {
                System.out.println("[" + threadName + "] 处理Accept事件 #" + eventId);
                handleAccept(key, selector);
            } else if (key.isReadable()) {
                System.out.println("[" + threadName + "] 处理Read事件 #" + eventId);
                handleRead(key);
            } else if (key.isWritable()) {
                System.out.println("[" + threadName + "] 处理Write事件 #" + eventId);
                handleWrite(key);
            }
            
        } catch (Exception e) {
            System.err.println("信号处理函数错误: " + e.getMessage());
            try {
                key.channel().close();
                key.cancel();
            } catch (IOException closeEx) {
                // 忽略关闭异常
            }
        }
    }
    
    /**
     * 处理Accept事件
     */
    private static void handleAccept(SelectionKey key, Selector selector) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        
        if (clientChannel != null) {
            clientChannel.configureBlocking(false);
            SelectionKey clientKey = clientChannel.register(selector, SelectionKey.OP_READ);
            clientKey.attach(ByteBuffer.allocate(BUFFER_SIZE));
            System.out.println("接受新连接: " + clientChannel.getRemoteAddress());
        }
    }
    
    /**
     * 处理Read事件
     */
    private static void handleRead(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        
        buffer.clear();
        int bytesRead = clientChannel.read(buffer);
        
        if (bytesRead > 0) {
            buffer.flip();
            String message = StandardCharsets.UTF_8.decode(buffer).toString().trim();
            System.out.println("收到消息: " + message);
            
            // 模拟业务处理时间
            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            String response = "Signal-Driven Echo: " + message;
            ByteBuffer responseBuffer = ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8));
            key.attach(responseBuffer);
            key.interestOps(SelectionKey.OP_WRITE);
            
        } else if (bytesRead == -1) {
            System.out.println("客户端断开连接");
            clientChannel.close();
            key.cancel();
        }
    }
    
    /**
     * 处理Write事件
     */
    private static void handleWrite(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        
        if (buffer.hasRemaining()) {
            clientChannel.write(buffer);
        }
        
        if (!buffer.hasRemaining()) {
            // 写完成，切换回读模式或关闭连接
            String response = new String(buffer.array(), StandardCharsets.UTF_8);
            if (response.contains("bye")) {
                System.out.println("关闭客户端连接");
                clientChannel.close();
                key.cancel();
            } else {
                key.attach(ByteBuffer.allocate(BUFFER_SIZE));
                key.interestOps(SelectionKey.OP_READ);
            }
        }
    }
    
    /**
     * 主程序的其他工作（模拟信号驱动I/O的优势）
     */
    private static void doOtherWork() {
        // 模拟主程序继续执行其他任务
        try {
            TimeUnit.MILLISECONDS.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 模拟主程序的工作
     */
    private static void simulateMainWork() {
        System.out.println("主程序开始执行其他重要工作...");
        for (int i = 1; i <= 10; i++) {
            System.out.println("主程序工作进度: " + (i * 10) + "%");
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("主程序工作完成！");
    }
    
    /**
     * 启动多个客户端测试
     */
    private static void startClients() {
        for (int i = 1; i <= 3; i++) {
            final int clientId = i;
            new Thread(() -> {
                try (SocketChannel socketChannel = SocketChannel.open()) {
                    socketChannel.connect(new InetSocketAddress("localhost", PORT));
                    
                    String message = "Hello from Signal-Driven client " + clientId;
                    ByteBuffer buffer = ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8));
                    socketChannel.write(buffer);
                    
                    ByteBuffer responseBuffer = ByteBuffer.allocate(BUFFER_SIZE);
                    int bytesRead = socketChannel.read(responseBuffer);
                    if (bytesRead > 0) {
                        responseBuffer.flip();
                        String response = StandardCharsets.UTF_8.decode(responseBuffer).toString();
                        System.out.println("客户端" + clientId + "收到响应: " + response);
                    }
                    
                    TimeUnit.MILLISECONDS.sleep(200);
                    
                    ByteBuffer byeBuffer = ByteBuffer.wrap("bye".getBytes(StandardCharsets.UTF_8));
                    socketChannel.write(byeBuffer);
                    
                } catch (IOException | InterruptedException e) {
                    System.err.println("客户端" + clientId + "错误: " + e.getMessage());
                }
            }).start();
        }
    }
}

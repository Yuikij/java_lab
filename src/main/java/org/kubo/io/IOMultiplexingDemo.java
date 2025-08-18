package org.kubo.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * I/O多路复用 (I/O Multiplexing) 模型演示
 * 使用Selector实现多路复用
 * 
 * 特性：
 * 1. 使用select/poll/epoll等系统调用监控多个I/O流
 * 2. 单个线程可以同时监控多个连接的I/O状态
 * 3. 当有I/O事件就绪时，系统会通知应用程序
 * 4. 避免了轮询带来的CPU浪费
 * 5. 适合高并发场景
 */
public class IOMultiplexingDemo {
    
    private static final int PORT = 8082;
    private static final int BUFFER_SIZE = 1024;
    
    public static void main(String[] args) {
        System.out.println("=== I/O多路复用模型演示 ===");
        
        // 启动服务器
        new Thread(IOMultiplexingDemo::startServer).start();
        
        // 等待服务器启动
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 启动客户端
        startClients();
    }
    
    /**
     * 使用Selector实现I/O多路复用服务器
     */
    public static void startServer() {
        try {
            // 创建Selector
            Selector selector = Selector.open();
            
            // 创建ServerSocketChannel
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.bind(new InetSocketAddress(PORT));
            
            // 将ServerSocketChannel注册到Selector，监听OP_ACCEPT事件
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            
            System.out.println("I/O多路复用服务器启动，监听端口: " + PORT);
            
            while (true) {
                // 阻塞等待就绪的Channel
                int readyChannels = selector.select();
                
                if (readyChannels == 0) {
                    continue;
                }
                
                // 获取就绪的SelectionKey集合
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove(); // 移除已处理的key
                    
                    if (!key.isValid()) {
                        continue;
                    }
                    
                    if (key.isAcceptable()) {
                        // 处理新连接
                        handleAccept(key, selector);
                    } else if (key.isReadable()) {
                        // 处理读事件
                        handleRead(key);
                    } else if (key.isWritable()) {
                        // 处理写事件
                        handleWrite(key);
                    }
                }
            }
            
        } catch (IOException e) {
            System.err.println("服务器错误: " + e.getMessage());
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
            
            // 将新连接注册到Selector，监听读事件
            SelectionKey clientKey = clientChannel.register(selector, SelectionKey.OP_READ);
            
            // 为每个连接分配一个缓冲区
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
            
            // 准备响应数据
            String response = "Echo: " + message;
            ByteBuffer responseBuffer = ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8));
            
            // 将响应数据附加到key上，并切换到写模式
            key.attach(responseBuffer);
            key.interestOps(SelectionKey.OP_WRITE);
            
            if ("bye".equalsIgnoreCase(message)) {
                // 标记需要关闭连接
                key.attach(responseBuffer);
                ((ByteBuffer) key.attachment()).put(" [CLOSE]".getBytes(StandardCharsets.UTF_8));
                ((ByteBuffer) key.attachment()).flip();
            }
            
        } else if (bytesRead == -1) {
            // 客户端关闭连接
            System.out.println("客户端断开连接: " + clientChannel.getRemoteAddress());
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
            // 写完成，检查是否需要关闭连接
            String response = new String(buffer.array(), StandardCharsets.UTF_8);
            if (response.contains("[CLOSE]")) {
                System.out.println("关闭客户端连接");
                clientChannel.close();
                key.cancel();
            } else {
                // 切换回读模式
                key.attach(ByteBuffer.allocate(BUFFER_SIZE));
                key.interestOps(SelectionKey.OP_READ);
            }
        }
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
                    
                    // 发送消息
                    String message = "Hello from Multiplexing client " + clientId;
                    ByteBuffer buffer = ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8));
                    socketChannel.write(buffer);
                    
                    // 读取响应
                    ByteBuffer responseBuffer = ByteBuffer.allocate(BUFFER_SIZE);
                    int bytesRead = socketChannel.read(responseBuffer);
                    if (bytesRead > 0) {
                        responseBuffer.flip();
                        String response = StandardCharsets.UTF_8.decode(responseBuffer).toString();
                        System.out.println("客户端" + clientId + "收到响应: " + response);
                    }
                    
                    // 等待一下再发送结束消息
                    TimeUnit.MILLISECONDS.sleep(100);
                    
                    // 发送结束消息
                    ByteBuffer byeBuffer = ByteBuffer.wrap("bye".getBytes(StandardCharsets.UTF_8));
                    socketChannel.write(byeBuffer);
                    
                    // 读取最终响应
                    responseBuffer.clear();
                    bytesRead = socketChannel.read(responseBuffer);
                    if (bytesRead > 0) {
                        responseBuffer.flip();
                        String finalResponse = StandardCharsets.UTF_8.decode(responseBuffer).toString();
                        System.out.println("客户端" + clientId + "收到最终响应: " + finalResponse);
                    }
                    
                } catch (IOException | InterruptedException e) {
                    System.err.println("客户端" + clientId + "错误: " + e.getMessage());
                }
            }).start();
        }
    }
}

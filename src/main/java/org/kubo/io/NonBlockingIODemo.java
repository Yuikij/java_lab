package org.kubo.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 非阻塞I/O (Non-blocking I/O, NIO) 模型演示
 * 
 * 特性：
 * 1. 应用程序主动轮询内核，检查I/O操作是否就绪
 * 2. 如果没有就绪的I/O操作，立即返回，不会阻塞
 * 3. 需要应用程序不断轮询，会消耗CPU资源
 * 4. 单线程可以处理多个连接
 */
public class NonBlockingIODemo {
    
    private static final int PORT = 8081;
    private static final int BUFFER_SIZE = 1024;
    
    public static void main(String[] args) {
        System.out.println("=== 非阻塞I/O (NIO) 模型演示 ===");
        
        // 启动服务器
        new Thread(NonBlockingIODemo::startServer).start();
        
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
     * NIO服务器实现（轮询模式）
     */
    public static void startServer() {
        try {
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false); // 设置为非阻塞模式
            serverChannel.bind(new InetSocketAddress(PORT));
            
            System.out.println("NIO服务器启动，监听端口: " + PORT);
            
            List<SocketChannel> clients = new ArrayList<>();
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            
            while (true) {
                // 非阻塞接受连接
                SocketChannel clientChannel = serverChannel.accept();
                if (clientChannel != null) {
                    clientChannel.configureBlocking(false);
                    clients.add(clientChannel);
                    System.out.println("接受新连接: " + clientChannel.getRemoteAddress());
                }
                
                // 处理现有连接
                Iterator<SocketChannel> iterator = clients.iterator();
                while (iterator.hasNext()) {
                    SocketChannel client = iterator.next();
                    
                    try {
                        buffer.clear();
                        int bytesRead = client.read(buffer);
                        
                        if (bytesRead > 0) {
                            // 有数据可读
                            buffer.flip();
                            String message = StandardCharsets.UTF_8.decode(buffer).toString().trim();
                            System.out.println("收到消息: " + message);
                            
                            // 回应客户端
                            String response = "Echo: " + message;
                            ByteBuffer responseBuffer = ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8));
                            client.write(responseBuffer);
                            
                            if ("bye".equalsIgnoreCase(message)) {
                                client.close();
                                iterator.remove();
                                System.out.println("客户端断开连接");
                            }
                        } else if (bytesRead == -1) {
                            // 客户端关闭连接
                            client.close();
                            iterator.remove();
                            System.out.println("客户端断开连接");
                        }
                        // bytesRead == 0 表示没有数据可读，继续轮询下一个客户端
                        
                    } catch (IOException e) {
                        System.err.println("处理客户端时出错: " + e.getMessage());
                        try {
                            client.close();
                        } catch (IOException closeEx) {
                            // 忽略关闭异常
                        }
                        iterator.remove();
                    }
                }
                
                // 短暂休眠，避免过度消耗CPU
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
        } catch (IOException e) {
            System.err.println("服务器错误: " + e.getMessage());
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
                    String message = "Hello from NIO client " + clientId;
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

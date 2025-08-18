package org.kubo.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 异步I/O (Asynchronous I/O, AIO) 模型演示
 * 使用Java NIO.2的异步通道实现
 * 
 * 特性：
 * 1. 应用程序发起I/O操作后立即返回，不阻塞
 * 2. 当I/O操作完成时，内核会通知应用程序（通过回调函数）
 * 3. 真正的异步，应用程序无需轮询或阻塞等待
 * 4. 适合高并发、高吞吐量的场景
 * 5. 编程复杂度较高，需要处理回调
 */
public class AsynchronousIODemo {
    
    private static final int PORT = 8084;
    private static final int BUFFER_SIZE = 1024;
    private static final AtomicInteger connectionCounter = new AtomicInteger(0);
    
    public static void main(String[] args) {
        System.out.println("=== 异步I/O (AIO) 模型演示 ===");
        
        // 启动服务器
        new Thread(AsynchronousIODemo::startServer).start();
        
        // 等待服务器启动
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 启动客户端
        startClients();
        
        // 主程序继续执行其他工作
        simulateMainWork();
    }
    
    /**
     * AIO服务器实现
     */
    public static void startServer() {
        try {
            AsynchronousServerSocketChannel serverChannel = AsynchronousServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(PORT));
            
            System.out.println("AIO服务器启动，监听端口: " + PORT);
            
            // 异步接受连接
            serverChannel.accept(null, new AcceptHandler(serverChannel));
            
            // 保持主线程运行
            CountDownLatch latch = new CountDownLatch(1);
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
        } catch (IOException e) {
            System.err.println("服务器错误: " + e.getMessage());
        }
    }
    
    /**
     * 接受连接的处理器
     */
    static class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, Void> {
        private final AsynchronousServerSocketChannel serverChannel;
        
        public AcceptHandler(AsynchronousServerSocketChannel serverChannel) {
            this.serverChannel = serverChannel;
        }
        
        @Override
        public void completed(AsynchronousSocketChannel clientChannel, Void attachment) {
            // 继续异步接受下一个连接
            serverChannel.accept(null, this);
            
            try {
                int connectionId = connectionCounter.incrementAndGet();
                System.out.println("接受新连接 #" + connectionId + ": " + 
                    clientChannel.getRemoteAddress());
                
                // 开始异步读取数据
                ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                clientChannel.read(buffer, new ClientContext(clientChannel, connectionId), 
                    new ReadHandler());
                    
            } catch (IOException e) {
                System.err.println("处理新连接时出错: " + e.getMessage());
            }
        }
        
        @Override
        public void failed(Throwable exc, Void attachment) {
            System.err.println("接受连接失败: " + exc.getMessage());
        }
    }
    
    /**
     * 客户端上下文信息
     */
    static class ClientContext {
        final AsynchronousSocketChannel channel;
        final int connectionId;
        
        ClientContext(AsynchronousSocketChannel channel, int connectionId) {
            this.channel = channel;
            this.connectionId = connectionId;
        }
    }
    
    /**
     * 读取数据的处理器
     */
    static class ReadHandler implements CompletionHandler<Integer, ClientContext> {
        
        @Override
        public void completed(Integer bytesRead, ClientContext context) {
            if (bytesRead > 0) {
                try {
                    // 读取到数据
                    ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                    context.channel.read(buffer, context, new CompletionHandler<Integer, ClientContext>() {
                        @Override
                        public void completed(Integer result, ClientContext ctx) {
                            if (result > 0) {
                                buffer.flip();
                                String message = StandardCharsets.UTF_8.decode(buffer).toString().trim();
                                System.out.println("连接#" + ctx.connectionId + " 收到消息: " + message);
                                
                                // 异步写回响应
                                String response = "AIO Echo: " + message;
                                ByteBuffer responseBuffer = ByteBuffer.wrap(
                                    response.getBytes(StandardCharsets.UTF_8));
                                
                                ctx.channel.write(responseBuffer, ctx, new WriteHandler());
                                
                                if (!"bye".equalsIgnoreCase(message)) {
                                    // 继续读取下一个消息
                                    ByteBuffer nextBuffer = ByteBuffer.allocate(BUFFER_SIZE);
                                    ctx.channel.read(nextBuffer, ctx, new ReadHandler());
                                }
                            }
                        }
                        
                        @Override
                        public void failed(Throwable exc, ClientContext ctx) {
                            handleConnectionError(exc, ctx);
                        }
                    });
                } catch (Exception e) {
                    handleConnectionError(e, context);
                }
            } else if (bytesRead == -1) {
                // 客户端关闭连接
                System.out.println("连接#" + context.connectionId + " 客户端断开连接");
                closeConnection(context);
            }
        }
        
        @Override
        public void failed(Throwable exc, ClientContext context) {
            handleConnectionError(exc, context);
        }
        
        private void handleConnectionError(Throwable exc, ClientContext context) {
            System.err.println("连接#" + context.connectionId + " 读取数据失败: " + exc.getMessage());
            closeConnection(context);
        }
        
        private void closeConnection(ClientContext context) {
            try {
                context.channel.close();
            } catch (IOException e) {
                System.err.println("关闭连接时出错: " + e.getMessage());
            }
        }
    }
    
    /**
     * 写入数据的处理器
     */
    static class WriteHandler implements CompletionHandler<Integer, ClientContext> {
        
        @Override
        public void completed(Integer bytesWritten, ClientContext context) {
            System.out.println("连接#" + context.connectionId + " 响应发送完成，字节数: " + bytesWritten);
            
            // 写入完成后，继续读取下一个消息
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            context.channel.read(buffer, context, new ReadHandler());
        }
        
        @Override
        public void failed(Throwable exc, ClientContext context) {
            System.err.println("连接#" + context.connectionId + " 写入数据失败: " + exc.getMessage());
            try {
                context.channel.close();
            } catch (IOException e) {
                System.err.println("关闭连接时出错: " + e.getMessage());
            }
        }
    }
    
    /**
     * 模拟主程序的其他工作
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
                try {
                    AsynchronousSocketChannel clientChannel = AsynchronousSocketChannel.open();
                    
                    // 异步连接到服务器
                    clientChannel.connect(new InetSocketAddress("localhost", PORT), 
                        null, new CompletionHandler<Void, Void>() {
                        
                        @Override
                        public void completed(Void result, Void attachment) {
                            try {
                                System.out.println("客户端" + clientId + " 连接成功");
                                
                                // 异步发送消息
                                String message = "Hello from AIO client " + clientId;
                                ByteBuffer buffer = ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8));
                                
                                clientChannel.write(buffer, null, 
                                    new CompletionHandler<Integer, Void>() {
                                    
                                    @Override
                                    public void completed(Integer bytesWritten, Void attachment) {
                                        // 异步读取响应
                                        ByteBuffer responseBuffer = ByteBuffer.allocate(BUFFER_SIZE);
                                        clientChannel.read(responseBuffer, null, 
                                            new CompletionHandler<Integer, Void>() {
                                            
                                            @Override
                                            public void completed(Integer bytesRead, Void attachment) {
                                                if (bytesRead > 0) {
                                                    responseBuffer.flip();
                                                    String response = StandardCharsets.UTF_8.decode(responseBuffer).toString();
                                                    System.out.println("客户端" + clientId + " 收到响应: " + response);
                                                }
                                                
                                                // 发送结束消息
                                                ByteBuffer byeBuffer = ByteBuffer.wrap("bye".getBytes(StandardCharsets.UTF_8));
                                                clientChannel.write(byeBuffer, null, 
                                                    new CompletionHandler<Integer, Void>() {
                                                    
                                                    @Override
                                                    public void completed(Integer result, Void attachment) {
                                                        try {
                                                            clientChannel.close();
                                                        } catch (IOException e) {
                                                            System.err.println("关闭客户端连接时出错: " + e.getMessage());
                                                        }
                                                    }
                                                    
                                                    @Override
                                                    public void failed(Throwable exc, Void attachment) {
                                                        System.err.println("客户端" + clientId + " 发送bye消息失败: " + exc.getMessage());
                                                    }
                                                });
                                            }
                                            
                                            @Override
                                            public void failed(Throwable exc, Void attachment) {
                                                System.err.println("客户端" + clientId + " 读取响应失败: " + exc.getMessage());
                                            }
                                        });
                                    }
                                    
                                    @Override
                                    public void failed(Throwable exc, Void attachment) {
                                        System.err.println("客户端" + clientId + " 发送消息失败: " + exc.getMessage());
                                    }
                                });
                                
                            } catch (Exception e) {
                                System.err.println("客户端" + clientId + " 处理连接时出错: " + e.getMessage());
                            }
                        }
                        
                        @Override
                        public void failed(Throwable exc, Void attachment) {
                            System.err.println("客户端" + clientId + " 连接失败: " + exc.getMessage());
                        }
                    });
                    
                    // 保持客户端线程运行一段时间
                    TimeUnit.SECONDS.sleep(3);
                    
                } catch (IOException | InterruptedException e) {
                    System.err.println("客户端" + clientId + " 错误: " + e.getMessage());
                }
            }).start();
        }
    }
}

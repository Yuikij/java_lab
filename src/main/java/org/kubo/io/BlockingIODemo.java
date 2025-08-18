package org.kubo.io;

import java.io.*;
import java.net.*;
import java.util.concurrent.TimeUnit;

/**
 * 阻塞I/O (Blocking I/O, BIO) 模型演示
 * 
 * 特性：
 * 1. 线程在执行I/O操作时会被阻塞，直到操作完成
 * 2. 一个连接需要一个线程来处理
 * 3. 适合连接数较少且固定的场景
 * 4. 编程模型简单，易于理解
 */
public class BlockingIODemo {
    
    private static final int PORT = 8080;
    
    public static void main(String[] args) {
        System.out.println("=== 阻塞I/O (BIO) 模型演示 ===");
        
        // 启动服务器
        new Thread(BlockingIODemo::startServer).start();
        
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
     * BIO服务器实现
     */
    public static void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("BIO服务器启动，监听端口: " + PORT);
            
            while (true) {
                // 阻塞等待客户端连接
                Socket clientSocket = serverSocket.accept();
                System.out.println("接受新连接: " + clientSocket.getRemoteSocketAddress());
                
                // 为每个连接创建新线程处理
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("服务器错误: " + e.getMessage());
        }
    }
    
    /**
     * 处理客户端连接（阻塞方式）
     */
    private static void handleClient(Socket clientSocket) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(
                clientSocket.getOutputStream(), true)) {
            
            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                System.out.println("收到消息: " + inputLine);
                
                // 模拟处理时间
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                
                // 回应客户端
                writer.println("Echo: " + inputLine);
                
                if ("bye".equalsIgnoreCase(inputLine)) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("处理客户端连接时出错: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("关闭客户端连接时出错: " + e.getMessage());
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
                try (Socket socket = new Socket("localhost", PORT);
                     PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                     BufferedReader reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()))) {
                    
                    // 发送消息
                    String message = "Hello from client " + clientId;
                    writer.println(message);
                    
                    // 读取响应（阻塞）
                    String response = reader.readLine();
                    System.out.println("客户端" + clientId + "收到响应: " + response);
                    
                    // 发送结束消息
                    writer.println("bye");
                    String finalResponse = reader.readLine();
                    System.out.println("客户端" + clientId + "收到最终响应: " + finalResponse);
                    
                } catch (IOException e) {
                    System.err.println("客户端" + clientId + "连接错误: " + e.getMessage());
                }
            }).start();
        }
    }
}

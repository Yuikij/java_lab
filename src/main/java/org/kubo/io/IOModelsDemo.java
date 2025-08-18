package org.kubo.io;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Java五种I/O模型演示主程序
 * 
 * 包含以下五种I/O模型：
 * 1. 阻塞I/O (Blocking I/O, BIO)
 * 2. 非阻塞I/O (Non-blocking I/O, NIO)
 * 3. I/O多路复用 (I/O Multiplexing)
 * 4. 信号驱动I/O (Signal-Driven I/O)
 * 5. 异步I/O (Asynchronous I/O, AIO)
 */
public class IOModelsDemo {
    
    public static void main(String[] args) {
        System.out.println("===============================================");
        System.out.println("        Java五种I/O模型演示程序");
        System.out.println("===============================================");
        
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
            printMenu();
            System.out.print("请选择要演示的I/O模型 (1-6): ");
            
            try {
                int choice = scanner.nextInt();
                
                switch (choice) {
                    case 1:
                        runBlockingIODemo();
                        break;
                    case 2:
                        runNonBlockingIODemo();
                        break;
                    case 3:
                        runIOMultiplexingDemo();
                        break;
                    case 4:
                        runSignalDrivenIODemo();
                        break;
                    case 5:
                        runAsynchronousIODemo();
                        break;
                    case 6:
                        System.out.println("感谢使用！再见！");
                        return;
                    default:
                        System.out.println("无效选择，请输入1-6之间的数字。");
                        continue;
                }
                
                System.out.println("\n演示完成，等待5秒后返回主菜单...");
                TimeUnit.SECONDS.sleep(5);
                System.out.println("\n" + "=".repeat(50) + "\n");
                
                } catch (Exception e) {
                    System.out.println("输入错误，请输入有效的数字。");
                    scanner.nextLine(); // 清除无效输入
                }
            }
        } catch (Exception e) {
            System.err.println("程序执行错误: " + e.getMessage());
        }
    }
    
    /**
     * 打印菜单
     */
    private static void printMenu() {
        System.out.println("请选择要演示的I/O模型：");
        System.out.println("1. 阻塞I/O (BIO) - 端口8080");
        System.out.println("2. 非阻塞I/O (NIO) - 端口8081");
        System.out.println("3. I/O多路复用 - 端口8082");
        System.out.println("4. 信号驱动I/O - 端口8083");
        System.out.println("5. 异步I/O (AIO) - 端口8084");
        System.out.println("6. 退出程序");
        System.out.println("-----------------------------------------------");
    }
    
    /**
     * 运行阻塞I/O演示
     */
    private static void runBlockingIODemo() {
        System.out.println("\n🚀 启动阻塞I/O (BIO) 演示...");
        printIOModelInfo("阻塞I/O (BIO)", 
            "• 线程在执行I/O操作时会被阻塞，直到操作完成",
            "• 一个连接需要一个线程来处理",
            "• 适合连接数较少且固定的场景",
            "• 编程模型简单，易于理解"
        );
        
        try {
            BlockingIODemo.main(new String[]{});
        } catch (Exception e) {
            System.err.println("阻塞I/O演示出错: " + e.getMessage());
        }
    }
    
    /**
     * 运行非阻塞I/O演示
     */
    private static void runNonBlockingIODemo() {
        System.out.println("\n🚀 启动非阻塞I/O (NIO) 演示...");
        printIOModelInfo("非阻塞I/O (NIO)",
            "• 应用程序主动轮询内核，检查I/O操作是否就绪",
            "• 如果没有就绪的I/O操作，立即返回，不会阻塞",
            "• 需要应用程序不断轮询，会消耗CPU资源",
            "• 单线程可以处理多个连接"
        );
        
        try {
            NonBlockingIODemo.main(new String[]{});
        } catch (Exception e) {
            System.err.println("非阻塞I/O演示出错: " + e.getMessage());
        }
    }
    
    /**
     * 运行I/O多路复用演示
     */
    private static void runIOMultiplexingDemo() {
        System.out.println("\n🚀 启动I/O多路复用演示...");
        printIOModelInfo("I/O多路复用",
            "• 使用select/poll/epoll等系统调用监控多个I/O流",
            "• 单个线程可以同时监控多个连接的I/O状态",
            "• 当有I/O事件就绪时，系统会通知应用程序",
            "• 避免了轮询带来的CPU浪费，适合高并发场景"
        );
        
        try {
            IOMultiplexingDemo.main(new String[]{});
        } catch (Exception e) {
            System.err.println("I/O多路复用演示出错: " + e.getMessage());
        }
    }
    
    /**
     * 运行信号驱动I/O演示
     */
    private static void runSignalDrivenIODemo() {
        System.out.println("\n🚀 启动信号驱动I/O演示...");
        printIOModelInfo("信号驱动I/O",
            "• 应用程序安装信号处理函数，当I/O就绪时内核发送SIGIO信号",
            "• 应用程序在信号处理函数中处理I/O操作",
            "• 主程序可以继续执行其他工作，不会被I/O操作阻塞",
            "• 实际应用较少，因为信号处理相对复杂"
        );
        
        try {
            SignalDrivenIODemo.main(new String[]{});
        } catch (Exception e) {
            System.err.println("信号驱动I/O演示出错: " + e.getMessage());
        }
    }
    
    /**
     * 运行异步I/O演示
     */
    private static void runAsynchronousIODemo() {
        System.out.println("\n🚀 启动异步I/O (AIO) 演示...");
        printIOModelInfo("异步I/O (AIO)",
            "• 应用程序发起I/O操作后立即返回，不阻塞",
            "• 当I/O操作完成时，内核会通知应用程序（通过回调函数）",
            "• 真正的异步，应用程序无需轮询或阻塞等待",
            "• 适合高并发、高吞吐量的场景，但编程复杂度较高"
        );
        
        try {
            AsynchronousIODemo.main(new String[]{});
        } catch (Exception e) {
            System.err.println("异步I/O演示出错: " + e.getMessage());
        }
    }
    
    /**
     * 打印I/O模型信息
     */
    private static void printIOModelInfo(String modelName, String... features) {
        System.out.println("📋 " + modelName + " 特性：");
        for (String feature : features) {
            System.out.println("   " + feature);
        }
        System.out.println("-----------------------------------------------");
    }
}

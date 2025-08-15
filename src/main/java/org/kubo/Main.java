package org.kubo;

import java.util.Scanner;
import org.kubo.collections.ArrayVsLinkedListDemo;
import org.kubo.reactor.ReactorDemo;
import org.kubo.reactor.ReactorPattern;
import org.kubo.reactor.ReactorPerformanceTest;

// 运行方式：在 IDE 中运行 Main.main，或使用 Maven 执行
public class Main {
    public static void main(String[] args) {
        // 示例输出
        System.out.printf("你好，欢迎使用！\n");
        
        // 显示菜单
        showMenu();
        
        try (Scanner scanner = new Scanner(System.in)) {
        
        while (true) {
            System.out.print("\n请选择要运行的演示 (输入数字): ");
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    runCollectionsDemo();
                    break;
                case "2":
                    runReactorPatternInfo();
                    break;
                case "3":
                    runReactorDemo();
                    break;
                case "4":
                    runReactorPerformanceTest();
                    break;
                case "0":
                    System.out.println("再见！");
                    return;
                default:
                    System.out.println("无效选择，请重新输入！");
                    break;
            }
            
            // 显示菜单
            showMenu();
        }
    }
    }
    
    private static void showMenu() {
        System.out.println("\n==========================================");
        System.out.println("           Java实验室演示菜单");
        System.out.println("==========================================");
        System.out.println("1. 集合类性能对比演示");
        System.out.println("2. Reactor线程模型原理说明");
        System.out.println("3. Reactor线程模型演示");
        System.out.println("4. Reactor性能对比测试");
        System.out.println("0. 退出");
        System.out.println("==========================================");
    }
    
    private static void runCollectionsDemo() {
        System.out.println("\n>>> 运行集合类性能对比演示...");
        ArrayVsLinkedListDemo.run();
    }
    
    private static void runReactorPatternInfo() {
        System.out.println("\n>>> 显示Reactor线程模型原理说明...");
        ReactorPattern.printReactorPatternInfo();
    }
    
    private static void runReactorDemo() {
        System.out.println("\n>>> 运行Reactor线程模型演示...");
        ReactorDemo.main(new String[]{});
    }
    
    private static void runReactorPerformanceTest() {
        System.out.println("\n>>> 运行Reactor性能对比测试...");
        System.out.println("注意：这个测试可能需要较长时间，请耐心等待...");
        ReactorPerformanceTest.main(new String[]{});
    }
}
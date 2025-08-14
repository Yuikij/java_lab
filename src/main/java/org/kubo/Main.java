package org.kubo;

import java.util.ArrayList;
import java.util.List;
import org.kubo.collections.ArrayVsLinkedListDemo;

// 运行方式：在 IDE 中运行 Main.main，或使用 Maven 执行
public class Main {
    public static void main(String[] args) {
        // 示例输出
        System.out.printf("你好，欢迎使用！\n");
        List<String> list = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            // 简单循环输出
            System.out.println("i = " + i);
        }

        ArrayVsLinkedListDemo.run();
    }
}
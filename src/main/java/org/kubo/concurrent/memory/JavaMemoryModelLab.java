package org.kubo.concurrent.memory;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Java内存模型（JMM）实验室主程序
 * 
 * 这个实验室提供了Java内存模型相关的综合演示，包括：
 * 1. JMM核心概念（主内存、工作内存、volatile、synchronized等）
 * 2. Happens-Before规则的详细演示
 * 3. 内存屏障的作用和实现
 * 4. 双重检查锁定模式的问题和解决方案
 * 5. 伪共享问题的分析和优化
 * 6. CPU缓存模型对程序性能的影响
 * 7. 并发编程三大特性（原子性、可见性、有序性）
 * 
 * @author kubo
 */
public class JavaMemoryModelLab {
    
    public static void main(String[] args) {
        System.out.println("===============================================");
        System.out.println("          Java内存模型（JMM）实验室");
        System.out.println("===============================================");
        System.out.println("欢迎来到Java内存模型实验室！");
        System.out.println("本实验室将帮助您深入理解Java内存模型的核心概念。");
        System.out.println();
        
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                printMainMenu();
                System.out.print("请选择要运行的实验 (1-8): ");
                
                try {
                    int choice = scanner.nextInt();
                    
                    switch (choice) {
                        case 1:
                            runJMMCoreConceptsDemo();
                            break;
                        case 2:
                            runHappensBeforeDemo();
                            break;
                        case 3:
                            runMemoryBarrierDemo();
                            break;
                        case 4:
                            runDoubleCheckedLockingDemo();
                            break;
                        case 5:
                            runFalseSharingDemo();
                            break;
                        case 6:
                            runCPUCacheDemo();
                            break;
                        case 7:
                            runConcurrencyPropertiesDemo();
                            break;
                        case 8:
                            System.out.println("\n感谢使用Java内存模型实验室！");
                            System.out.println("希望这些演示帮助您更好地理解JMM和并发编程。");
                            return;
                        default:
                            System.out.println("无效选择，请输入1-8之间的数字。");
                            continue;
                    }
                    
                    System.out.println("\n实验完成！");
                    System.out.print("按回车键返回主菜单...");
                    scanner.nextLine(); // 消费换行符
                    scanner.nextLine(); // 等待用户按回车
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
     * 打印主菜单
     */
    private static void printMainMenu() {
        System.out.println("🧠 Java内存模型实验菜单：");
        System.out.println("───────────────────────────────────────────────");
        System.out.println("1. 🏗️  JMM核心概念演示");
        System.out.println("   - 主内存与工作内存");
        System.out.println("   - volatile内存语义");
        System.out.println("   - synchronized内存语义");
        System.out.println("   - final内存语义");
        System.out.println("   - 对象构造过程内存模型");
        System.out.println();
        
        System.out.println("2. 🔗  Happens-Before规则演示");
        System.out.println("   - 程序顺序规则");
        System.out.println("   - 监视器锁规则");
        System.out.println("   - volatile变量规则");
        System.out.println("   - 线程启动/终止/中断规则");
        System.out.println("   - 传递性规则");
        System.out.println();
        
        System.out.println("3. 🚧  内存屏障演示");
        System.out.println("   - volatile的内存屏障效果");
        System.out.println("   - StoreLoad/StoreStore/LoadLoad屏障");
        System.out.println("   - 重排序问题和解决方案");
        System.out.println("   - Unsafe内存屏障方法");
        System.out.println();
        
        System.out.println("4. 🔒  双重检查锁定模式");
        System.out.println("   - 错误实现的问题");
        System.out.println("   - volatile的正确使用");
        System.out.println("   - 静态内部类解决方案");
        System.out.println("   - 枚举单例最佳实践");
        System.out.println();
        
        System.out.println("5. 👥  伪共享问题演示");
        System.out.println("   - 缓存行冲突原理");
        System.out.println("   - 缓存行填充解决方案");
        System.out.println("   - @Contended注解使用");
        System.out.println("   - 性能对比分析");
        System.out.println();
        
        System.out.println("6. 💾  CPU缓存模型演示");
        System.out.println("   - 多级缓存架构");
        System.out.println("   - 局部性原理");
        System.out.println("   - 缓存一致性开销");
        System.out.println("   - 缓存友好编程");
        System.out.println();
        
        System.out.println("7. ⚖️  并发编程三大特性");
        System.out.println("   - 原子性演示");
        System.out.println("   - 可见性演示");
        System.out.println("   - 有序性演示");
        System.out.println("   - 综合特性对比");
        System.out.println();
        
        System.out.println("8. 🚪  退出实验室");
        System.out.println("───────────────────────────────────────────────");
    }
    
    /**
     * 运行JMM核心概念演示
     */
    private static void runJMMCoreConceptsDemo() {
        System.out.println("\n🏗️ 启动JMM核心概念演示...");
        printExperimentInfo(
            "Java内存模型核心概念",
            "本实验将演示JMM的核心概念，包括主内存与工作内存的关系、",
            "volatile/synchronized/final的内存语义，以及对象构造过程中的内存模型问题。"
        );
        
        try {
            JavaMemoryModelDemo.main(new String[]{});
        } catch (Exception e) {
            System.err.println("JMM核心概念演示出错: " + e.getMessage());
        }
    }
    
    /**
     * 运行Happens-Before规则演示
     */
    private static void runHappensBeforeDemo() {
        System.out.println("\n🔗 启动Happens-Before规则演示...");
        printExperimentInfo(
            "Happens-Before规则",
            "本实验将详细演示JMM中的Happens-Before规则，这是理解并发程序",
            "内存可见性和操作顺序的关键概念。"
        );
        
        try {
            HappensBeforeDemo.main(new String[]{});
        } catch (Exception e) {
            System.err.println("Happens-Before规则演示出错: " + e.getMessage());
        }
    }
    
    /**
     * 运行内存屏障演示
     */
    private static void runMemoryBarrierDemo() {
        System.out.println("\n🚧 启动内存屏障演示...");
        printExperimentInfo(
            "内存屏障机制",
            "本实验将演示内存屏障如何防止指令重排序，保证内存操作的顺序性，",
            "以及volatile关键字如何通过内存屏障实现其语义。"
        );
        
        try {
            MemoryBarrierDemo.main(new String[]{});
        } catch (Exception e) {
            System.err.println("内存屏障演示出错: " + e.getMessage());
        }
    }
    
    /**
     * 运行双重检查锁定模式演示
     */
    private static void runDoubleCheckedLockingDemo() {
        System.out.println("\n🔒 启动双重检查锁定模式演示...");
        printExperimentInfo(
            "双重检查锁定模式",
            "本实验将演示DCL模式中的指令重排序问题，以及如何使用volatile、",
            "静态内部类、枚举等方式正确实现线程安全的延迟初始化。"
        );
        
        try {
            DoubleCheckedLockingDemo.main(new String[]{});
        } catch (Exception e) {
            System.err.println("双重检查锁定演示出错: " + e.getMessage());
        }
    }
    
    /**
     * 运行伪共享问题演示
     */
    private static void runFalseSharingDemo() {
        System.out.println("\n👥 启动伪共享问题演示...");
        printExperimentInfo(
            "伪共享性能问题",
            "本实验将演示CPU缓存行冲突导致的伪共享问题，以及如何通过",
            "缓存行填充、@Contended注解等方式解决性能问题。"
        );
        
        try {
            FalseSharingDemo.main(new String[]{});
        } catch (Exception e) {
            System.err.println("伪共享问题演示出错: " + e.getMessage());
        }
    }
    
    /**
     * 运行CPU缓存模型演示
     */
    private static void runCPUCacheDemo() {
        System.out.println("\n💾 启动CPU缓存模型演示...");
        printExperimentInfo(
            "CPU缓存模型",
            "本实验将演示现代CPU的多级缓存架构、局部性原理、缓存一致性",
            "等概念，以及如何编写缓存友好的程序提升性能。"
        );
        
        try {
            CPUCacheDemo.main(new String[]{});
        } catch (Exception e) {
            System.err.println("CPU缓存模型演示出错: " + e.getMessage());
        }
    }
    
    /**
     * 运行并发编程三大特性演示
     */
    private static void runConcurrencyPropertiesDemo() {
        System.out.println("\n⚖️ 启动并发编程三大特性演示...");
        printExperimentInfo(
            "并发编程三大特性",
            "本实验将演示并发编程中的原子性、可见性、有序性三大特性，",
            "以及各种同步机制如何保证这些特性。"
        );
        
        try {
            ConcurrencyPropertiesTestMain.main(new String[]{});
        } catch (Exception e) {
            System.err.println("并发编程特性演示出错: " + e.getMessage());
        }
    }
    
    /**
     * 打印实验信息
     */
    private static void printExperimentInfo(String title, String... descriptions) {
        System.out.println("📋 实验: " + title);
        System.out.println("─".repeat(50));
        for (String desc : descriptions) {
            System.out.println("   " + desc);
        }
        System.out.println("─".repeat(50));
        
        // 短暂延迟，让用户看到实验信息
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

/**
 * JMM实验统计和总结
 */
class JMMExperimentSummary {
    
    /**
     * 打印JMM知识点总结
     */
    public static void printJMMSummary() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("             Java内存模型（JMM）知识点总结");
        System.out.println("=".repeat(60));
        
        System.out.println("\n🧠 核心概念");
        System.out.println("───────────────────────────────────────────────────────");
        System.out.println("• 主内存: 所有线程共享的内存区域，存储共享变量的主拷贝");
        System.out.println("• 工作内存: 每个线程私有的内存区域，存储共享变量的本地拷贝");
        System.out.println("• 内存交互操作: lock、unlock、read、load、use、assign、store、write");
        
        System.out.println("\n🔗 Happens-Before规则");
        System.out.println("───────────────────────────────────────────────────────");
        System.out.println("1. 程序顺序规则: 同一线程内的操作按程序顺序执行");
        System.out.println("2. 监视器锁规则: unlock操作 happens-before 后续lock操作");
        System.out.println("3. volatile变量规则: volatile写 happens-before 后续volatile读");
        System.out.println("4. 线程启动规则: Thread.start() happens-before 线程内所有操作");
        System.out.println("5. 线程终止规则: 线程所有操作 happens-before 检测到线程终止");
        System.out.println("6. 线程中断规则: interrupt() happens-before 检测到中断");
        System.out.println("7. 对象终结规则: 构造完成 happens-before finalize()");
        System.out.println("8. 传递性规则: A hb B && B hb C => A hb C");
        
        System.out.println("\n🚧 内存屏障");
        System.out.println("───────────────────────────────────────────────────────");
        System.out.println("• LoadLoad屏障: 确保前面的读操作在后面的读操作之前完成");
        System.out.println("• StoreStore屏障: 确保前面的写操作在后面的写操作之前完成");
        System.out.println("• LoadStore屏障: 确保前面的读操作在后面的写操作之前完成");
        System.out.println("• StoreLoad屏障: 确保前面的写操作在后面的读操作之前完成");
        
        System.out.println("\n⚖️ 并发编程三大特性");
        System.out.println("───────────────────────────────────────────────────────");
        System.out.println("• 原子性: 操作要么全部执行，要么全部不执行");
        System.out.println("  - 解决方案: AtomicXXX、synchronized、Lock");
        System.out.println("• 可见性: 一个线程的修改能被其他线程立即看到");
        System.out.println("  - 解决方案: volatile、synchronized、Lock");
        System.out.println("• 有序性: 程序按照代码顺序执行");
        System.out.println("  - 解决方案: volatile内存屏障、synchronized、Lock");
        
        System.out.println("\n🔧 关键字语义");
        System.out.println("───────────────────────────────────────────────────────");
        System.out.println("• volatile:");
        System.out.println("  - 保证可见性: 修改立即同步到主内存");
        System.out.println("  - 保证有序性: 禁止特定的指令重排序");
        System.out.println("  - 不保证原子性: 复合操作仍需要额外同步");
        System.out.println("• synchronized:");
        System.out.println("  - 保证原子性: 同步块内操作具有原子性");
        System.out.println("  - 保证可见性: 进入时从主内存读取，退出时写回主内存");
        System.out.println("  - 保证有序性: 同步块内外操作不会重排序");
        System.out.println("• final:");
        System.out.println("  - 构造安全性: 对象构造完成后对其他线程可见");
        System.out.println("  - 不可变性: 一旦初始化完成就不能修改");
        
        System.out.println("\n💡 最佳实践");
        System.out.println("───────────────────────────────────────────────────────");
        System.out.println("1. 优先使用不可变对象和final字段");
        System.out.println("2. 合理使用volatile关键字保证可见性");
        System.out.println("3. 正确使用synchronized或Lock保证线程安全");
        System.out.println("4. 使用AtomicXXX类进行无锁编程");
        System.out.println("5. 避免伪共享，合理设计数据结构");
        System.out.println("6. 理解CPU缓存，编写缓存友好的代码");
        System.out.println("7. 遵循happens-before规则确保程序正确性");
        
        System.out.println("\n" + "=".repeat(60));
    }
}

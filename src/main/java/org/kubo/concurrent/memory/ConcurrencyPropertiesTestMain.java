package org.kubo.concurrent.memory;

import java.util.Scanner;

/**
 * Java并发编程三大特性测试主类
 * 
 * 这个类提供了一个交互式菜单，让用户可以选择运行不同的并发特性演示：
 * 1. 原子性 (Atomicity) 演示
 * 2. 可见性 (Visibility) 演示  
 * 3. 有序性 (Ordering) 演示
 * 4. 综合演示
 * 
 * @author kubo
 */
public class ConcurrencyPropertiesTestMain {

    public static void main(String[] args) {
        System.out.println("=======================================================");
        System.out.println("         Java并发编程三大特性测试实验室");
        System.out.println("=======================================================");
        System.out.println("本实验室将演示Java并发编程中最重要的三个概念：");
        System.out.println("• 原子性 (Atomicity) - 操作的不可分割性");
        System.out.println("• 可见性 (Visibility) - 变量修改的即时可见性");
        System.out.println("• 有序性 (Ordering) - 程序执行的顺序保证");
        System.out.println("=======================================================");
        
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            showMenu();
            System.out.print("请选择要运行的演示 (输入数字): ");
            
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    runAtomicityDemo();
                    break;
                case "2":
                    runVisibilityDemo();
                    break;
                case "3":
                    runOrderingDemo();
                    break;
                case "4":
                    runComprehensiveDemo();
                    break;
                case "5":
                    showConceptExplanation();
                    break;
                case "0":
                    System.out.println("\n感谢使用Java并发编程实验室！");
                    scanner.close();
                    return;
                default:
                    System.out.println("❌ 无效选择，请重新输入！");
                    break;
            }
            
            System.out.println("\n按回车键继续...");
            scanner.nextLine();
        }
    }
    
    private static void showMenu() {
        System.out.println("\n═══════════════════════════════════════════════════════");
        System.out.println("                    选择演示内容");
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("1. 🔒 原子性 (Atomicity) 演示");
        System.out.println("   - 演示非原子操作的并发问题");
        System.out.println("   - 展示原子类和同步机制的解决方案");
        System.out.println();
        System.out.println("2. 👁 可见性 (Visibility) 演示");
        System.out.println("   - 演示变量修改的可见性问题");
        System.out.println("   - 展示volatile和synchronized的解决方案");
        System.out.println();
        System.out.println("3. 📋 有序性 (Ordering) 演示");
        System.out.println("   - 演示指令重排序现象");
        System.out.println("   - 展示内存屏障和同步机制的作用");
        System.out.println();
        System.out.println("4. 🎯 综合演示");
        System.out.println("   - 运行所有三个特性的完整演示");
        System.out.println();
        System.out.println("5. 📚 概念说明");
        System.out.println("   - 查看详细的理论解释");
        System.out.println();
        System.out.println("0. 🚪 退出程序");
        System.out.println("═══════════════════════════════════════════════════════");
    }
    
    private static void runAtomicityDemo() {
        System.out.println("\n🔒 启动原子性演示...");
        System.out.println("─────────────────────────────────────────────────────");
        
        AtomicityDemo demo = new AtomicityDemo();
        demo.runAtomicityTest();
        
        System.out.println("─────────────────────────────────────────────────────");
        System.out.println("✅ 原子性演示完成");
        System.out.println("💡 总结：使用AtomicInteger、synchronized或锁可以保证操作的原子性");
    }
    
    private static void runVisibilityDemo() {
        System.out.println("\n👁 启动可见性演示...");
        System.out.println("─────────────────────────────────────────────────────");
        
        VisibilityDemo demo = new VisibilityDemo();
        demo.runVisibilityTest();
        
        System.out.println("─────────────────────────────────────────────────────");
        System.out.println("✅ 可见性演示完成");
        System.out.println("💡 总结：使用volatile、synchronized或锁可以保证变量修改的可见性");
    }
    
    private static void runOrderingDemo() {
        System.out.println("\n📋 启动有序性演示...");
        System.out.println("─────────────────────────────────────────────────────");
        
        OrderingDemo demo = new OrderingDemo();
        demo.runOrderingTest();
        
        System.out.println("─────────────────────────────────────────────────────");
        System.out.println("✅ 有序性演示完成");
        System.out.println("💡 总结：使用volatile、synchronized或锁可以防止指令重排序");
    }
    
    private static void runComprehensiveDemo() {
        System.out.println("\n🎯 启动综合演示...");
        System.out.println("═══════════════════════════════════════════════════════");
        
        // 运行所有演示
        runAtomicityDemo();
        System.out.println();
        runVisibilityDemo();
        System.out.println();
        runOrderingDemo();
        
        System.out.println("\n═══════════════════════════════════════════════════════");
        System.out.println("🎉 所有演示完成！");
        
        showSummary();
    }
    
    private static void showSummary() {
        System.out.println("\n📋 Java并发编程三大特性总结");
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println();
        
        System.out.println("🔒 原子性 (Atomicity)");
        System.out.println("   定义：操作要么全部执行，要么全部不执行");
        System.out.println("   问题：多线程下的复合操作可能被中断");
        System.out.println("   解决：AtomicXXX类、synchronized、Lock");
        System.out.println();
        
        System.out.println("👁 可见性 (Visibility)");
        System.out.println("   定义：一个线程的修改能被其他线程立即看到");
        System.out.println("   问题：线程间的变量修改可能不可见");
        System.out.println("   解决：volatile、synchronized、Lock");
        System.out.println();
        
        System.out.println("📋 有序性 (Ordering)");
        System.out.println("   定义：程序按照代码顺序执行");
        System.out.println("   问题：编译器和CPU可能重排序指令");
        System.out.println("   解决：volatile内存屏障、synchronized、Lock");
        System.out.println();
        
        System.out.println("🎯 关键要点");
        System.out.println("   • volatile：保证可见性和有序性，不保证原子性");
        System.out.println("   • synchronized：三个特性都保证");
        System.out.println("   • AtomicXXX：保证原子性，某些操作保证可见性");
        System.out.println("   • Lock：三个特性都保证（正确使用时）");
        System.out.println("═══════════════════════════════════════════════════════");
    }
    
    private static void showConceptExplanation() {
        System.out.println("\n📚 Java并发编程概念详解");
        System.out.println("═══════════════════════════════════════════════════════");
        
        System.out.println("\n🧠 Java内存模型 (JMM - Java Memory Model)");
        System.out.println("───────────────────────────────────────────────────────");
        System.out.println("Java内存模型定义了线程与内存的交互方式：");
        System.out.println("• 主内存：存储所有变量的主副本");
        System.out.println("• 工作内存：每个线程的私有内存，存储变量的副本");
        System.out.println("• 线程只能直接访问工作内存，不能直接访问主内存");
        System.out.println("• 变量值的传递需要通过主内存完成");
        
        System.out.println("\n🔒 原子性 (Atomicity) 详解");
        System.out.println("───────────────────────────────────────────────────────");
        System.out.println("原子性确保操作的不可分割性：");
        System.out.println("• 问题原因：复合操作（如i++）包含多个步骤");
        System.out.println("  - 读取变量值");
        System.out.println("  - 执行运算");
        System.out.println("  - 写回结果");
        System.out.println("• 解决方案：");
        System.out.println("  - AtomicInteger等原子类（CAS机制）");
        System.out.println("  - synchronized关键字（互斥锁）");
        System.out.println("  - ReentrantLock等显式锁");
        
        System.out.println("\n👁 可见性 (Visibility) 详解");
        System.out.println("───────────────────────────────────────────────────────");
        System.out.println("可见性确保修改能被其他线程看到：");
        System.out.println("• 问题原因：线程间的工作内存独立");
        System.out.println("  - 线程修改只在自己的工作内存中");
        System.out.println("  - 修改可能不会及时同步到主内存");
        System.out.println("  - 其他线程可能读取到过期值");
        System.out.println("• 解决方案：");
        System.out.println("  - volatile关键字（禁用缓存）");
        System.out.println("  - synchronized关键字（内存同步）");
        System.out.println("  - Lock（显式内存同步）");
        
        System.out.println("\n📋 有序性 (Ordering) 详解");
        System.out.println("───────────────────────────────────────────────────────");
        System.out.println("有序性确保代码按预期顺序执行：");
        System.out.println("• 问题原因：指令重排序优化");
        System.out.println("  - 编译器重排序（编译期优化）");
        System.out.println("  - CPU重排序（运行期优化）");
        System.out.println("  - 内存系统重排序（缓存优化）");
        System.out.println("• 解决方案：");
        System.out.println("  - volatile（内存屏障）");
        System.out.println("  - synchronized（临界区保护）");
        System.out.println("  - happens-before规则");
        
        System.out.println("\n🛡 Happens-Before规则");
        System.out.println("───────────────────────────────────────────────────────");
        System.out.println("JMM通过happens-before规则保证有序性：");
        System.out.println("• 程序顺序规则：同一线程内按代码顺序");
        System.out.println("• 监视器锁规则：unlock happens-before 后续lock");
        System.out.println("• volatile规则：写 happens-before 后续读");
        System.out.println("• 传递性：A→B, B→C 则 A→C");
        System.out.println("• 线程启动规则：start() happens-before 线程内动作");
        System.out.println("• 线程终止规则：线程内动作 happens-before join()");
        
        System.out.println("\n🎯 实践建议");
        System.out.println("───────────────────────────────────────────────────────");
        System.out.println("• 尽量使用java.util.concurrent包下的工具类");
        System.out.println("• 能用AtomicXXX就不用synchronized");
        System.out.println("• 共享变量优先考虑volatile");
        System.out.println("• 复杂同步逻辑使用Lock");
        System.out.println("• 避免过度同步导致性能问题");
        System.out.println("• 使用ThreadLocal避免共享");
        
        System.out.println("═══════════════════════════════════════════════════════");
    }
}

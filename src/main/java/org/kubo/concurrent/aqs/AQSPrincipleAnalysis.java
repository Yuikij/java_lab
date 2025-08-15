package org.kubo.concurrent.aqs;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.LockSupport;

/**
 * AQS原理深度分析
 * 
 * AbstractQueuedSynchronizer (AQS) 是Java并发包的核心基础框架
 * 
 * 核心设计思想：
 * 1. 使用一个int类型的state变量表示同步状态
 * 2. 使用FIFO队列来管理等待线程（CLH队列的变种）
 * 3. 通过CAS操作保证state修改的原子性
 * 4. 使用LockSupport.park/unpark实现线程的阻塞和唤醒
 * 
 * 主要组件：
 * - 同步状态(state): volatile int，表示资源的状态
 * - 等待队列: 双向链表，管理等待的线程
 * - 条件队列: 支持条件变量的等待队列
 * 
 * 两种模式：
 * - 独占模式(Exclusive): 只允许一个线程获取资源，如ReentrantLock
 * - 共享模式(Shared): 允许多个线程同时获取资源，如CountDownLatch、Semaphore
 */
public class AQSPrincipleAnalysis {
    
    /**
     * 模拟AQS的核心数据结构 - 等待队列节点
     */
    public static class Node {
        static final int CANCELLED =  1;  // 节点被取消
        static final int SIGNAL    = -1;  // 后继节点需要唤醒
        static final int CONDITION = -2;  // 节点在条件队列中
        static final int PROPAGATE = -3;  // 共享模式下需要传播
        
        volatile int waitStatus;          // 等待状态
        volatile Node prev;               // 前驱节点
        volatile Node next;               // 后继节点
        volatile Thread thread;           // 等待的线程
        Node nextWaiter;                 // 条件队列的下一个节点
        
        Node() {}
        
        Node(Thread thread, Node mode) {
            this.nextWaiter = mode;
            this.thread = thread;
        }
        
        static final Node SHARED = new Node();     // 共享模式标记
        static final Node EXCLUSIVE = null;       // 独占模式标记
        static final Node SHARED_NODE = new Node(); // 共享节点
        
        final boolean isShared() {
            return nextWaiter == SHARED;
        }
        
        @Override
        public String toString() {
            return String.format("Node{thread=%s, waitStatus=%d, mode=%s}", 
                               thread != null ? thread.getName() : "null", 
                               waitStatus, 
                               isShared() ? "SHARED" : "EXCLUSIVE");
        }
    }
    
    /**
     * AQS原理演示 - 简化版本的AQS实现
     * 主要展示AQS的核心算法逻辑
     */
    public static class SimplifiedAQS {
        private volatile int state;           // 同步状态
        private volatile Node head;           // 队列头节点
        private volatile Node tail;           // 队列尾节点
        protected volatile Thread exclusiveOwnerThread; // 独占模式下的持有线程
        
        // 获取同步状态
        protected final int getState() {
            return state;
        }
        
        // 设置同步状态
        protected final void setState(int newState) {
            state = newState;
        }
        
        // CAS修改同步状态
        protected final boolean compareAndSetState(int expect, int update) {
            // 这里简化实现，实际使用Unsafe.compareAndSwapInt
            if (state == expect) {
                state = update;
                return true;
            }
            return false;
        }
        
        /**
         * 独占模式获取资源的核心算法
         */
        public final void acquire(int arg) {
            // 1. 尝试获取资源
            if (!tryAcquire(arg) &&
                // 2. 获取失败，将当前线程加入等待队列并阻塞
                acquireQueued(addWaiter(Node.EXCLUSIVE), arg)) {
                // 3. 如果等待过程中被中断，重新设置中断状态
                Thread.currentThread().interrupt();
            }
        }
        
        /**
         * 尝试获取资源 - 由子类实现具体逻辑
         */
        protected boolean tryAcquire(int arg) {
            throw new UnsupportedOperationException();
        }
        
        /**
         * 将线程包装成Node并加入等待队列尾部
         */
        private Node addWaiter(Node mode) {
            Node node = new Node(Thread.currentThread(), mode);
            
            // 快速尝试加入队列尾部
            Node pred = tail;
            if (pred != null) {
                node.prev = pred;
                if (compareAndSetTail(pred, node)) {
                    pred.next = node;
                    return node;
                }
            }
            
            // 快速失败，使用完整的入队操作
            enq(node);
            return node;
        }
        
        /**
         * 完整的入队操作，包含初始化队列
         */
        private Node enq(final Node node) {
            for (;;) {
                Node t = tail;
                if (t == null) { // 队列为空，需要初始化
                    if (compareAndSetHead(new Node())) {
                        tail = head;
                    }
                } else {
                    node.prev = t;
                    if (compareAndSetTail(t, node)) {
                        t.next = node;
                        return t;
                    }
                }
            }
        }
        
        /**
         * 在等待队列中等待获取资源
         */
        final boolean acquireQueued(final Node node, int arg) {
            boolean failed = true;
            try {
                boolean interrupted = false;
                for (;;) {
                    final Node p = node.prev;
                    // 如果前驱是头节点，尝试获取资源
                    if (p == head && tryAcquire(arg)) {
                        setHead(node);
                        p.next = null; // help GC
                        failed = false;
                        return interrupted;
                    }
                    
                    // 检查是否需要阻塞，需要的话就阻塞等待
                    if (shouldParkAfterFailedAcquire(p, node) &&
                        parkAndCheckInterrupt()) {
                        interrupted = true;
                    }
                }
            } finally {
                if (failed) {
                    cancelAcquire(node);
                }
            }
        }
        
        /**
         * 检查获取失败后是否需要阻塞
         */
        private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
            int ws = pred.waitStatus;
            if (ws == Node.SIGNAL) {
                // 前驱节点已经设置了SIGNAL，可以安全阻塞
                return true;
            }
            if (ws > 0) {
                // 前驱节点被取消，跳过这些节点
                do {
                    node.prev = pred = pred.prev;
                } while (pred.waitStatus > 0);
                pred.next = node;
            } else {
                // 设置前驱节点状态为SIGNAL
                compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
            }
            return false;
        }
        
        /**
         * 阻塞当前线程并检查中断状态
         */
        private final boolean parkAndCheckInterrupt() {
            LockSupport.park(this);
            return Thread.interrupted();
        }
        
        /**
         * 释放资源
         */
        public final boolean release(int arg) {
            if (tryRelease(arg)) {
                Node h = head;
                if (h != null && h.waitStatus != 0) {
                    unparkSuccessor(h);
                }
                return true;
            }
            return false;
        }
        
        /**
         * 尝试释放资源 - 由子类实现
         */
        protected boolean tryRelease(int arg) {
            throw new UnsupportedOperationException();
        }
        
        /**
         * 唤醒后继节点
         */
        private void unparkSuccessor(Node node) {
            int ws = node.waitStatus;
            if (ws < 0) {
                compareAndSetWaitStatus(node, ws, 0);
            }
            
            Node s = node.next;
            if (s == null || s.waitStatus > 0) {
                s = null;
                // 从尾部向前查找最前面的有效节点
                for (Node t = tail; t != null && t != node; t = t.prev) {
                    if (t.waitStatus <= 0) {
                        s = t;
                    }
                }
            }
            if (s != null) {
                LockSupport.unpark(s.thread);
            }
        }
        
        /**
         * 取消节点的获取操作
         */
        private void cancelAcquire(Node node) {
            if (node == null) return;
            
            node.thread = null;
            
            // 跳过被取消的前驱节点
            Node pred = node.prev;
            while (pred.waitStatus > 0) {
                node.prev = pred = pred.prev;
            }
            
            Node predNext = pred.next;
            node.waitStatus = Node.CANCELLED;
            
            // 如果是尾节点，直接移除
            if (node == tail && compareAndSetTail(node, pred)) {
                compareAndSetNext(pred, predNext, null);
            } else {
                int ws;
                if (pred != head &&
                    ((ws = pred.waitStatus) == Node.SIGNAL ||
                     (ws <= 0 && compareAndSetWaitStatus(pred, ws, Node.SIGNAL))) &&
                    pred.thread != null) {
                    Node next = node.next;
                    if (next != null && next.waitStatus <= 0) {
                        compareAndSetNext(pred, predNext, next);
                    }
                } else {
                    unparkSuccessor(node);
                }
                
                node.next = node; // help GC
            }
        }
        
        // 简化的CAS操作方法
        private boolean compareAndSetHead(Node update) {
            if (head == null) {
                head = update;
                return true;
            }
            return false;
        }
        
        private boolean compareAndSetTail(Node expect, Node update) {
            if (tail == expect) {
                tail = update;
                return true;
            }
            return false;
        }
        
        private static boolean compareAndSetWaitStatus(Node node, int expect, int update) {
            if (node.waitStatus == expect) {
                node.waitStatus = update;
                return true;
            }
            return false;
        }
        
        private static boolean compareAndSetNext(Node node, Node expect, Node update) {
            if (node.next == expect) {
                node.next = update;
                return true;
            }
            return false;
        }
        
        private void setHead(Node node) {
            head = node;
            node.thread = null;
            node.prev = null;
        }
        
        /**
         * 获取队列状态信息
         */
        public String getQueueInfo() {
            StringBuilder sb = new StringBuilder();
            sb.append("Queue Status:\n");
            sb.append("State: ").append(state).append("\n");
            sb.append("Head: ").append(head != null ? head.toString() : "null").append("\n");
            sb.append("Tail: ").append(tail != null ? tail.toString() : "null").append("\n");
            
            if (head != null) {
                sb.append("Queue nodes: ");
                Node current = head.next;
                int count = 0;
                while (current != null && count < 10) { // 限制输出数量
                    sb.append(current.toString()).append(" -> ");
                    current = current.next;
                    count++;
                }
                if (current != null) {
                    sb.append("...");
                }
                sb.append("\n");
            }
            
            return sb.toString();
        }
    }
    
    /**
     * 基于SimplifiedAQS实现的简单互斥锁
     */
    public static class SimpleMutexLock extends SimplifiedAQS {
        
        @Override
        protected boolean tryAcquire(int arg) {
            if (compareAndSetState(0, 1)) {
                exclusiveOwnerThread = Thread.currentThread();
                return true;
            }
            return false;
        }
        
        @Override
        protected boolean tryRelease(int arg) {
            if (getState() == 0) {
                throw new IllegalMonitorStateException();
            }
            exclusiveOwnerThread = null;
            setState(0);
            return true;
        }
        
        public void lock() {
            acquire(1);
        }
        
        public void unlock() {
            release(1);
        }
        
        public boolean isLocked() {
            return getState() == 1;
        }
        
        public Thread getOwner() {
            return exclusiveOwnerThread;
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== AQS原理分析演示 ===\n");
        
        demonstrateAQSPrinciple();
    }
    
    /**
     * 演示AQS的核心原理
     */
    private static void demonstrateAQSPrinciple() throws InterruptedException {
        System.out.println("AQS核心原理演示：");
        System.out.println("1. 使用SimpleMutexLock演示队列的形成和消费");
        System.out.println("2. 观察线程在AQS队列中的状态变化\n");
        
        SimpleMutexLock mutex = new SimpleMutexLock();
        
        // 创建一个持有锁的线程
        Thread holder = new Thread(() -> {
            mutex.lock();
            try {
                System.out.println("持有者线程获取锁成功");
                System.out.println(mutex.getQueueInfo());
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                System.out.println("持有者线程释放锁");
                mutex.unlock();
            }
        }, "Holder");
        
        holder.start();
        Thread.sleep(100); // 确保holder先获取锁
        
        // 创建等待线程，观察队列形成
        Thread[] waiters = new Thread[3];
        for (int i = 0; i < waiters.length; i++) {
            final int id = i;
            waiters[i] = new Thread(() -> {
                System.out.println("等待线程 " + id + " 尝试获取锁");
                mutex.lock();
                try {
                    System.out.println("等待线程 " + id + " 获取锁成功");
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    System.out.println("等待线程 " + id + " 释放锁");
                    mutex.unlock();
                }
            }, "Waiter-" + i);
        }
        
        // 启动等待线程
        for (Thread waiter : waiters) {
            waiter.start();
            Thread.sleep(200); // 间隔启动，观察队列形成过程
            System.out.println("当前队列状态：");
            System.out.println(mutex.getQueueInfo());
        }
        
        // 等待所有线程完成
        holder.join();
        for (Thread waiter : waiters) {
            waiter.join();
        }
        
        System.out.println("最终队列状态：");
        System.out.println(mutex.getQueueInfo());
        
        printAQSPrinciplesSummary();
    }
    
    /**
     * 打印AQS原理总结
     */
    private static void printAQSPrinciplesSummary() {
        System.out.println("\n=== AQS核心原理总结 ===");
        System.out.println("1. 同步状态管理：");
        System.out.println("   - 使用volatile int state表示资源状态");
        System.out.println("   - 通过CAS操作保证state修改的原子性");
        System.out.println("   - 子类重写tryAcquire/tryRelease定义获取/释放逻辑");
        
        System.out.println("\n2. 等待队列管理：");
        System.out.println("   - 使用FIFO双向链表管理等待线程");
        System.out.println("   - 每个节点包含线程引用和等待状态");
        System.out.println("   - 头节点不包含线程，作为哨兵节点");
        
        System.out.println("\n3. 阻塞和唤醒机制：");
        System.out.println("   - 使用LockSupport.park()阻塞线程");
        System.out.println("   - 使用LockSupport.unpark()唤醒线程");
        System.out.println("   - 避免了Object.wait/notify的monitor依赖");
        
        System.out.println("\n4. 节点状态管理：");
        System.out.println("   - SIGNAL(-1): 后继节点需要唤醒");
        System.out.println("   - CANCELLED(1): 节点被取消");
        System.out.println("   - CONDITION(-2): 节点在条件队列中");
        System.out.println("   - PROPAGATE(-3): 共享模式下需要传播");
        
        System.out.println("\n5. 两种同步模式：");
        System.out.println("   - 独占模式: 只有一个线程能获取资源 (如ReentrantLock)");
        System.out.println("   - 共享模式: 多个线程可以同时获取资源 (如CountDownLatch)");
        
        System.out.println("\n6. 核心算法流程：");
        System.out.println("   获取资源: tryAcquire() -> 失败加入队列 -> park等待 -> 被唤醒重试");
        System.out.println("   释放资源: tryRelease() -> 成功则唤醒后继节点");
    }
}

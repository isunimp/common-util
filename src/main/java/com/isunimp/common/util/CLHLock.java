package com.isunimp.common.util;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * com.isunimp.common.util.CLHLock class
 *
 * @author renguiquan
 * @date 2019/8/8
 */
public class CLHLock {

    class CLHNode {
        private volatile boolean isLocked = true;
    }

    @SuppressWarnings("unused")
    private volatile CLHNode tail;
    private ThreadLocal<CLHNode> threadLocal = new ThreadLocal<>();
    private static final AtomicReferenceFieldUpdater<CLHLock, CLHNode> UPDATER = AtomicReferenceFieldUpdater
            .newUpdater(CLHLock.class, CLHNode.class, "tail");

    public void lock() {
        CLHNode currentThreadNode = new CLHNode();
        threadLocal.set(currentThreadNode);
        CLHNode preNode = UPDATER.getAndSet(this, currentThreadNode);
        if (preNode != null) {
            // 已有线程占用了锁，进入自旋
            while (preNode.isLocked) {
            }
        }
    }

    public void unlock() {
        CLHNode currentThreadNode = threadLocal.get();
        // 如果队列里只有当前线程，则释放对当前线程的引用（for GC）。
        if (!UPDATER.compareAndSet(this, currentThreadNode, null)) {
            // 还有后续线程
            // 改变状态，让后续线程结束自旋
            currentThreadNode.isLocked = false;
        }
    }

    public static void main(String[] args) {
        CLHLock lock = new CLHLock();

        for (Integer idx = 0; idx < 5; ++idx) {
            final String name = idx.toString();
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    lock.lock();
                    System.out.println(name);
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    lock.unlock();
                }
            });
            t.setDaemon(false);
            t.start();
        }
    }
}

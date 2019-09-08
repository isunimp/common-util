package com.isunimp.common.util;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * com.isunimp.common.util.CLHLock class
 *
 * @author renguiquan
 * @date 2019/8/8
 */
public class CLHLock {

    public static class CLHNode {
        // 默认是在等待锁
        private volatile boolean isLocked = true;
    }

    @SuppressWarnings("unused")
    private volatile CLHNode tail;
    private static final AtomicReferenceFieldUpdater<CLHLock, CLHNode> UPDATER = AtomicReferenceFieldUpdater
            .newUpdater(CLHLock.class, CLHNode.class, "tail");

    public void lock(CLHNode currentThread) {
        CLHNode preNode = UPDATER.getAndSet(this, currentThread);
        if (preNode != null) {
            //已有线程占用了锁，进入自旋
            while (preNode.isLocked) {
            }
        }
    }

    public void unlock(CLHNode currentThread) {
        // 如果队列里只有当前线程，则释放对当前线程的引用（for GC）。
        if (!UPDATER.compareAndSet(this, currentThread, null)) {
            // 还有后续线程
            // 改变状态，让后续线程结束自旋
            currentThread.isLocked = false;
        }
    }
}

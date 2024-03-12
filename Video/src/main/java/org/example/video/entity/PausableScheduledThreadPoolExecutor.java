package org.example.video.entity;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class PausableScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {
    private volatile AtomicBoolean pause;
    private ReentrantLock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();
    private long startTime;


    public PausableScheduledThreadPoolExecutor(int corePoolSize) {
        super(corePoolSize);
        pause=new AtomicBoolean(false);
    }

    public PausableScheduledThreadPoolExecutor(int corePoolSize, @NotNull ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
        pause=new AtomicBoolean(false);
    }

    public PausableScheduledThreadPoolExecutor(int corePoolSize, @NotNull RejectedExecutionHandler handler) {
        super(corePoolSize, handler);
        pause=new AtomicBoolean(false);
    }

    public PausableScheduledThreadPoolExecutor(int corePoolSize, @NotNull ThreadFactory threadFactory, @NotNull RejectedExecutionHandler handler) {
        super(corePoolSize, threadFactory, handler);
        pause=new AtomicBoolean(false);
    }
    public void pause(){
        pause.compareAndSet(false,true);
        startTime=System.currentTimeMillis();
    }
    public long unpause(){
        lock.lock();
        try {
            pause.compareAndSet(true, false);
            condition.signalAll();
        }finally {
            lock.unlock();
        }
        return System.currentTimeMillis()-startTime;
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        lock.lock();
        super.beforeExecute(t, r);
        try {
            while (pause.get()) {
                try {
                    condition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }finally {
            lock.unlock();
        }
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
    }

    public boolean isPause() {
        return pause.get();
    }

    public long getPauseStartTime() {
        return startTime;
    }
}

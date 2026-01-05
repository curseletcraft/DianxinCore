package com.dianxin.core.api.scheduler;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class Task {
    private static final AtomicInteger idCounter = new AtomicInteger(0);

    private final int taskId;
    private final boolean isSync; // Ở đây Sync hiểu là chạy trên ScheduledPool, Async là CachedPool
    private Future<?> future;     // Giữ reference để cancel

    public Task(boolean isSync) {
        this.taskId = idCounter.incrementAndGet();
        this.isSync = isSync;
    }

    public int getTaskId() {
        return taskId;
    }

    public boolean isSync() {
        return isSync;
    }

    public void cancel() {
        if (future != null && !future.isCancelled()) {
            future.cancel(true); // true = cho phép interrupt nếu đang chạy
        }
    }

    // Chỉ dùng nội bộ để set future sau khi submit vào pool
    protected void setFuture(Future<?> future) {
        this.future = future;
    }
}
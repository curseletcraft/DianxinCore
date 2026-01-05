package com.dianxin.core.api;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Deprecated
final class InternalAsyncScheduler implements AsyncScheduler {
    private final ExecutorService asyncPool = Executors.newCachedThreadPool();

    public void runAsync(Runnable task) {
        asyncPool.submit(task);
    }

    @Override
    public void runLater(Runnable task, long delay, TimeUnit unit) {

    }

    @Override
    public void runRepeating(Runnable task, long initialDelay, long period, TimeUnit unit) {

    }

    public void shutdown() {
        asyncPool.shutdownNow();
    }
}

package com.dianxin.core.api;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

final class InternalScheduler implements Scheduler {
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

    @Override
    public void runAsync(Runnable task) {
        executor.execute(task);
    }

    @Override
    public void runLater(Runnable task, long delay, TimeUnit unit) {
        executor.schedule(task, delay, unit);
    }

    @Override
    public void runRepeating(Runnable task, long initialDelay, long period, TimeUnit unit) {
        executor.scheduleAtFixedRate(task, initialDelay, period, unit);
    }

    @Override
    public void shutdown() {
        executor.shutdownNow();
    }
}

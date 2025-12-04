package com.dianxin.core.api;

import org.jetbrains.annotations.ApiStatus;

import java.util.concurrent.TimeUnit;

@ApiStatus.Experimental
public interface Scheduler {

    /**
     * Phương thức chạy asynchronous
     * @param task Runnable cần chạy
     */
    void runAsync(Runnable task);

    /**
     * Phuương thức chạy một task sau một thời gian được chỉ định
     * @param task Runnable cần chạy
     * @param delay Thời gian được delay
     * @param unit loại TimeUnit cần để delay
     */
    void runLater(Runnable task, long delay, TimeUnit unit);

    /**
     * Phương thức chạy một task được lặp lại sau một thời gian chỉ định
     * @param task
     * @param initialDelay
     * @param period
     * @param unit
     */
    void runRepeating(Runnable task, long initialDelay, long period, TimeUnit unit);

    /**
     * Shutdown scheduler
     */
    void shutdown();
}

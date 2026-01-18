package com.dianxin.core.api.concurrent;

import com.dianxin.core.api.exceptions.UtilityClassInitializationException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public final class ExecutorFactory {
    private ExecutorFactory() {
        throw new UtilityClassInitializationException(ExecutorFactory.class);
    }

    /**
     * Tạo ExecutorService cho I/O-bound tasks (HTTP, DB, API, file, ...)
     */
    public static ExecutorService createIoExecutor() {
        int cores = Runtime.getRuntime().availableProcessors();
        int threads = Math.max(4, cores * 2);

        AtomicInteger counter = new AtomicInteger(1);

        return Executors.newFixedThreadPool(
                threads,
                r -> {
                    Thread t = new Thread(r);
                    t.setName("dianxin-io-" + counter.getAndIncrement());
                    t.setDaemon(true);
                    return t;
                }
        );
    }

    /**
     * Tạo ExecutorService cho CPU-bound tasks
     */
    public static ExecutorService createCpuExecutor() {
        int cores = Runtime.getRuntime().availableProcessors();
        AtomicInteger counter = new AtomicInteger(1);

        return Executors.newFixedThreadPool(
                cores,
                r -> {
                    Thread t = new Thread(r);
                    t.setName("dianxin-cpu-" + counter.getAndIncrement());
                    t.setDaemon(true);
                    return t;
                }
        );
    }
}


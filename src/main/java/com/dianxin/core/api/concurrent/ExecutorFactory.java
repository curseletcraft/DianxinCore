package com.dianxin.core.api.concurrent;

import com.dianxin.core.api.exceptions.UtilityClassInitializationException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ExecutorFactory {

    private ExecutorFactory() {
        throw new UtilityClassInitializationException(ExecutorFactory.class);
    }

    public static ExecutorService createIoExecutor() {
        int cores = Runtime.getRuntime().availableProcessors();

        int threads = Math.max(4, cores * 2);

        return Executors.newFixedThreadPool(
                threads,
                r -> {
                    Thread t = new Thread(r);
                    t.setName("dianxin-io-" + t.getId()); // TODO  't.getId()' is deprecated since version 19
                    t.setDaemon(true);
                    return t;
                }
        );
    }

    public static ExecutorService createCpuExecutor() {
        int cores = Runtime.getRuntime().availableProcessors();

        return Executors.newFixedThreadPool(
                cores,
                r -> {
                    Thread t = new Thread(r);
                    t.setName("dianxin-cpu-" + t.getId());
                    t.setDaemon(true);
                    return t;
                }
        );
    }
}


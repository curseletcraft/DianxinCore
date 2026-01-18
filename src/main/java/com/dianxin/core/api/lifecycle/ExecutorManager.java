package com.dianxin.core.api.lifecycle;

import com.dianxin.core.api.concurrent.ExecutorFactory;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public final class ExecutorManager {
    private static ExecutorService IO_EXECUTOR;
    private static ExecutorService CPU_EXECUTOR;
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    private ExecutorManager() { }

    /**
     * Khởi tạo toàn bộ executor cho framework.
     * Nên được gọi trong bootstrap.
     */
    public static void initialize() {
        if (INITIALIZED.compareAndSet(false, true)) {
            IO_EXECUTOR = ExecutorFactory.createIoExecutor();
            CPU_EXECUTOR = ExecutorFactory.createCpuExecutor();
        }
    }

    public static ExecutorService io() {
        ensureInitialized();
        return IO_EXECUTOR;
    }

    public static ExecutorService cpu() {
        ensureInitialized();
        return CPU_EXECUTOR;
    }

    /**
     * Shutdown toàn bộ executor (gọi khi bot tắt).
     */
    public static void shutdown() {
        if (!INITIALIZED.get()) return;

        IO_EXECUTOR.shutdown();
        CPU_EXECUTOR.shutdown();
    }

    private static void ensureInitialized() {
        if (!INITIALIZED.get()) {
            throw new IllegalStateException(
                    "ExecutorManager has not been initialized. Call ExecutorManager.initialize() first."
            );
        }
    }

    public static <T> CompletableFuture<T> runIoAsync(@NotNull Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, io());
    }

    public static <T> CompletableFuture<T> runCpuAsync(@NotNull Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, cpu());
    }
}

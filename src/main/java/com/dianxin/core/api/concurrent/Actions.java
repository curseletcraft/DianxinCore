package com.dianxin.core.api.concurrent;

import com.dianxin.core.api.exceptions.UtilityClassInitializationException;
import org.jetbrains.annotations.ApiStatus;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Deprecated(since = "1.2.0-M3", forRemoval = true)
@ApiStatus.ScheduledForRemoval(inVersion = "2.0")
public final class Actions {
    private Actions() {
        throw new UtilityClassInitializationException(Actions.class);
    }

    public static <T> IAction<T> supplyAsync(Callable<T> task, Executor executor) {
        return new IActionImpl<>(
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return task.call();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, executor)
        );
    }

    public static IAction<Void> runAsync(Runnable task, Executor executor) {
        return new IActionImpl<>(CompletableFuture.runAsync(() -> {
            try {
                task.run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executor));
    }
}


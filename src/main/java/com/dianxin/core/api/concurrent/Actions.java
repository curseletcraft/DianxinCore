package com.dianxin.core.api.concurrent;

import com.dianxin.core.api.exceptions.UtilityClassInitializationException;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class Actions {
    private Actions() {
        throw new UtilityClassInitializationException(Actions.class);
    }

    public static <T> IAction<T> supplyAsync(Callable<T> task, Executor executor) {
        return new FutureAction<>(
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return task.call();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, executor)
        );
    }
}


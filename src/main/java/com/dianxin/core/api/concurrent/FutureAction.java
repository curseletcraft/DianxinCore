package com.dianxin.core.api.concurrent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("ClassCanBeRecord")
public class FutureAction<T> implements IAction<T> {
    private final CompletableFuture<T> future;

    public FutureAction(CompletableFuture<T> future) {
        this.future = future;
    }

    @Override
    public void queue(Consumer<T> success) {
        future.thenAccept(success);
    }

    @Override
    public void queue(Consumer<T> success, Consumer<Throwable> failure) {
        future.thenAccept(success)
                .exceptionally(ex -> {
                    failure.accept(ex);
                    return null;
                });
    }

    @Override
    public <U> IAction<U> map(Function<T, U> mapper) {
        return new FutureAction<>(future.thenApply(mapper));
    }

    @Override
    public IAction<T> runAsync(Executor executor) {
        return new FutureAction<>(future.thenApplyAsync(Function.identity(), executor));
    }

    @Override
    public T complete() {
        return future.join();
    }
}

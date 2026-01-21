package com.dianxin.core.api.concurrent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class FutureAction<T> implements IAction<T> {

    private final CompletableFuture<T> future;
    private final Logger logger = LoggerFactory.getLogger(FutureAction.class);

    public FutureAction(CompletableFuture<T> future) {
        this.future = future;
    }

    @Override
    public void queue(@Nullable Consumer<T> success) {
        queue(success, null);
    }

    @Override
    public void queue(@Nullable Consumer<T> success, @Nullable Consumer<Throwable> failure) {
        future.whenComplete((result, error) -> {
            if (error != null) {
                if (failure != null) {
                    failure.accept(error);
                } else {
                    // Mặc định in lỗi nếu không có handler
                    // error.printStackTrace();
                    logger.error("IAction error: '{}'", error.getMessage() , error);
                }
            } else {
                if (success != null) {
                    success.accept(result);
                }
            }
        });
    }

    @Override
    public T complete() {
        return future.join();
    }

    @Override
    public T complete(long timeout, @NotNull TimeUnit unit) throws TimeoutException {
        try {
            return future.get(timeout, unit);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull CompletableFuture<T> submit() {
        // Trả về bản sao để tránh người dùng can thiệp vào future gốc của Action
        return future.thenApply(Function.identity());
    }

    @Override
    public <U> @NotNull IAction<U> map(@NotNull Function<T, U> mapper) {
        return new FutureAction<>(future.thenApply(mapper));
    }

    @Override
    public <U> @NotNull IAction<U> flatMap(@NotNull Function<T, IAction<U>> mapper) {
        // Đây là phép thuật của chaining
        // thenCompose cho phép nối 1 CompletableFuture với 1 CompletableFuture khác
        return new FutureAction<>(future.thenCompose(result ->
                mapper.apply(result).submit() // Chuyển IAction về CompletableFuture
        ));
    }

    @Override
    public @NotNull IAction<T> onErrorReturn(T fallback) {
        return new FutureAction<>(future.exceptionally(ex -> fallback));
    }

    @Override
    public @NotNull IAction<T> onExecutor(@NotNull Executor executor) {
        // Chuyển kết quả sang xử lý ở executor mới
        return new FutureAction<>(future.thenApplyAsync(Function.identity(), executor));
    }
}
package com.dianxin.core.api.concurrent;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

class LazyActionImpl<T> implements LazyAction<T> {

    private final Callable<T> recipe; // Tờ giấy ghi công thức
    private final Executor executor;  // Đầu bếp sẽ thực hiện

    LazyActionImpl(Callable<T> recipe, Executor executor) {
        this.recipe = recipe;
        this.executor = executor;
    }

    @Override
    public void queue(@NotNull Consumer<ResultedAction.ActionResult<T>> callback) {
        // CHỈ KHI GỌI QUEUE, CHÚNG TA MỚI TẠO COMPLETABLE FUTURE
        CompletableFuture.supplyAsync(() -> {
            try {
                return ResultedAction.ActionResult.success(recipe.call());
            } catch (Throwable t) {
                return ResultedAction.ActionResult.failure(t);
            }
        }, executor).thenAccept(callback);
    }

    @Override
    public void queue() {
        queue(result -> {}); // Bắn và quên
    }

    @Override
    public @NotNull ResultedAction.ActionResult<T> complete() {
        // Chạy thẳng công thức trên luồng hiện tại (Block)
        try {
            return ResultedAction.ActionResult.success(recipe.call());
        } catch (Throwable t) {
            return ResultedAction.ActionResult.failure(t);
        }
    }

    @Override
    public <U> @NotNull LazyAction<U> map(@NotNull Function<T, U> mapper) {
        // KHÔNG CHẠY GÌ CẢ! Chỉ tạo ra một công thức mới lồng vào công thức cũ.
        Callable<U> newRecipe = () -> {
            T originalResult = this.recipe.call(); // Nấu món cũ
            return mapper.apply(originalResult);   // Nhào nặn thành món mới
        };
        return new LazyActionImpl<>(newRecipe, this.executor);
    }

    @Override
    public @NotNull LazyAction<T> onExecutor(@NotNull Executor newExecutor) {
        // Đổi đầu bếp, giữ nguyên công thức
        return new LazyActionImpl<>(this.recipe, newExecutor);
    }
}
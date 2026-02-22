package com.dianxin.core.api.concurrent;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

public class ResultedActionImpl<T> implements ResultedAction<T> {

    // Future bây giờ ôm trọn ActionResult, không lo bị ngắt quãng bởi Exception
    private final CompletableFuture<ActionResult<T>> future;

    public ResultedActionImpl(CompletableFuture<ActionResult<T>> future) {
        // Fallback an toàn: lỡ Future gốc bị lỗi do thread pool từ chối (RejectedExecutionException)
        this.future = future.exceptionally(ActionResult::failure);
    }

    @Override
    public void queue(@NotNull Consumer<ActionResult<T>> callback) {
        future.thenAccept(callback);
    }

    @Override
    public @NotNull ActionResult<T> complete() {
        return future.join();
    }

    @Override
    public @NotNull ActionResult<T> complete(long timeout, @NotNull TimeUnit unit) {
        try {
            return future.get(timeout, unit);
        } catch (Exception e) {
            return ActionResult.failure(e);
        }
    }

    @Override
    public @NotNull CompletableFuture<ActionResult<T>> submit() {
        return future.thenApply(Function.identity());
    }

    @Override
    public <U> @NotNull ResultedAction<U> map(@NotNull Function<T, U> mapper) {
        CompletableFuture<ActionResult<U>> mappedFuture = future.thenApply(result -> {
            // Chỉ chạy mapper nếu kết quả trước đó thành công
            if (result.isSuccess()) {
                try {
                    return ActionResult.success(mapper.apply(result.getValue()));
                } catch (Throwable t) {
                    return ActionResult.failure(t); // Bắt lỗi xảy ra trong lúc map
                }
            }
            // Nếu trước đó đã lỗi, đẩy tiếp lỗi đó xuống dưới
            return ActionResult.failure(result.getException());
        });
        return new ResultedActionImpl<>(mappedFuture);
    }

    @Override
    public <U> @NotNull ResultedAction<U> flatMap(@NotNull Function<T, ResultedAction<U>> mapper) {
        CompletableFuture<ActionResult<U>> flatMappedFuture = future.thenCompose(result -> {
            if (result.isSuccess()) {
                try {
                    return mapper.apply(result.getValue()).submit();
                } catch (Throwable t) {
                    return CompletableFuture.completedFuture(ActionResult.failure(t));
                }
            }
            return CompletableFuture.completedFuture(ActionResult.failure(result.getException()));
        });
        return new ResultedActionImpl<>(flatMappedFuture);
    }

    @Override
    public @NotNull ResultedAction<T> onExecutor(@NotNull Executor executor) {
        return new ResultedActionImpl<>(future.thenApplyAsync(Function.identity(), executor));
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isDone() {
        return future.isDone();
    }

    @Override
    public boolean isCancelled() {
        return future.isCancelled();
    }
}
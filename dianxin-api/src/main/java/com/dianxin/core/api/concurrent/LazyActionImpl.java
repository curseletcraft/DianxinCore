package com.dianxin.core.api.concurrent;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

class LazyActionImpl<T> implements LazyAction<T> {

    // Đây là "Bản thiết kế" - Một nhà máy chuyên tạo ra CompletableFuture
    private final Supplier<CompletableFuture<ActionResult<T>>> actionFactory;

    // Lưu lại cái Future đang chạy (nếu đã được kích hoạt) để có thể gọi cancel()
    private volatile CompletableFuture<ActionResult<T>> runningFuture;

    LazyActionImpl(Supplier<CompletableFuture<ActionResult<T>>> actionFactory) {
        this.actionFactory = actionFactory;
    }

    // =========================================================================
    // TERMINAL OPERATIONS (Khi các hàm này được gọi, Bản thiết kế mới biến thành Tác vụ thực)
    // =========================================================================

    @Override
    public @NotNull CompletableFuture<ActionResult<T>> submit() {
        // Kích hoạt nhà máy sản xuất ra Future
        this.runningFuture = actionFactory.get().exceptionally(ActionResult::failure);
        return this.runningFuture;
    }

    @Override
    public void queue(@NotNull Consumer<ActionResult<T>> callback) {
        submit().thenAccept(callback);
    }

    @Override
    public void queue() {
        submit().thenAccept(result -> {}); // Bắn và quên
    }

    @Override
    public void queue(@NotNull Consumer<T> success, @NotNull Consumer<Throwable> failure) {
        submit().thenAccept(result -> {
            if (result.isSuccess()) {
                success.accept(result.getValue());
            } else {
                Throwable ex = result.getException();
                if (ex == null && result.isCancelled()) {
                    ex = new CancellationException("LazyAction đã bị hủy chủ động.");
                }
                failure.accept(ex);
            }
        });
    }

    @Override
    public @NotNull ActionResult<T> complete() {
        return submit().join();
    }

    @Override
    public @NotNull ActionResult<T> complete(long timeout, @NotNull TimeUnit unit) {
        try {
            return submit().get(timeout, unit);
        } catch (Exception e) {
            return ActionResult.failure(e);
        }
    }

    // =========================================================================
    // CHAINING & MIDDLEWARE (Lười biếng: Chỉ bọc factory lại, không chạy code)
    // =========================================================================

    @Override
    public <U> @NotNull LazyAction<U> map(@NotNull Function<T, U> mapper) {
        return new LazyActionImpl<>(() -> actionFactory.get().thenApply(result -> {
            if (result.isSuccess()) {
                try {
                    return ActionResult.success(mapper.apply(result.getValue()));
                } catch (Throwable t) {
                    return ActionResult.failure(t);
                }
            }
            return ActionResult.failure(result.getException());
        }));
    }

    @Override
    public <U> @NotNull LazyAction<U> flatMap(@NotNull Function<T, LazyAction<U>> mapper) {
        return new LazyActionImpl<>(() -> actionFactory.get().thenCompose(result -> {
            if (result.isSuccess()) {
                try {
                    // Cực kỳ quan trọng: mapper trả về LazyAction,
                    // ta phải gọi submit() để nó bung thành CompletableFuture nối vào chuỗi
                    return mapper.apply(result.getValue()).submit();
                } catch (Throwable t) {
                    return CompletableFuture.completedFuture(ActionResult.failure(t));
                }
            }
            return CompletableFuture.completedFuture(ActionResult.failure(result.getException()));
        }));
    }

    @Override
    public @NotNull LazyAction<T> onExecutor(@NotNull Executor executor) {
        return new LazyActionImpl<>(() -> actionFactory.get().thenApplyAsync(Function.identity(), executor));
    }

    @Override
    public @NotNull LazyAction<T> onSuccess(@NotNull Consumer<T> successCallback) {
        return new LazyActionImpl<>(() -> actionFactory.get().thenApply(result -> {
            if (result.isSuccess()) {
                try { successCallback.accept(result.getValue()); }
                catch (Exception ignored) {}
            }
            return result;
        }));
    }

    @Override
    public @NotNull LazyAction<T> onError(@NotNull Consumer<Throwable> failureCallback) {
        return new LazyActionImpl<>(() -> actionFactory.get().thenApply(result -> {
            if (!result.isSuccess()) {
                Throwable ex = result.getException();
                if (ex == null && result.isCancelled()) ex = new CancellationException("LazyAction Cancelled");
                try { failureCallback.accept(ex); }
                catch (Exception ignored) {}
            }
            return result;
        }));
    }

    // =========================================================================
    // STATE MANAGEMENT
    // =========================================================================

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (runningFuture != null) {
            return runningFuture.cancel(mayInterruptIfRunning);
        }
        return false; // Chưa kích hoạt thì không thể hủy luồng
    }

    @Override
    public boolean isDone() {
        return runningFuture != null && runningFuture.isDone();
    }

    @Override
    public boolean isCancelled() {
        return runningFuture != null && runningFuture.isCancelled();
    }
}
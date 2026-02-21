package com.dianxin.core.api.concurrent;

import com.dianxin.core.api.lifecycle.ExecutorManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

public interface ResultedAction<T> {

    // =========================================================================
    // Execution Methods
    // =========================================================================

    /**
     * Thực thi action và trả về kết quả qua callback duy nhất (xử lý cả success & failure).
     */
    void queue(@NotNull Consumer<ActionResult<T>> callback);

    /**
     * Block luồng hiện tại để lấy kết quả an toàn (không bao giờ throw Exception rác).
     */
    @NotNull
    ActionResult<T> complete();

    @NotNull
    ActionResult<T> complete(long timeout, @NotNull TimeUnit unit);

    @NotNull
    CompletableFuture<ActionResult<T>> submit();

    // =========================================================================
    // Chaining & Transformation
    // =========================================================================

    @NotNull
    <U> ResultedAction<U> map(@NotNull Function<T, U> mapper);

    @NotNull
    <U> ResultedAction<U> flatMap(@NotNull Function<T, ResultedAction<U>> mapper);

    @NotNull
    ResultedAction<T> onExecutor(@NotNull Executor executor);

    // =========================================================================
    // Static Factories
    // =========================================================================

    static <T> ResultedAction<T> supplyAsync(@NotNull Callable<T> task, @Nullable Executor executor) {
        Executor exec = (executor != null) ? executor : ExecutorManager.io();

        // Cốt lõi: Luôn trả về ActionResult, KHÔNG BAO GIỜ throw lỗi ra ngoài Future
        CompletableFuture<ActionResult<T>> future = CompletableFuture.supplyAsync(() -> {
            try {
                return ActionResult.success(task.call());
            } catch (Throwable e) {
                return ActionResult.failure(e);
            }
        }, exec);

        return new ResultedActionImpl<>(future);
    }

    // =========================================================================
    // Data Classes
    // =========================================================================

    enum ActionStatus {
        SUCCESS, FAILURE, WAITING;
    }

    class ActionResult<T> {
        private final ActionStatus status;
        private final T value;
        private final Throwable exception;

        private ActionResult(ActionStatus status, T value, Throwable exception) {
            this.status = status;
            this.value = value;
            this.exception = exception;
        }

        public static <T> ActionResult<T> success(T value) {
            return new ActionResult<>(ActionStatus.SUCCESS, value, null);
        }

        public static <T> ActionResult<T> failure(Throwable exception) {
            return new ActionResult<>(ActionStatus.FAILURE, null, exception);
        }

        public boolean isSuccess() {
            return status == ActionStatus.SUCCESS;
        }

        public boolean isFailure() {
            return status == ActionStatus.FAILURE;
        }

        public ActionStatus getStatus() { return status; }
        public T getValue() { return value; }
        public Throwable getException() { return exception; }
    }
}
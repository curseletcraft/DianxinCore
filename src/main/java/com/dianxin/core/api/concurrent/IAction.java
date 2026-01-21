package com.dianxin.core.api.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unused")
public interface IAction<T> {
    /**
     * Thực thi action bất đồng bộ.
     *
     * @param success callback khi thành công
     */
    void queue(Consumer<T> success);

    /**
     * Thực thi action bất đồng bộ với xử lý lỗi.
     *
     * @param success callback khi thành công
     * @param failure callback khi lỗi
     */
    void queue(Consumer<T> success, Consumer<Throwable> failure);

    /**
     * Chuyển đổi kết quả action.
     *
     * @param mapper hàm chuyển đổi
     * @return IAction mới
     */
    <U> IAction<U> map(Function<T, U> mapper);

    /**
     * Thực thi action trên executor chỉ định.
     *
     * @param executor executor để chạy
     * @return IAction mới
     */
    IAction<T> runAsync(Executor executor);

    /**
     * Blocks the current thread until the action completes.
     *
     * <p><b>WARNING:</b> This method blocks the calling thread.
     * It must NOT be called from event loops, schedulers,
     * or other performance-critical threads.</p>
     *
     * @return result of the action
     */
    T complete();

    public static <T> IAction<T> supplyAsync(Callable<T> task, Executor executor) { // Executor có thể dùng ExecutorService từ ExecutorManager ban nãy
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

    public static IAction<Void> runAsync(Runnable task, Executor executor) {
        return new FutureAction<>(CompletableFuture.runAsync(() -> {
            try {
                task.run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executor));
    }
}

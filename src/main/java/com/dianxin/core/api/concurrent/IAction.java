package com.dianxin.core.api.concurrent;

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
}

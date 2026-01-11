package com.dianxin.core.api.concurrent;

import net.dv8tion.jda.internal.entities.GuildImpl;

import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

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
}

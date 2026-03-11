package com.dianxin.core.api.concurrent;

import com.dianxin.core.api.lifecycle.ExecutorManager;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

public interface LazyAction<T> {

    // Kích hoạt tác vụ (Bắt đầu "nấu ăn")
    void queue(@NotNull Consumer<ResultedAction.ActionResult<T>> callback);
    void queue();

    // Chặn luồng để lấy kết quả
    @NotNull
    ResultedAction.ActionResult<T> complete();

    // Các hàm biến đổi (Vẫn lười biếng, chỉ ghi đè thêm công thức)
    @NotNull
    <U> LazyAction<U> map(@NotNull Function<T, U> mapper);

    @NotNull
    LazyAction<T> onExecutor(@NotNull Executor executor);

    // Factory method tạo LazyAction
    static <T> LazyAction<T> defer(@NotNull Callable<T> task) {
        return new LazyActionImpl<>(task, ExecutorManager.io());
    }
}
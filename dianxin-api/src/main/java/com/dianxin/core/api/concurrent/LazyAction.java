package com.dianxin.core.api.concurrent;

import com.dianxin.core.api.annotations.ReleasedSince;
import com.dianxin.core.api.lifecycle.ExecutorManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Đại diện cho một hành động bất đồng bộ lười biếng (Lazy Asynchronous Action).
 * <p>
 * Khác với {@link ResultedAction} (Eager - Chạy ngay khi khởi tạo), {@code LazyAction}
 * chỉ là một "Bản thiết kế". Tác vụ thực sự sẽ <b>KHÔNG BAO GIỜ CHẠY</b> cho đến khi
 * một trong các hàm kết thúc (Terminal Operations) như {@link #queue()}, {@link #complete()},
 * hoặc {@link #submit()} được gọi.
 * </p>
 * <p>
 * Tính chất lười biếng này giúp tiết kiệm tài nguyên tuyệt đối, hỗ trợ tái sử dụng action nhiều lần,
 * và cho phép xây dựng các chuỗi xử lý (chaining) phức tạp mà không sợ bị chặn luồng ngang chừng.
 * </p>
 *
 * <pre>{@code
 * // Chỉ là khai báo, không có request mạng nào được gửi đi
 * LazyAction<Long> pingMinecraftServer = LazyAction.defer(() -> mcApi.ping("play.hypixel.net"))
 *     .onSuccess(ping -> System.out.println("Ping thành công: " + ping + "ms"))
 *     .onError(err -> System.err.println("Server sập rồi!"));
 *
 * // 10 phút sau cần check ping? Lôi nó ra dùng!
 * pingMinecraftServer.queue();
 *
 * // Nửa tiếng sau cần check tiếp? Lôi ra dùng tiếp!
 * pingMinecraftServer.queue();
 * }</pre>
 *
 * @param <T> Kiểu dữ liệu trả về khi hành động hoàn tất thành công.
 */
@ReleasedSince("2.1")
@SuppressWarnings({"unused", "resource"})
public interface LazyAction<T> {

    // =========================================================================
    // Terminal Operations (Kích hoạt Tác vụ)
    // =========================================================================

    /** Thực thi action (Fire and forget). */
    void queue();

    /** Thực thi và phân nhánh Thành công / Thất bại. */
    void queue(@NotNull Consumer<T> success, @NotNull Consumer<Throwable> failure);

    /** Thực thi và trả về kết quả qua một callback duy nhất. */
    void queue(@NotNull Consumer<ActionResult<T>> callback);

    /** Chặn luồng hiện tại cho đến khi lấy được kết quả. */
    @NotNull
    ActionResult<T> complete();

    /** Chặn luồng hiện tại với thời gian chờ tối đa. */
    @NotNull
    ActionResult<T> complete(long timeout, @NotNull TimeUnit unit);

    /** * Kích hoạt action và trả về CompletableFuture để tích hợp với API khác.
     * Lưu ý: Gọi hàm này đồng nghĩa với việc tác vụ bắt đầu chạy.
     */
    @NotNull
    CompletableFuture<ActionResult<T>> submit();

    // =========================================================================
    // Middleware & Hooks (Gắn thêm logic ngầm)
    // =========================================================================

    @NotNull
    LazyAction<T> onSuccess(@NotNull Consumer<T> successCallback);

    @NotNull
    LazyAction<T> onError(@NotNull Consumer<Throwable> failureCallback);

    // =========================================================================
    // Chaining & Transformation (Biến đổi công thức)
    // =========================================================================

    @NotNull
    <U> LazyAction<U> map(@NotNull Function<T, U> mapper);

    @NotNull
    <U> LazyAction<U> flatMap(@NotNull Function<T, LazyAction<U>> mapper);

    /** Chuyển đổi Executor thực thi cho phần còn lại của chuỗi. */
    @NotNull
    LazyAction<T> onExecutor(@NotNull Executor executor);

    // =========================================================================
    // Process Management (Quản lý Tiến trình)
    // =========================================================================

    /** Hủy bỏ tác vụ (Chỉ có tác dụng nếu tác vụ ĐÃ được kích hoạt bằng queue/submit). */
    boolean cancel(boolean mayInterruptIfRunning);

    /** @return true nếu tác vụ ĐÃ kích hoạt và chạy xong. */
    boolean isDone();

    /** @return true nếu tác vụ ĐÃ kích hoạt và bị hủy. */
    boolean isCancelled();

    // =========================================================================
    // Static Factories (Khởi tạo Bản thiết kế)
    // =========================================================================

    /**
     * Tạo một LazyAction từ một tác vụ (Callable).
     * Mặc định sử dụng {@link ExecutorManager#io()} khi kích hoạt.
     */
    static <T> LazyAction<T> defer(@NotNull Callable<T> task) {
        return defer(task, ExecutorManager.io());
    }

    /**
     * Tạo một LazyAction với luồng (Executor) được chỉ định sẵn.
     */
    static <T> LazyAction<T> defer(@NotNull Callable<T> task, @Nullable Executor executor) {
        Executor exec = (executor != null) ? executor : ExecutorManager.io();

        return new LazyActionImpl<>(() -> CompletableFuture.supplyAsync(() -> {
            try {
                return ActionResult.success(task.call());
            } catch (Throwable e) {
                return ActionResult.failure(e);
            }
        }, exec));
    }
}
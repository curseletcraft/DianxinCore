package com.dianxin.core.api.concurrent;

import com.dianxin.core.api.lifecycle.ExecutorManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Đại diện cho một hành động bất đồng bộ có thể thực thi sau (Lazy execution).
 * <p>
 * Interface này được thiết kế tương tự {@code RestAction} của JDA nhưng tối giản hóa,
 * tập trung vào việc xử lý chuỗi hành động (chaining) và callback.
 * </p>
 *
 * Ví dụ về xử lý IAction chuyên nghiệp:
 * <pre><code>
 *     // Ví dụ: Lấy dữ liệu -> Xử lý -> Lưu DB -> Thông báo
 * IAction.supplyAsync(() -> fetchDataFromWeb(), ExecutorManager.io())
 *     .map(data -> processData(data)) // Chuyển đổi dữ liệu (Map)
 *     .onExecutor(ExecutorManager.cpu()) // Chuyển sang luồng CPU để xử lý nặng
 *     .flatMap(processedData -> saveToDatabaseAction(processedData)) // Nối tiếp action lưu DB (FlatMap)
 *     .queue(
 *         result -> System.out.println("✅ Thành công: " + result),
 *         error -> System.err.println("❌ Lỗi: " + error.getMessage())
 *     );
 * </code></pre>
 *
 * @param <T> Kiểu dữ liệu trả về khi hành động hoàn tất.
 */
@SuppressWarnings("unused")
public interface IAction<T> {

    // =========================================================================
    // Execution Methods (Thực thi)
    // =========================================================================

    /**
     * Thực thi action bất đồng bộ và quên đi (Fire-and-forget).
     * <p>
     * Nếu có lỗi xảy ra, nó sẽ được in ra logger mặc định hoặc bị nuốt chửng
     * tùy thuộc vào implementation.
     */
    default void queue() {
        queue(null);
    }

    /**
     * Thực thi action bất đồng bộ với callback khi thành công.
     *
     * @param success Callback được gọi khi action hoàn tất thành công.
     * Có thể là {@code null} nếu không cần xử lý.
     */
    void queue(@Nullable Consumer<T> success);

    /**
     * Thực thi action bất đồng bộ với đầy đủ callback xử lý thành công và lỗi.
     *
     * @param success Callback được gọi khi action hoàn tất thành công.
     * @param failure Callback được gọi khi action gặp lỗi (Exception).
     */
    void queue(@Nullable Consumer<T> success, @Nullable Consumer<Throwable> failure);

    /**
     * Chặn (Block) luồng hiện tại cho đến khi action hoàn tất và trả về kết quả.
     * <p>
     * <b>CẢNH BÁO:</b> Không được gọi method này trên luồng sự kiện (Event Loop)
     * hoặc các luồng quan trọng khác vì nó sẽ gây treo ứng dụng.
     * </p>
     *
     * @return Kết quả của action.
     * @throws RuntimeException Nếu action gặp lỗi trong quá trình thực thi.
     */
    T complete();

    /**
     * Chặn (Block) luồng hiện tại với thời gian chờ tối đa (Timeout).
     *
     * @param timeout Thời gian chờ tối đa.
     * @param unit    Đơn vị thời gian.
     * @return Kết quả của action.
     * @throws TimeoutException Nếu action không hoàn thành trong thời gian quy định.
     */
    T complete(long timeout, @NotNull TimeUnit unit) throws TimeoutException;

    /**
     * Thực thi action và trả về một {@link CompletableFuture} để xử lý nâng cao.
     *
     * @return Future đại diện cho kết quả của action.
     */
    @NotNull
    CompletableFuture<T> submit();

    // =========================================================================
    // Chaining & Transformation (Nối chuỗi & Chuyển đổi)
    // =========================================================================

    /**
     * Chuyển đổi kết quả của action này sang một giá trị khác (Synchronous).
     * <p>
     * Ví dụ: {@code action.map(User::getName)}
     * </p>
     *
     * @param mapper Hàm chuyển đổi dữ liệu.
     * @param <U>    Kiểu dữ liệu mới.
     * @return Một IAction mới trả về kiểu U.
     */
    @NotNull
    <U> IAction<U> map(@NotNull Function<T, U> mapper);

    /**
     * Nối tiếp một action khác sau khi action này hoàn tất (Asynchronous chaining).
     * <p>
     * Đây là method quan trọng nhất để tránh "Callback Hell".
     * Giúp nối chuỗi: A -> B -> C một cách tuần tự.
     * </p>
     *
     * @param mapper Hàm nhận kết quả của action này và trả về một IAction mới.
     * @param <U>    Kiểu dữ liệu của action tiếp theo.
     * @return Một IAction mới đại diện cho kết quả của chuỗi action.
     */
    @NotNull
    <U> IAction<U> flatMap(@NotNull Function<T, IAction<U>> mapper);

    /**
     * Cung cấp một giá trị thay thế nếu action gặp lỗi.
     *
     * @param fallback Giá trị trả về nếu có lỗi.
     * @return Một IAction mới an toàn hơn.
     */
    @NotNull
    IAction<T> onErrorReturn(T fallback);

    /**
     * Chuyển đổi Executor thực thi action này.
     *
     * @param executor Executor mới để chạy action.
     * @return Một IAction mới chạy trên executor chỉ định.
     */
    @NotNull
    IAction<T> onExecutor(@NotNull Executor executor);

    // =========================================================================
    // Static Factories
    // =========================================================================

    /**
     * Tạo một IAction trả về giá trị (Callable).
     * Mặc định sử dụng {@link ExecutorManager#io()} nếu executor truyền vào là null.
     */
    static <T> IAction<T> supplyAsync(@NotNull Callable<T> task, @Nullable Executor executor) {
        Executor exec = (executor != null) ? executor : ExecutorManager.io();
        return new FutureAction<>(CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, exec));
    }

    /**
     * Tạo một IAction không trả về giá trị (Runnable).
     * Mặc định sử dụng {@link ExecutorManager#io()}.
     */
    static IAction<Void> runAsync(@NotNull Runnable task, @Nullable Executor executor) {
        Executor exec = (executor != null) ? executor : ExecutorManager.io();
        return new FutureAction<>(CompletableFuture.runAsync(() -> {
            try {
                task.run();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, exec));
    }

    /**
     * Tạo một IAction đã hoàn thành sẵn với giá trị cố định.
     */
    static <T> IAction<T> completed(T value) {
        return new FutureAction<>(CompletableFuture.completedFuture(value));
    }
}
package com.dianxin.core.api.concurrent;

import com.dianxin.core.api.lifecycle.ExecutorManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Đại diện cho một hành động bất đồng bộ an toàn (Safe Asynchronous Action).
 * <p>
 * Khác với {@link IAction}, {@code ResultedAction} áp dụng <b>Result Pattern</b> (tương tự như
 * {@code Result} trong Rust hay {@code Try} trong Scala). Nó không bao giờ ném ngoại lệ (throw Exception)
 * ra ngoài luồng chính hoặc làm đứt gãy chuỗi xử lý. Thay vào đó, cả kết quả thành công lẫn
 * thất bại đều được gói gọn một cách an toàn vào đối tượng {@link ActionResult}.
 * </p>
 *
 * Tác dụng:
 * <ul>
 * <li>Loại bỏ hoàn toàn rủi ro crash thread do các lỗi không được bắt (Unhandled Exceptions).</li>
 * <li>Giúp luồng code xử lý lỗi tập trung, tường minh và thanh lịch hơn thông qua 1 callback duy nhất.</li>
 * <li>Tự động bỏ qua các bước {@code map}/{@code flatMap} phía sau nếu một bước phía trước gặp lỗi.</li>
 * </ul>
 *
 * Ví dụ sử dụng:
 * <pre><code>
 * ResultedAction.supplyAsync(() -> api.getUser(123), ExecutorManager.io())
 *      .map(User::getName) // Chỉ chạy nếu getUser thành công
 *      .flatMap(name -> checkNameInDatabase(name)) // Chỉ chạy nếu map thành công
 *      .queue(result -> {
 *          if (result.isSuccess()) {
 *              System.out.println("✅ Tên hợp lệ: " + result.getValue());
 *          } else {
 *              System.err.println("❌ Lỗi trong quá trình xử lý: " + result.getException().getMessage());
 *          }
 *      });
 * </code></pre>
 *
 * @param <T> Kiểu dữ liệu trả về khi hành động hoàn tất thành công.
 */
@SuppressWarnings("unused")
public interface ResultedAction<T> {

    // =========================================================================
    // Execution Methods
    // =========================================================================

    /**
     * Thực thi action bất đồng bộ và trả về kết quả qua một callback duy nhất.
     * <p>
     * Callback này sẽ luôn được gọi bất kể action thành công hay thất bại. Bạn cần kiểm tra
     * {@link ActionResult#isSuccess()} để xác định luồng xử lý tiếp theo.
     * </p>
     *
     * @param callback Hàm xử lý kết quả trả về.
     */
    void queue(@NotNull Consumer<ActionResult<T>> callback);

    /**
     * Chặn (Block) luồng hiện tại cho đến khi action hoàn tất và trả về {@link ActionResult}.
     * <p>
     * <b>Lưu ý:</b> Method này an toàn 100%, không bao giờ ném ra Exception do tác vụ bên trong gây ra.
     * Tuy nhiên, vẫn không nên gọi nó trên Main/Event Thread để tránh treo ứng dụng.
     * </p>
     *
     * @return Đối tượng chứa kết quả (thành công hoặc lỗi).
     */
    @NotNull
    ActionResult<T> complete();

    /**
     * Chặn (Block) luồng hiện tại với thời gian chờ tối đa (Timeout).
     * <p>
     * Nếu quá thời gian quy định, nó sẽ trả về một {@link ActionResult} mang lỗi {@link TimeoutException}.
     * </p>
     *
     * @param timeout Thời gian chờ tối đa.
     * @param unit    Đơn vị thời gian.
     * @return Đối tượng chứa kết quả hoặc lỗi Timeout.
     */
    @NotNull
    ActionResult<T> complete(long timeout, @NotNull TimeUnit unit);

    /**
     * Thực thi action và trả về một {@link CompletableFuture} chứa {@link ActionResult} để xử lý nâng cao.
     *
     * @return Future đại diện cho kết quả an toàn của action.
     */
    @NotNull
    CompletableFuture<ActionResult<T>> submit();

    // =========================================================================
    // Chaining & Transformation
    // =========================================================================

    /**
     * Chuyển đổi giá trị thành công của action này sang một giá trị khác (Synchronous).
     * <p>
     * <b>Cơ chế an toàn:</b> Nếu action hiện tại đang mang trạng thái LỖI (Failure), hàm {@code mapper}
     * này sẽ <b>bị bỏ qua hoàn toàn</b>, và lỗi đó sẽ tiếp tục được truyền xuống IAction mới.
     * Nếu quá trình chuyển đổi sinh ra lỗi mới, lỗi đó sẽ được bắt lại và chuyển thành Failure.
     * </p>
     *
     * @param mapper Hàm chuyển đổi dữ liệu.
     * @param <U>    Kiểu dữ liệu mới sau khi chuyển đổi.
     * @return Một ResultedAction mới chứa dữ liệu kiểu U hoặc mang theo lỗi.
     */
    @NotNull
    <U> ResultedAction<U> map(@NotNull Function<T, U> mapper);

    /**
     * Nối tiếp một ResultedAction khác sau khi action này hoàn tất thành công (Asynchronous chaining).
     * <p>
     * <b>Cơ chế an toàn:</b> Tương tự {@link #map}, chuỗi nối tiếp này sẽ bị bỏ qua nếu action
     * hiện tại bị lỗi. Tính năng này giúp kết nối chuỗi nhiều thao tác I/O mà không sợ sụp đổ dây chuyền.
     * </p>
     *
     * @param mapper Hàm nhận kết quả thành công và sinh ra một ResultedAction mới.
     * @param <U>    Kiểu dữ liệu của action tiếp theo.
     * @return Một ResultedAction đại diện cho toàn bộ chuỗi xử lý.
     */
    @NotNull
    <U> ResultedAction<U> flatMap(@NotNull Function<T, ResultedAction<U>> mapper);

    /**
     * Chuyển đổi Executor thực thi các tác vụ xử lý tiếp theo của action này.
     *
     * @param executor Executor mới (ví dụ: chuyển từ IO sang CPU để tính toán).
     * @return Một ResultedAction mới chạy trên executor đã chỉ định.
     */
    @NotNull
    ResultedAction<T> onExecutor(@NotNull Executor executor);

    // =========================================================================
    // Static Factories
    // =========================================================================

    /**
     * Tạo một ResultedAction từ một tác vụ (Callable).
     * <p>
     * Bất kỳ Exception nào quăng ra từ {@code task.call()} đều sẽ bị bắt lại và gói vào
     * {@link ActionResult#failure(Throwable)}.
     * </p>
     *
     * @param task     Tác vụ cần thực thi.
     * @param executor Thread pool để chạy tác vụ (mặc định dùng {@link ExecutorManager#io()} nếu null).
     * @param <T>      Kiểu dữ liệu trả về.
     * @return Một ResultedAction sẵn sàng để chạy hoặc nối chuỗi.
     */
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

    /**
     * Trạng thái kết quả của một ResultedAction.
     */
    enum ActionStatus {
        /** Hành động đã hoàn tất thành công và mang theo dữ liệu. */
        SUCCESS,
        /** Hành động gặp lỗi (Exception) trong quá trình thực thi. */
        FAILURE
    }

    /**
     * Lớp Wrapper bao bọc kết quả trả về của một {@link ResultedAction}.
     * Nó buộc người lập trình phải kiểm tra trạng thái trước khi lấy dữ liệu.
     *
     * @param <T> Kiểu dữ liệu chứa bên trong nếu thành công.
     */
    class ActionResult<T> {
        @NotNull
        private final ActionStatus status;
        private final T value;
        private final Throwable exception;

        private ActionResult(@NotNull ActionStatus status, T value, Throwable exception) {
            this.status = status;
            this.value = value;
            this.exception = exception;
        }

        /** Tạo một kết quả Thành Công. */
        public static <T> ActionResult<T> success(T value) {
            return new ActionResult<>(ActionStatus.SUCCESS, value, null);
        }

        /** Tạo một kết quả Thất Bại mang theo ngoại lệ. */
        public static <T> ActionResult<T> failure(Throwable exception) {
            return new ActionResult<>(ActionStatus.FAILURE, null, exception);
        }

        /** @return true nếu action hoàn tất không có lỗi. */
        public boolean isSuccess() {
            return status == ActionStatus.SUCCESS;
        }

        /** @return true nếu action gặp lỗi (Exception). */
        public boolean isFailure() {
            return status == ActionStatus.FAILURE;
        }

        /** @return Trạng thái của action. */
        @NotNull
        public ActionStatus getStatus() { return status; }

        /** @return Dữ liệu trả về (có thể null nếu action sinh ra null hoặc bị lỗi). */
        public T getValue() { return value; }

        /** @return Ngoại lệ gây ra lỗi (null nếu thành công). */
        public Throwable getException() { return exception; }
    }
}
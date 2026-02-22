package com.dianxin.core.api.concurrent;

import com.dianxin.core.api.lifecycle.ExecutorManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Đại diện cho một hành động bất đồng bộ an toàn (Safe Asynchronous Action).
 * <p>
 * Khác với {@code IAction}, {@code ResultedAction} áp dụng <b>Result Pattern</b> (tương tự như
 * {@code Result} trong Rust hay {@code Try} trong Scala). Nó không bao giờ ném ngoại lệ (throw Exception)
 * ra ngoài luồng chính hoặc làm đứt gãy chuỗi xử lý. Thay vào đó, cả kết quả thành công,
 * thất bại, hoặc bị hủy đều được gói gọn một cách an toàn vào đối tượng {@link ActionResult}.
 * </p>
 *
 * <b>Tác dụng:</b>
 * <ul>
 * <li>Loại bỏ hoàn toàn rủi ro crash thread do các lỗi không được bắt (Unhandled Exceptions).</li>
 * <li>Giúp luồng code xử lý lỗi tập trung, tường minh và thanh lịch hơn thông qua 1 callback duy nhất.</li>
 * <li>Tự động bỏ qua các bước {@code map}/{@code flatMap} phía sau nếu một bước phía trước gặp lỗi hoặc bị hủy.</li>
 * </ul>
 *
 * <b>Ví dụ sử dụng:</b>
 * <pre>{@code
 * ResultedAction.supplyAsync(() -> api.getUser(123), ExecutorManager.io())
 *      .map(User::getName) // Chỉ chạy nếu getUser thành công
 *      .flatMap(name -> checkNameInDatabase(name)) // Chỉ chạy nếu map thành công
 *      .queue(result -> {
 *          if (result.isSuccess()) {
 *              System.out.println("✅ Tên hợp lệ: " + result.getValue());
 *          } else if (result.isCancelled()) {
 *              System.out.println("⚠️ Tác vụ đã bị hủy bởi người dùng.");
 *          } else {
 *              System.err.println("❌ Lỗi trong quá trình xử lý: " + result.getException().getMessage());
 *          }
 *      });
 * }</pre>
 *
 * @param <T> Kiểu dữ liệu trả về khi hành động hoàn tất thành công.
 */
@SuppressWarnings("unused")
public interface ResultedAction<T> {

    // =========================================================================
    // Execution Methods (Phương thức Thực thi)
    // =========================================================================

    /**
     * Thực thi action bất đồng bộ và trả về kết quả qua một callback duy nhất.
     * <p>
     * Callback này sẽ luôn được gọi bất kể action thành công, thất bại hay bị hủy. Bạn cần kiểm tra
     * các hàm như {@link ActionResult#isSuccess()} để xác định luồng xử lý tiếp theo.
     * </p>
     *
     * @param callback Hàm xử lý kết quả trả về.
     */
    void queue(@NotNull Consumer<ActionResult<T>> callback);

    /**
     * Chặn (Block) luồng hiện tại cho đến khi action hoàn tất và trả về {@link ActionResult}.
     * <p>
     * <b>Lưu ý:</b> Method này an toàn 100%, không bao giờ ném ra Exception do tác vụ bên trong gây ra.
     * Tuy nhiên, vẫn <b>không nên</b> gọi nó trên Main/Event Thread để tránh treo ứng dụng.
     * </p>
     *
     * @return Đối tượng chứa kết quả (thành công, lỗi, hoặc bị hủy).
     */
    @NotNull
    ActionResult<T> complete();

    /**
     * Chặn (Block) luồng hiện tại với thời gian chờ tối đa (Timeout).
     * <p>
     * Nếu quá thời gian quy định, nó sẽ trả về một {@link ActionResult} mang trạng thái LỖI với
     * nguyên nhân là {@link TimeoutException}.
     * </p>
     *
     * @param timeout Thời gian chờ tối đa.
     * @param unit    Đơn vị thời gian cho timeout.
     * @return Đối tượng chứa kết quả hoặc lỗi Timeout.
     */
    @NotNull
    ActionResult<T> complete(long timeout, @NotNull TimeUnit unit);

    /**
     * Thực thi action và trả về một {@link CompletableFuture} chứa {@link ActionResult} để xử lý nâng cao.
     * <p>
     * Phương thức này hữu ích khi bạn muốn tích hợp {@code ResultedAction} vào các API
     * sử dụng CompletableFuture có sẵn của Java.
     * </p>
     *
     * @return Future đại diện cho kết quả an toàn của action.
     */
    @NotNull
    CompletableFuture<ActionResult<T>> submit();

    // =========================================================================
    // Chaining & Transformation (Nối chuỗi và Biến đổi)
    // =========================================================================

    /**
     * Chuyển đổi giá trị thành công của action này sang một giá trị khác (Synchronous).
     * <p>
     * <b>Cơ chế an toàn:</b> Nếu action hiện tại đang mang trạng thái LỖI hoặc BỊ HỦY, hàm {@code mapper}
     * này sẽ <b>bị bỏ qua hoàn toàn</b>, và trạng thái đó sẽ tiếp tục được truyền xuống ResultedAction mới.
     * Nếu quá trình chuyển đổi sinh ra lỗi mới, lỗi đó sẽ được bắt lại và chuyển thành trạng thái FAILURE.
     * </p>
     *
     * @param mapper Hàm biến đổi dữ liệu.
     * @param <U>    Kiểu dữ liệu mới sau khi biến đổi.
     * @return Một ResultedAction mới chứa dữ liệu kiểu U hoặc mang theo trạng thái lỗi cũ.
     */
    @NotNull
    <U> ResultedAction<U> map(@NotNull Function<T, U> mapper);

    /**
     * Nối tiếp một ResultedAction khác sau khi action này hoàn tất thành công (Asynchronous chaining).
     * <p>
     * <b>Cơ chế an toàn:</b> Tương tự {@link #map}, chuỗi nối tiếp này sẽ bị bỏ qua nếu action
     * hiện tại bị lỗi/hủy. Tính năng này giúp kết nối chuỗi nhiều thao tác I/O mà không sợ sụp đổ dây chuyền.
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
     * <p>
     * Hữu ích khi bạn vừa hoàn thành lấy dữ liệu (I/O thread) và muốn chuyển sang xử lý dữ liệu nặng (CPU thread).
     * </p>
     *
     * @param executor Executor mới sẽ nhận trách nhiệm thực thi.
     * @return Một ResultedAction mới chạy trên executor đã chỉ định.
     */
    @NotNull
    ResultedAction<T> onExecutor(@NotNull Executor executor);

    // =========================================================================
    // Process Management (Quản lý Tiến trình)
    // =========================================================================

    /**
     * Chủ động hủy bỏ tác vụ này nếu nó chưa hoàn thành.
     * <p>
     * Khi gọi hàm này, các tiến trình map/flatMap phía sau sẽ tự động chuyển sang trạng thái
     * {@link ActionStatus#CANCELLED} và ngừng thực thi logic.
     * </p>
     *
     * @param mayInterruptIfRunning Cho phép (true) hoặc Không cho phép (false) ngắt luồng đang chạy tác vụ này.
     * @return {@code true} nếu hủy thành công, {@code false} nếu tác vụ đã xong từ trước không thể hủy nữa.
     */
    boolean cancel(boolean mayInterruptIfRunning);

    /**
     * Kiểm tra xem tác vụ đã hoàn tất chưa (Thành công, Thất bại, hoặc Bị hủy).
     *
     * @return {@code true} nếu tác vụ đã kết thúc.
     */
    boolean isDone();

    /**
     * Kiểm tra xem tác vụ này có bị hủy bỏ chủ động trước khi hoàn thành hay không.
     *
     * @return {@code true} nếu tác vụ mang trạng thái CANCELLED.
     */
    boolean isCancelled();

    // =========================================================================
    // Static Factories (Khởi tạo)
    // =========================================================================

    /**
     * Tạo một ResultedAction từ một tác vụ (Callable).
     * <p>
     * Bất kỳ Exception nào quăng ra từ {@code task.call()} đều sẽ bị hệ thống bắt lại
     * và tự động đóng gói vào {@link ActionResult#failure(Throwable)}.
     * </p>
     *
     * @param task     Tác vụ cần thực thi.
     * @param executor Thread pool để chạy tác vụ (mặc định dùng {@link ExecutorManager#io()} nếu là null).
     * @param <T>      Kiểu dữ liệu trả về.
     * @return Một ResultedAction sẵn sàng để chạy hoặc nối chuỗi.
     */
    static <T> ResultedAction<T> supplyAsync(@NotNull Callable<T> task, @Nullable Executor executor) {
        Executor exec = (executor != null) ? executor : ExecutorManager.io();

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
    // Data Classes (Các lớp Dữ liệu)
    // =========================================================================

    /**
     * Định nghĩa các trạng thái cuối cùng có thể xảy ra của một ResultedAction.
     */
    enum ActionStatus {
        /** Hành động đã hoàn tất thành công và mang theo dữ liệu. */
        SUCCESS,
        /** Hành động gặp lỗi (Exception/Timeout) trong quá trình thực thi. */
        FAILURE,
        /** Hành động đã bị hủy bỏ chủ động (Cancelled) trước khi hoàn thành. */
        CANCELLED
    }

    /**
     * Lớp Wrapper bao bọc kết quả trả về của một {@link ResultedAction}.
     * Nó buộc người lập trình phải kiểm tra trạng thái trước khi tương tác với dữ liệu,
     * đảm bảo an toàn tuyệt đối (Null/Exception Safety).
     *
     * @param <T> Kiểu dữ liệu chứa bên trong (chỉ có nghĩa nếu trạng thái là SUCCESS).
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

        /**
         * Khởi tạo kết quả mang trạng thái THÀNH CÔNG.
         *
         * @param value Dữ liệu trả về từ tác vụ.
         * @param <T> Kiểu dữ liệu.
         * @return ActionResult đại diện cho sự thành công.
         */
        public static <T> ActionResult<T> success(T value) {
            return new ActionResult<>(ActionStatus.SUCCESS, value, null);
        }

        /**
         * Khởi tạo kết quả mang trạng thái THẤT BẠI.
         * <p>
         * Tính năng thông minh: Nếu ngoại lệ truyền vào mang dấu hiệu của việc "Hủy bỏ"
         * (CancellationException), hàm sẽ tự động trả về trạng thái BỊ HỦY thay vì THẤT BẠI.
         * </p>
         *
         * @param exception Ngoại lệ gây ra lỗi.
         * @param <T> Kiểu dữ liệu.
         * @return ActionResult đại diện cho sự thất bại (hoặc bị hủy).
         */
        public static <T> ActionResult<T> failure(Throwable exception) {
            if (exception instanceof CancellationException || exception.getCause() instanceof CancellationException) {
                return cancelled();
            }
            return new ActionResult<>(ActionStatus.FAILURE, null, exception);
        }

        /**
         * Khởi tạo kết quả mang trạng thái BỊ HỦY.
         *
         * @param <T> Kiểu dữ liệu.
         * @return ActionResult đại diện cho việc tác vụ đã bị ngắt.
         */
        public static <T> ActionResult<T> cancelled() {
            return new ActionResult<>(ActionStatus.CANCELLED, null, null);
        }

        /** @return {@code true} nếu action hoàn tất và có dữ liệu hợp lệ. */
        public boolean isSuccess() { return status == ActionStatus.SUCCESS; }

        /** @return {@code true} nếu action bị gián đoạn do lỗi. */
        public boolean isFailure() { return status == ActionStatus.FAILURE; }

        /** @return {@code true} nếu action bị ngừng bởi thao tác Hủy. */
        public boolean isCancelled() { return status == ActionStatus.CANCELLED; }

        /** @return Enum thể hiện trạng thái chính thức của tác vụ. */
        @NotNull public ActionStatus getStatus() { return status; }

        /**
         * Lấy dữ liệu kết quả của tác vụ.
         * @return Dữ liệu kiểu T, hoặc {@code null} nếu tác vụ thất bại/bị hủy.
         */
        public T getValue() { return value; }

        /**
         * Lấy thông tin lỗi.
         * @return Ngoại lệ gây ra lỗi, hoặc {@code null} nếu tác vụ thành công/bị hủy an toàn.
         */
        public Throwable getException() { return exception; }
    }
}
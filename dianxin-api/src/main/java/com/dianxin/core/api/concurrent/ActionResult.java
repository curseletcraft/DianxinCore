package com.dianxin.core.api.concurrent;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CancellationException;

/**
 * Lớp Wrapper bao bọc kết quả trả về của một {@link ResultedAction}.
 * Nó buộc người lập trình phải kiểm tra trạng thái trước khi tương tác với dữ liệu,
 * đảm bảo an toàn tuyệt đối (Null/Exception Safety).
 *
 * @param <T> Kiểu dữ liệu chứa bên trong (chỉ có nghĩa nếu trạng thái là SUCCESS).
 */
public class ActionResult<T> {
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
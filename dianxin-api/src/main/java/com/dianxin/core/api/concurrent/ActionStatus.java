package com.dianxin.core.api.concurrent;

/**
 * Định nghĩa các trạng thái cuối cùng có thể xảy ra của một ResultedAction.
 */
public enum ActionStatus {
    /** Hành động đã hoàn tất thành công và mang theo dữ liệu. */
    SUCCESS,
    /** Hành động gặp lỗi (Exception/Timeout) trong quá trình thực thi. */
    FAILURE,
    /** Hành động đã bị hủy bỏ chủ động (Cancelled) trước khi hoàn thành. */
    CANCELLED
}

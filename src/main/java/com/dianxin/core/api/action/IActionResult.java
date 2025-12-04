package com.dianxin.core.api.action;

public interface IActionResult<T> extends IAction {

    /**
     * @return trạng thái thực thi
     */
    ActionStatus status();

    /**
     * @return kết quả (nullable)
     */
    T result();

    /**
     * @return exception nếu thất bại
     */
    Throwable error();
}

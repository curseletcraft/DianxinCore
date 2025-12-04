package com.dianxin.core.api.action;

public interface IAction {
    /**
     * @return tên action (log key)
     * ví dụ: "command.execute", "permission.check"
     */
    String name();

    /**
     * @return context bổ sung (structured logging)
     */
    ActionContext context();
}

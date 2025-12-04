package com.dianxin.core.api.action;

public abstract class BaseActionResult<T> extends BaseAction implements IActionResult<T> {

    private final ActionStatus status;
    private final T result;
    private final Throwable error;

    protected BaseActionResult(
            String name,
            ActionContext context,
            ActionStatus status,
            T result,
            Throwable error
    ) {
        super(name, context);
        this.status = status;
        this.result = result;
        this.error = error;
    }

    @Override
    public ActionStatus status() {
        return status;
    }

    @Override
    public T result() {
        return result;
    }

    @Override
    public Throwable error() {
        return error;
    }
}


package com.dianxin.core.api.action;

public abstract class BaseAction implements IAction {

    private final String name;
    private final ActionContext context;

    protected BaseAction(String name, ActionContext context) {
        this.name = name;
        this.context = context;
    }

    @Override
    public final String name() {
        return name;
    }

    @Override
    public final ActionContext context() {
        return context;
    }
}


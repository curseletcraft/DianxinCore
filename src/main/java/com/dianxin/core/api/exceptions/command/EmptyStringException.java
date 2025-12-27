package com.dianxin.core.api.exceptions.command;

public class EmptyStringException extends IllegalStateException {
    public EmptyStringException(String message) {
        super(message);
    }
}

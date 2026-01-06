package com.dianxin.core.api.exceptions.java;

@SuppressWarnings("unused")
public class EmptyStringException extends IllegalStateException {
    public EmptyStringException(String message) {
        super(message);
    }
}

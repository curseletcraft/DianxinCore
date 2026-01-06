package com.dianxin.core.api.exceptions.lifecycle;

@SuppressWarnings("unused")
public class ServiceIsAlreadyInitException extends IllegalStateException {
    public ServiceIsAlreadyInitException(String message) {
        super(message);
    }
}

package com.dianxin.core.api.exceptions;

@SuppressWarnings("unused")
public class ServiceIsAlreadyInitException extends IllegalStateException {
    public ServiceIsAlreadyInitException(String message) {
        super(message);
    }
}

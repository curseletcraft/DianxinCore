package com.dianxin.core.api.exceptions;

public class ServiceIsAlreadyInitException extends IllegalStateException {
    public ServiceIsAlreadyInitException(String message) {
        super(message);
    }
}

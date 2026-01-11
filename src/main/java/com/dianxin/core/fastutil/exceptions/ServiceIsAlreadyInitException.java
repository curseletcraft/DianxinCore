package com.dianxin.core.fastutil.exceptions;

@SuppressWarnings("unused")
public class ServiceIsAlreadyInitException extends IllegalStateException {
    public ServiceIsAlreadyInitException(String message) {
        super(message);
    }
}

package com.dianxin.core.api.exceptions;

@SuppressWarnings("unused")
public class ServiceUnavailableException extends IllegalStateException {
    public ServiceUnavailableException(String message) {
        super(message);
    }
}

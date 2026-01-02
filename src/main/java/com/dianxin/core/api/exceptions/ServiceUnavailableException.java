package com.dianxin.core.api.exceptions;

public class ServiceUnavailableException extends IllegalStateException {
    public ServiceUnavailableException(String message) {
        super(message);
    }
}

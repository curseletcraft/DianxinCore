package com.dianxin.core.api.exceptions.lifecycle;

public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message) {
        super(message);
    }
}

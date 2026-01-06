package com.dianxin.core.api.exceptions.lifecycle;

@SuppressWarnings("unused")
public class ServiceUnavailableException extends IllegalStateException {
    public ServiceUnavailableException(String message) {
        super(message);
    }
}

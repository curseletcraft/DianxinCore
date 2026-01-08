package com.dianxin.core.api.exceptions.lifecycle;

@SuppressWarnings("unused")
public class ServiceUnavailableException extends UnsupportedOperationException {
    public ServiceUnavailableException(String message) {
        super(message);
    }
}

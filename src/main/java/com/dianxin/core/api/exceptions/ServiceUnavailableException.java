package com.dianxin.core.api.exceptions;

@SuppressWarnings("unused")
public class ServiceUnavailableException extends UnsupportedOperationException {
    public ServiceUnavailableException(String message) {
        super(message);
    }
}

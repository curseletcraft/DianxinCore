package com.dianxin.core.api.exceptions;

@SuppressWarnings("unused")
public class InvalidRegistrationNameException extends IllegalStateException {
    public InvalidRegistrationNameException(String message) {
        super(message);
    }
}

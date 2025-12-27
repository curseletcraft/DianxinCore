package com.dianxin.core.api.exceptions.command;

public class InvalidRegistrationNameException extends IllegalStateException {
    public InvalidRegistrationNameException(String message) {
        super(message);
    }
}

package com.dianxin.core.api.exceptions;

public final class EnvVariableNotFoundException extends IllegalStateException {
    public EnvVariableNotFoundException(String key) {
        super("Missing environment variable: " + key);
    }
}

package com.dianxin.core.api.config.env;

public final class EnvVariableNotFoundException extends IllegalStateException {
    public EnvVariableNotFoundException(String key) {
        super("Missing environment variable: " + key);
    }
}

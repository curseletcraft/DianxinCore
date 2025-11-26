package com.dianxin.core.api.config.env;

import io.github.cdimascio.dotenv.Dotenv;

public class VirtualEnvironmentConfiguration {
    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    private VirtualEnvironmentConfiguration() {}

    public static String get(String key) {
        return dotenv.get(key);
    }

    public static String getOrThrow(String key) {
        String value = dotenv.get(key);
        if (value == null) {
            throw new IllegalStateException("Missing environment variable: " + key);
        }
        return value;
    }
}

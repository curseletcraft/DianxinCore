package com.dianxin.core.api.config.env;

import io.github.cdimascio.dotenv.Dotenv;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

@NullMarked
@SuppressWarnings("unused")
public class VirtualEnvironmentConfiguration {
    private static final Dotenv dotenv = VirtualEnvironmentConfiguration.load();

    private static Dotenv load() {
        String dir = System.getenv().getOrDefault("DOTENV_DIR", ".");
        return Dotenv.configure().directory(dir).ignoreIfMissing().load();
    }

    private VirtualEnvironmentConfiguration() {}

    @Nullable
    public static String get(String key) {
        // Ưu tiên env hệ thống trước
        String sys = System.getenv(key);
        return sys != null ? sys : dotenv.get(key);
    }

    public static String getOrThrow(String key) {
        String value = get(key);
        if (value == null) {
            throw new EnvVariableNotFoundException(key);
        }
        return value;
    }
}

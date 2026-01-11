package com.dianxin.core.fastutil.exceptions;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class UtilityClassInitializationException extends UnsupportedOperationException {
    public UtilityClassInitializationException(@NotNull String s) {
        super(s);
    }

    public UtilityClassInitializationException() {
        super();
    }

    public UtilityClassInitializationException(@NotNull Class<?> clazz) {
        super(clazz.getName() + " utility class cannot be initialized!");
    }
}

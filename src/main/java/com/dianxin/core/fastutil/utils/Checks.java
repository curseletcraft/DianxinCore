package com.dianxin.core.fastutil.utils;

public final class Checks {
    public static void notNull(Object arg, String name) {
        if (arg == null) {
            throw new IllegalArgumentException(name + " cannot be null");
        }
    }
}
